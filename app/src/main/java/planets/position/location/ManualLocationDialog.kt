/*
 * Planet's Position
 * A program to calculate the position of the planets in the night sky
 * based on a given location on Earth.
 * Copyright (c) 2023-2024 Tim Gaddis
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
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import planets.position.R
import planets.position.database.timezone.State
import planets.position.database.timezone.WorldCity
import planets.position.databinding.FragmentManualLocationDialogBinding

class ManualLocationDialog : DialogFragment() {

    private val manualLocationViewModel: ManualLocationDialogViewModel by viewModels { ManualLocationDialogViewModel.Factory }
    private var country: String = ""
    private var state: String = ""
    private var cityID = -1
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navigationHost =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navigationHost.navController
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val bind: FragmentManualLocationDialogBinding =
            FragmentManualLocationDialogBinding.inflate(inflater)
        dialog?.setContentView(bind.root)
        val spinCountry = bind.spinnerCountry
        val spinState = bind.spinnerState
        val spinCity = bind.spinnerCity

        val adapter = ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.country_array, R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_drop_item)
        spinCountry.adapter = adapter

        manualLocationViewModel.getCountry()?.observe(this) {
            it.let {
                if (it != null)
                    manualLocationViewModel.saveCountryCode(it.country_code)
            }
        }
        manualLocationViewModel.getStateList()?.observe(this) {
            it.let {
                // load state spinner
                val adapter1 = ArrayAdapter(requireContext(), R.layout.spinner_item, it)
                adapter1.setDropDownViewResource(R.layout.spinner_drop_item)
                spinState.adapter = adapter1
            }
        }
        manualLocationViewModel.getCityList()?.observe(this) {
            it.let {
                // load city spinner
                val adapter2 = ArrayAdapter(requireContext(), R.layout.spinner_item, it)
                adapter2.setDropDownViewResource(R.layout.spinner_drop_item)
                spinCity.adapter = adapter2
            }
        }
        spinCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                country = parent.getItemAtPosition(position) as String
                manualLocationViewModel.saveCountry(country)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinState.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                val s: State = parent.getItemAtPosition(position) as State
                state = s.state
                manualLocationViewModel.saveState(state)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                val wc: WorldCity = parent.getItemAtPosition(position) as WorldCity
                cityID = wc._id
                manualLocationViewModel.saveCityID(cityID)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        val alert = MaterialAlertDialogBuilder(requireContext(), R.style.CustomMaterialDialog)
        alert.setTitle(R.string.loc_user_city)
        alert.setView(bind.root)
        alert.setPositiveButton(R.string.action_save) { _, _ ->
            val b = Bundle()
            b.putInt("cityID", cityID)
            navController.navigate(R.id.action_nav_manual_location_dialog_to_nav_location, b)
        }
        alert.setNegativeButton(R.string.action_cancel) { _, _ ->
            val b = Bundle()
            b.putInt("cityID", -2)
            navController.navigate(R.id.action_nav_manual_location_dialog_to_nav_location, b)
        }
        return alert.create()
    }
}
