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

package planets.position.skyposition

import android.app.AlertDialog
import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import planets.position.R

class PlanetDialog : DialogFragment() {
    private lateinit var navController: NavController
    private lateinit var settings: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navigationHost =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navigationHost.navController
        settings = PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, R.style.LocDialogTheme)
            builder.setTitle(R.string.planet_select)
                .setItems(
                    R.array.planets_array
                ) { _, which ->
                    val b = Bundle()
                    b.putInt("planet", which)
                    Log.d("PlanetsPosition", "PlanetDialog p:${which}")
                    with(settings.edit()) {
                        putInt("sky_planet", which)
                    }
                    navController.navigate(R.id.action_nav_planet_dialog_to_nav_sky_position, b)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
