package planets.position.location

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import planets.position.R

class TimeZoneDialog : DialogFragment() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navigationHost =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navigationHost.navController
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, R.style.LocDialogTheme)
            builder.setTitle(R.string.loc_tz_title)
                .setItems(
                    R.array.time_zones
                ) { _, which ->
                    val tz: String = resources.getStringArray(R.array.time_zones)[which]
                    val b = Bundle()
                    b.putString("timezone", tz)
                    navController.navigate(
                        R.id.action_nav_timezone_dialog_to_nav_location, b
                    )
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
