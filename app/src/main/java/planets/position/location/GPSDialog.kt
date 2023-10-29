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

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import planets.position.R

class GPSDialog : DialogFragment() {
    private lateinit var navController: NavController
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var permissionRequest: ActivityResultLauncher<Array<String>>

    companion object {
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navigationHost =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navigationHost.navController
        dialog?.setCanceledOnTouchOutside(false)
        registerPermissionRequest()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alert = AlertDialog.Builder(activity, R.style.LocDialogTheme)
        val inflaterLat = requireActivity().layoutInflater
        inflaterLat.inflate(R.layout.fragment_gpsdialog, null)
        return alert.create()
    }

    override fun onStart() {
        super.onStart()
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            getLastLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            })
            .addOnSuccessListener { location: Location? ->
                if (location == null) {
                    // location not found
                    val b = Bundle()
                    b.putInt("GPS", 200)
//                    navController.navigate(R.id.nav_location, b)
                    navController.navigate(R.id.action_nav_gps_dialog_to_nav_location, b)
                } else {
                    val b = Bundle()
                    b.putInt("GPS", 100)
                    b.putDouble("latitude", location.latitude)
                    b.putDouble("longitude", location.longitude)
                    b.putLong("last_update", location.time)
                    b.putDouble("altitude", location.altitude)
//                    navController.navigate(R.id.nav_location, b)
                    navController.navigate(R.id.action_nav_gps_dialog_to_nav_location, b)
                }
            }
    }

    private fun registerPermissionRequest() {
        var permissionCount = 0
        permissionRequest =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissions.entries.forEach {
                    if (it.value) {
                        permissionCount++
                    }
                }
                if (permissionCount == 2) {
                    getLastLocation()
                } else {
                    // Permission denied.
                    val b = Bundle()
                    b.putInt("GPS", 500)
                    navController.navigate(R.id.action_nav_gps_dialog_to_nav_location, b)
                }
            }
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            Log.i("GPSDialog", "Displaying permission rationale to provide additional context.")
            val b = Bundle()
            b.putInt("GPS", 300)
            navController.navigate(R.id.action_nav_gps_dialog_to_nav_location, b)
        } else {
            Log.i("GPSDialog", "Requesting permission")
            permissionRequest.launch(LOCATION_PERMISSIONS)
        }
    }
}
