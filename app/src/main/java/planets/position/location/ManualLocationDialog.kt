package planets.position.location

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import planets.position.R
import planets.position.database.timezone.State
import planets.position.database.timezone.WorldCity

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

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alert = AlertDialog.Builder(activity, R.style.LocDialogTheme)
        val inflaterLat = requireActivity().layoutInflater
        val v = inflaterLat.inflate(R.layout.fragment_manual_location_dialog, null)
        val spinCountry = v.findViewById<Spinner>(R.id.spinnerCountry)
        val spinState = v.findViewById<Spinner>(R.id.spinnerState)
        val spinCity = v.findViewById<Spinner>(R.id.spinnerCity)

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

        alert.setTitle(R.string.loc_user_city)
            .setView(v)
            .setPositiveButton(R.string.action_save) { _: DialogInterface, _: Int ->
                val b = Bundle()
                b.putInt("cityID", cityID)
                navController.navigate(R.id.action_nav_manual_location_dialog_to_nav_location, b)
            }
            .setNegativeButton(R.string.action_cancel) { _: DialogInterface, _: Int ->
                val b = Bundle()
                b.putInt("cityID", -2)
                navController.navigate(R.id.action_nav_manual_location_dialog_to_nav_location, b)
            }
        return alert.create()
    }
}
