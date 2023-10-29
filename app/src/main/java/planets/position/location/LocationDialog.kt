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

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import planets.position.R

class LocationDialog : DialogFragment() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navigationHost =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navigationHost.navController
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.alert_string)
                .setPositiveButton(
                    R.string.loc_down
                ) { _, _ ->
                    // GPS
//                    navController.navigate(R.id.nav_gps_dialog)
                    val b = Bundle()
                    b.putBoolean("manual", false)
                    navController.navigate(
                        R.id.action_nav_location_dialog_to_nav_location, b
                    )
                }
                .setNegativeButton(
                    R.string.alert_manual
                ) { _, _ ->
                    // Manual
//                    navController.navigate(R.id.nav_manual_location_dialog)
                    val b = Bundle()
                    b.putBoolean("manual", true)
                    navController.navigate(
                        R.id.action_nav_location_dialog_to_nav_location, b
                    )
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
