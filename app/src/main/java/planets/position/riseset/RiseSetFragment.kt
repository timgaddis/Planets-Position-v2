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

package planets.position.riseset

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import planets.position.R
import planets.position.database.planet.Planet
import planets.position.databinding.FragmentRiseSetBinding
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import java.util.Calendar
import java.util.GregorianCalendar

class RiseSetFragment : Fragment() {

    private val riseSetViewModel: RiseSetViewModel by viewModels { RiseSetViewModel.Factory }
    private var _binding: FragmentRiseSetBinding? = null
    private val binding get() = _binding!!
    private var lastUpdate: Long = 0
    private var offset = 0.0
    private var zoneName: String = ""
    private var g = DoubleArray(3)
    private var viewIndex = 0
    private lateinit var settings: SharedPreferences
    private lateinit var navController: NavController
    private lateinit var riseRadio: AppCompatRadioButton
    private lateinit var setRadio: AppCompatRadioButton
    private lateinit var allRadio: AppCompatRadioButton
    private lateinit var updateText: TextView
    private lateinit var mDateFormat: DateFormat
    private lateinit var mTimeFormat: DateFormat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDateFormat = android.text.format.DateFormat
            .getDateFormat(requireActivity().applicationContext)
        mTimeFormat = android.text.format.DateFormat
            .getTimeFormat(requireActivity().applicationContext)
        settings = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val navigationHost =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navigationHost.navController
    }

    private fun adapterOnClick(planet: Planet) {
        val bundle = Bundle()
        bundle.putInt("planetID", planet.cID)
        bundle.putLong("lastUpdate", lastUpdate)
        bundle.putDouble("offset", offset)
        navController.navigate(R.id.action_nav_rise_set_to_nav_rise_set_data, bundle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRiseSetBinding.inflate(inflater, container, false)
        val root: View = binding.root

        riseRadio = binding.riseRadioButton as AppCompatRadioButton
        setRadio = binding.setRadioButton as AppCompatRadioButton
        allRadio = binding.allRadioButton as AppCompatRadioButton
        updateText = binding.lastUpdateText
        val rgLayout = binding.radioGroupRS

        loadSettings()

        rgLayout.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            when (checkedId) {
                R.id.riseRadioButton -> viewIndex = 0
                R.id.setRadioButton -> viewIndex = 1
                R.id.allRadioButton -> viewIndex = 2
            }
            riseSetViewModel.saveView(viewIndex)
            with(settings.edit()) {
                putInt("viewIndex", viewIndex)
                apply()
            }
            loadPlanets(viewIndex)
        }

        setupMenu()

        val planetsAdapter = RiseSetListAdapter { planet -> adapterOnClick(planet) }

        val planetsRecycler = binding.planetsList
        planetsRecycler.adapter = planetsAdapter

        riseSetViewModel.getPlanets()?.observe(viewLifecycleOwner) {
            it.let {
                planetsAdapter.submitList(it)
            }
        }

        riseSetViewModel.getZone().observe(viewLifecycleOwner) {
            it.let {
                if (it != null) {
                    val df = DecimalFormat("##.#")
                    df.roundingMode = RoundingMode.DOWN
                    offset = df.format(it.gmt_offset / 3600.0).toDouble()
                    riseSetViewModel.setOffset(offset)
                }
            }
        }

        if (g[1] < -90) {
//            navCo.navigate(R.id.nav_location_dialog)
            Toast.makeText(requireContext(), "rise lat < -90.0", Toast.LENGTH_LONG).show()
        } else {
            lastUpdate = Calendar.getInstance().timeInMillis
            riseSetViewModel.saveTime(lastUpdate)
//            saveSettings()
            loadPlanets(viewIndex)
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
                menuInflater.inflate(R.menu.riseset_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.action_refresh) {
                    lastUpdate = Calendar.getInstance().timeInMillis
                    riseSetViewModel.saveTime(lastUpdate)
//                    saveSettings()
                    loadPlanets(viewIndex)
                    return true
                }
                return false
            }
        }, viewLifecycleOwner)
    }

    private fun loadSettings() {
        // read location from Shared Preferences
        g[1] = settings.getFloat("latitude", (-91.0).toFloat()).toDouble()
        g[0] = settings.getFloat("longitude", 0f).toDouble()
        g[2] = settings.getFloat("altitude", 0f).toDouble()
        offset = settings.getFloat("offset", 0f).toDouble()
        lastUpdate = settings.getLong("lastUpdate", 0)
        viewIndex = settings.getInt("viewIndex", 1)
        zoneName = settings.getString("zoneName", "").toString()
    }

    private fun loadPlanets(index: Int) {
        val gc: Calendar = GregorianCalendar()
        gc.clear()
        gc.timeInMillis = lastUpdate
        when (index) {
            0 -> {
                updateText.text = mTimeFormat.let {
                    mDateFormat.let { it1 ->
                        String.format(
                            "What's rising on %s @ %s",
                            it1.format(gc.time), it.format(gc.time)
                        )
                    }
                }
                riseRadio.isChecked = true
            }

            1 -> {
                updateText.text = mDateFormat.let {
                    mTimeFormat.let { it1 ->
                        String.format(
                            "What's setting on %s @ %s",
                            it.format(gc.time), it1.format(gc.time)
                        )
                    }
                }
                setRadio.isChecked = true
            }

            else -> {
                updateText.text = mDateFormat.let {
                    mTimeFormat.let { it1 ->
                        String.format(
                            "All Planets on %s @ %s",
                            it.format(gc.time), it1.format(gc.time)
                        )
                    }
                }
                allRadio.isChecked = true
            }
        }
    }
}
