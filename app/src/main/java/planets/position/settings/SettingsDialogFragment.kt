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
