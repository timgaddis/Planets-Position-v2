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
