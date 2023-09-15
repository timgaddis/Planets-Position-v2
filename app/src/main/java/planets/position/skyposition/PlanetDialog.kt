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
