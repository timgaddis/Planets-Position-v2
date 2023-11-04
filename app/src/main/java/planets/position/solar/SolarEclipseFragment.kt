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

package planets.position.solar

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
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
import planets.position.database.solar.Solar
import planets.position.databinding.FragmentSolarEclipseBinding
import java.util.Calendar
import java.util.TimeZone

class SolarEclipseFragment : Fragment() {

    private val solarEclipseViewModel: SolarEclipseViewModel by viewModels { SolarEclipseViewModel.Factory }
    private var _binding: FragmentSolarEclipseBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var settings: SharedPreferences
    private lateinit var textSolar: TextView
    private lateinit var progressSolar: ProgressBar
    private lateinit var solarRecyclerView: RecyclerView
    private val g = DoubleArray(3)
    private var offset = 0.0
    private var lastUpdate = 0L
    private var firstRun = false
    private var newLoc = false
    private var zoneName: String = ""

    private fun adapterOnClick(solar: Solar) {
        val bundle = Bundle()
        bundle.putInt("solarID", solar.id)
        bundle.putDouble("latitude", g[1])
        bundle.putDouble("longitude", g[0])
        navController.navigate(R.id.action_nav_solar_eclipse_to_nav_solar_eclipse_data, bundle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val navigationHost =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navigationHost.navController
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSolarEclipseBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupMenu()
        loadSettings()

        textSolar = binding.solarText
        progressSolar = binding.solarProgress

        val solarAdapter = SolarEclipseListAdapter { solar -> adapterOnClick(solar) }
        solarRecyclerView = binding.solarList
        solarRecyclerView.adapter = solarAdapter

        solarEclipseViewModel.getSolarList().observe(viewLifecycleOwner) {
            it.let {
                firstRun = false
                saveSettings()
                solarAdapter.submitList(it)
                if (it.size == 10) {
                    solarRecyclerView.visibility = VISIBLE
                    textSolar.visibility = GONE
                    progressSolar.visibility = GONE
                }
            }
        }

        if (g[1] < -90) {
            navController.navigate(R.id.nav_location_dialog)
        } else {
            if (firstRun) {
                lastUpdate = Calendar.getInstance().timeInMillis
                saveSettings()
                launchTask(lastUpdate, 0)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.eclipse_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.action_previous -> {
                        launchTask(lastUpdate, -1)
                        return true
                    }

                    R.id.action_next -> {
                        launchTask(lastUpdate, 1)
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
                            launchTask(lastUpdate, 0)
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
        newLoc = settings.getBoolean("newLocation", true)
        lastUpdate = settings.getLong("seLastUpdate", 0)
        firstRun = settings.getBoolean("seFirstRun", true)
    }

    private fun saveSettings() {
        with(settings.edit()) {
            putLong("seLastUpdate", lastUpdate)
            putBoolean("seFirstRun", firstRun)
            apply()
        }
    }

    private fun launchTask(time: Long, back: Int) {
        solarRecyclerView.visibility = GONE
        textSolar.visibility = VISIBLE
        progressSolar.visibility = VISIBLE
        solarEclipseViewModel.launchTask(back, time)
    }
}
