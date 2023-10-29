/*
 * Planet's Position
 * A program to calculate the position of the planets in the night sky
 * based on a given location on Earth.
 * Copyright (c) 2023 Tim Gaddis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package planets.position.lunar.occult

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import planets.position.R
import planets.position.database.lunar.Occult
import planets.position.databinding.FragmentLunarOccultBinding
import java.util.Calendar
import java.util.TimeZone

class LunarOccultFragment : Fragment() {

    private val lunarOccultViewModel: LunarOccultViewModel by viewModels { LunarOccultViewModel.Factory }
    private var _binding: FragmentLunarOccultBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var settings: SharedPreferences
    private lateinit var textOccult: TextView
    private lateinit var progressOccult: ProgressBar
    private lateinit var occultRecyclerView: RecyclerView
    private lateinit var planetNames: List<String>
    private val g = DoubleArray(3)
    private var offset = 0.0
    private var lastUpdate = 0L
    private var firstRun = false
    private var planetNum = 0
    private var allPlanets = false
    private var userSelect = false
    private var zoneName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val navigationHost =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navigationHost.navController
        planetNames = listOf<String>(*resources.getStringArray(R.array.occult_array))
    }

    private fun adapterOnClick(occult: Occult) {
        val bundle = Bundle()
        bundle.putInt("occultID", occult.id)
        navController.navigate(R.id.action_nav_lunar_occult_to_nav_lunar_occult_data, bundle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLunarOccultBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupMenu()
        loadSettings()
        textOccult = binding.occultText
        progressOccult = binding.occultProgress

        userSelect = false
        val occultSpinner = binding.occultSpinner
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.occult_array, R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_drop_item)
        occultSpinner.adapter = adapter
        occultSpinner.setSelection(planetNum - 1)

        occultSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (userSelect) {
                    allPlanets = position == 0
                    requireActivity().invalidateOptionsMenu()
                    planetNum = position + 1
                    val c = Calendar.getInstance()
                    lastUpdate = c.timeInMillis
                    saveSettings()
                    launchTask(lastUpdate, 0, planetNum)
                } else {
                    userSelect = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("Lunar Occult", "Nothing Selected")
            }

        }

        val occultAdapter = LunarOccultListAdapter { occult -> adapterOnClick(occult) }
        occultRecyclerView = binding.occultList
        occultRecyclerView.adapter = occultAdapter

        lunarOccultViewModel.getOccultList().observe(viewLifecycleOwner) {
            it.let {
                firstRun = false
                saveSettings()
                occultAdapter.submitList(it)
                occultRecyclerView.visibility = View.VISIBLE
                textOccult.visibility = View.GONE
                progressOccult.visibility = View.GONE
            }
        }

        if (g[1] < -90) {
            navController.navigate(R.id.nav_location_dialog)
        } else {
            if (firstRun) {
                lastUpdate = Calendar.getInstance().timeInMillis
                saveSettings()
                launchTask(lastUpdate, 0, planetNum)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMenu() {
        var next: MenuItem? = null
        var previous: MenuItem? = null
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.eclipse_menu, menu)
                next = menu.findItem(R.id.action_next)
                previous = menu.findItem(R.id.action_previous)
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                next?.isVisible = !allPlanets
                previous?.isVisible = !allPlanets
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.action_previous -> {
                        launchTask(lastUpdate, -1, planetNum)
                        return true
                    }

                    R.id.action_next -> {
                        launchTask(lastUpdate, 1, planetNum)
                        return true
                    }

                    R.id.action_calendar -> {
                        // material date picker
                        val datePicker =
                            MaterialDatePicker.Builder.datePicker()
                                .setTitleText("Select date")
                                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                                .setTheme(R.style.MaterialCalendarTheme)
                                .build()
                        datePicker.show(parentFragmentManager, datePicker.toString())
                        datePicker.addOnPositiveButtonClickListener {
                            val selectedUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            selectedUtc.timeInMillis = it
                            val selectedLocal = Calendar.getInstance()
                            selectedLocal.clear()
                            selectedLocal.set(
                                selectedUtc.get(Calendar.YEAR),
                                selectedUtc.get(Calendar.MONTH),
                                selectedUtc.get(Calendar.DAY_OF_MONTH),
                                0,
                                0,
                                0
                            )
                            lastUpdate = selectedLocal.timeInMillis
                            saveSettings()
                            launchTask(lastUpdate, 0, planetNum)
                        }
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner)
    }

    private fun loadSettings() {
        // read settings from Shared Preferences
        g[1] = settings.getFloat("latitude", (-91.0).toFloat()).toDouble()
        g[0] = settings.getFloat("longitude", 0f).toDouble()
        g[2] = settings.getFloat("altitude", 0f).toDouble()
        offset = settings.getFloat("offset", 0f).toDouble()
        zoneName = settings.getString("zoneName", "").toString()
        firstRun = settings.getBoolean("loFirstRun", true)
        allPlanets = settings.getBoolean("loAllPlanets", true)
        planetNum = settings.getInt("loPlanetNum", 1)
    }

    private fun saveSettings() {
        with(settings.edit()) {
            putBoolean("loFirstRun", firstRun)
            putLong("loLastUpdate", lastUpdate)
            putBoolean("loAllPlanets", allPlanets)
            putInt("loPlanetNum", planetNum)
            apply()
        }
    }

    private fun launchTask(time: Long, back: Int, planet: Int) {
        occultRecyclerView.visibility = View.GONE
        textOccult.visibility = View.VISIBLE
        progressOccult.visibility = View.VISIBLE
        if (planet == 1) {
            textOccult.text = String.format(
                "Calculating the first\noccultation for %s",
                planetNames[planet]
            )
        } else {
            textOccult.text = String.format(
                "Calculating occultations for %s",
                planetNames[planet - 1]
            )
        }
        lunarOccultViewModel.launchTask(back, time, planet)
    }
}
