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

package planets.position.location

import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.Group
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import planets.position.R
import planets.position.databinding.FragmentLocationBinding
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

class LocationFragment : Fragment() {

    private val locationViewModel: LocationViewModel by viewModels { LocationViewModel.Factory }
    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var settings: SharedPreferences
    private lateinit var latitudeText: TextView
    private lateinit var longitudeText: TextView
    private lateinit var elevationText: TextView
    private lateinit var timezoneText: TextView
    private lateinit var gmtOffsetText: TextView
    private lateinit var latitudeEdit: EditText
    private lateinit var longitudeEdit: EditText
    private lateinit var elevationEdit: EditText
    private lateinit var timezoneEdit: EditText
    private lateinit var spinnerLat: Spinner
    private lateinit var spinnerLong: Spinner
    private lateinit var groupText: Group
    private lateinit var groupEdit: Group
    private lateinit var groupButtons: Group
    private var lastUpdate: Long = 0
    private var latitude = 0.0
    private var longitude: Double = 0.0
    private var altitude: Double = 0.0
    private var offset: Double = 0.0
    private var cityID: Int = -1
    private var edit = false
    private var manualEdit: Boolean = false
    private var zoneName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navigationHost =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navigationHost.navController
        settings = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navController.popBackStack(R.id.nav_main, false)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            callback
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        latitudeText = binding.newLatText
        longitudeText = binding.newLongText
        elevationText = binding.newElevationText
        gmtOffsetText = binding.newGMTOffsetText
        timezoneText = binding.newTimezoneText
        latitudeEdit = binding.newLatEdit
        longitudeEdit = binding.newLongEdit
        elevationEdit = binding.newElevationEdit
        timezoneEdit = binding.newTimezoneEdit
        spinnerLat = binding.spinnerLat
        spinnerLong = binding.spinnerLong
        val buttonCity = binding.buttonCity
        val buttonEdit = binding.buttonEdit
        groupText = binding.manualEditGroup1
        groupEdit = binding.manualEditGroup2
        groupButtons = binding.manualEditGroup3

        timezoneEdit.inputType = InputType.TYPE_NULL
        timezoneEdit.isFocusable = false

        val adapterLat = ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.edit_lat, R.layout.spinner_item
        )
        adapterLat.setDropDownViewResource(R.layout.spinner_drop_item)
        spinnerLat.adapter = adapterLat

        val adapterLng = ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.edit_lng,
            R.layout.spinner_item
        )
        adapterLng.setDropDownViewResource(R.layout.spinner_drop_item)
        spinnerLong.adapter = adapterLng

        buttonCity.setOnClickListener {
            navController.navigate(R.id.action_nav_location_to_nav_manual_location_dialog)
        }

        buttonEdit.setOnClickListener {
            manualEdit = true
            showManualEdit()
        }

        setupMenu()

        locationViewModel.getCity()?.observe(viewLifecycleOwner) {
            it.let {
//                Log.d("PlanetsPosition", "in city observe:$it")
                if (it != null) {
//                    Log.d("PlanetsPosition", "in city observe:${it.timezone}")
                    latitude = it.lat
                    longitude = it.lng
                    altitude = it.altitude
                    zoneName = it.timezone
                    lastUpdate = Calendar.getInstance().timeInMillis
                    locationViewModel.saveZone(zoneName, lastUpdate)
                }
            }
        }

        locationViewModel.getZone()?.observe(viewLifecycleOwner) {
            it.let {
                if (it != null) {
                    val df = DecimalFormat("##.#")
                    df.roundingMode = RoundingMode.DOWN
                    offset = df.format(it.gmt_offset / 3600.0).toDouble()
                    saveLocation()
                    displayLocation()
                }
            }
        }

        timezoneEdit.setOnClickListener {
            saveManualEdit()
            saveLocation()
            navController.navigate(R.id.action_nav_location_to_nav_timezone_dialog)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments?.containsKey("cityID") == true) {
            // manual search for city
            cityID = requireArguments().getInt("cityID", -1)
            if (cityID > 0) locationViewModel.saveCityID(cityID)
            else {
                loadLocation()
                displayLocation()
            }
        } else if (arguments?.containsKey("timezone") == true) {
            // select timezone from dialog
            loadLocation()
            zoneName = requireArguments().getString("timezone").toString()
            saveLocation()
            displayLocation()
        } else if (arguments?.containsKey("manual") == true) {
            // GPS/Manual dialog
            if (requireArguments().getBoolean("manual")) {
                navController.navigate(R.id.action_nav_location_to_nav_manual_location_dialog)
            } else {
                navController.navigate(R.id.action_nav_location_to_nav_gps_dialog)
            }
        } else if (arguments?.containsKey("GPS") == true) {
            // GPS location
            when (requireArguments().getInt("GPS")) {
                100 -> {
                    // location found
                    showSnackBar(R.string.loc_found)
                    latitude = requireArguments().getDouble("latitude")
                    longitude = requireArguments().getDouble("longitude")
                    altitude = requireArguments().getDouble("altitude")
                    lastUpdate = requireArguments().getLong("last_update")
                    val t = TimeZone.getDefault()
                    zoneName = t.id
                    locationViewModel.saveZone(zoneName, lastUpdate)
                }

                200 -> {
                    // location not found
                    showSnackBar(R.string.no_location_detected)
                }

                300 -> {
                    // Displaying permission rationale
                    showSnackBar(R.string.permission_reason)
                }

                500 -> {
                    // Permission denied
                    showSnackBar(R.string.location_denied)
                }
            }
        } else {
            loadLocation()
            displayLocation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMenu() {
        var editLoc: MenuItem? = null
        var saveLoc: MenuItem? = null

        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.location_menu, menu)
                editLoc = menu.findItem(R.id.action_edit)
                saveLoc = menu.findItem(R.id.action_save)
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                editLoc?.isVisible = !edit
                saveLoc?.isVisible = edit
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                var save = true
                when (menuItem.itemId) {
                    R.id.action_save -> {
                        if (manualEdit) {
                            save = saveManualEdit()
                            manualEdit = false
                        }
                        edit = false
                        groupButtons.visibility = View.GONE
                        requireActivity().invalidateOptionsMenu()

                        groupEdit.visibility = View.GONE
                        groupText.visibility = View.VISIBLE

                        saveLocation()
                        displayLocation()
                        return save
                    }

                    R.id.action_edit -> {
                        edit = true
                        manualEdit = false
                        groupButtons.visibility = View.VISIBLE
                        requireActivity().invalidateOptionsMenu()
                        return true
                    }

                    R.id.action_gps -> {
                        navController.navigate(R.id.action_nav_location_to_nav_gps_dialog)
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner)
    }

    private fun saveManualEdit(): Boolean {
        try {
            latitude = latitudeEdit.text.toString().toDouble()
            if (spinnerLat.selectedItemPosition == 1) latitude *= -1.0
        } catch (nfe: NumberFormatException) {
            Toast.makeText(
                requireActivity(),
                "Please enter a value for the latitude.", Toast.LENGTH_SHORT
            ).show()
            return false
        }
        try {
            longitude = longitudeEdit.text.toString().toDouble()
            if (spinnerLong.selectedItemPosition == 1) longitude *= -1.0
        } catch (nfe: NumberFormatException) {
            Toast.makeText(
                requireActivity(),
                "Please enter a value for the longitude.", Toast.LENGTH_SHORT
            ).show()
            return false
        }
        altitude = try {
            elevationEdit.text.toString().toDouble()
        } catch (nfe: NumberFormatException) {
            Toast.makeText(
                requireActivity(),
                "Please enter a value for the elevation.", Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    private fun showManualEdit() {
        // Show the EditTexts
        groupText.visibility = View.GONE
        groupEdit.visibility = View.VISIBLE
        latitudeEdit.setText(abs(latitude).toString())
        if (latitude >= 0) spinnerLat.setSelection(0) else spinnerLat.setSelection(1)
        longitudeEdit.setText(abs(longitude).toString())
        if (longitude >= 0) spinnerLong.setSelection(0) else spinnerLong.setSelection(1)
        elevationEdit.setText(altitude.toString())
        timezoneEdit.setText(zoneName)
    }

    private fun displayLocation() {
        if (latitude >= 0) {
            latitudeText.text = String.format(Locale.getDefault(), "%.6f° N", latitude)
        } else {
            latitudeText.text = String.format(Locale.getDefault(), "%.6f° S", abs(latitude))
        }
        if (longitude >= 0) {
            longitudeText.text = String.format(Locale.getDefault(), "%.6f° E", longitude)
        } else {
            longitudeText.text = String.format(Locale.getDefault(), "%.6f° W", abs(longitude))
        }
        elevationText.text = String.format(Locale.getDefault(), "%.1f m", altitude)
        timezoneText.text = zoneName
        gmtOffsetText.text = String.format(Locale.getDefault(), "%.2f", offset)
    }

    private fun saveLocation() {
        with(settings.edit()) {
            this?.putFloat("latitude", latitude.toFloat())
            this?.putFloat("longitude", longitude.toFloat())
            this?.putFloat("altitude", altitude.toFloat())
            this?.putFloat("offset", offset.toFloat())
            this?.putString("zoneName", zoneName)
            this?.putLong("date", lastUpdate)
            this?.putBoolean("newLocation", true)
            this?.apply()
        }
    }

    private fun loadLocation() {
        // read location from Shared Preferences
        latitude = settings.getFloat("latitude", 0f).toDouble()
        longitude = settings.getFloat("longitude", 0f).toDouble()
        altitude = settings.getFloat("altitude", 0f).toDouble()
        offset = settings.getFloat("offset", 0f).toDouble()
        zoneName = settings.getString("zoneName", "").toString()
        lastUpdate = settings.getLong("date", 0)
    }

    private fun showSnackBar(message: Int) {
        val container = requireView().findViewById<View>(R.id.fragment_location_container)
        Snackbar.make(
            container,
            message,
            Snackbar.LENGTH_SHORT
        ).show()

    }
}
