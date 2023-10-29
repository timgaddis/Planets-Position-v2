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

package planets.position.settings

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import planets.position.R

class SettingsDialogFragment : DialogFragment() {

    private lateinit var navController: NavController
    private var index = 0
    private var textID = 0
    private var arrayID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navigationHost =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navigationHost.navController

        if (arguments?.containsKey("settingID") == true) {
            index = requireArguments().getInt("settingID", 0)
            textID = requireArguments().getInt("textID", 0)
            arrayID = requireArguments().getInt("arrayID", 0)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, R.style.LocDialogTheme)
            builder.setTitle(textID)
                .setItems(
                    arrayID
                ) { _, which ->
                    val s: String = resources.getStringArray(arrayID)[which]
                    val b = Bundle()
                    b.putInt("settingID", index)
                    b.putInt("stringID", which)
                    b.putString("settingString", s)
                    navController.navigate(R.id.action_nav_settings_dialog_to_nav_settings, b)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
