package planets.position

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import planets.position.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var settings: SharedPreferences
    private var latitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_main,
                R.id.nav_solar_eclipse,
//                R.id.nav_lunar_eclipse,
//                R.id.nav_lunar_occult,
                R.id.nav_rise_set,
                R.id.nav_sky_position,
                R.id.nav_location,
                R.id.nav_settings,
                R.id.nav_about
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        settings = PreferenceManager.getDefaultSharedPreferences(this)

        loadLocation()

        if (latitude < -90.0) {
//            navController.navigate(R.id.action_nav_main_to_nav_location_dialog)
            Toast.makeText(this, "main lat < -90.0", Toast.LENGTH_LONG).show()
            // sample data for Denver, CO
            with(settings.edit()) {
                putFloat("latitude", 39.707165F)
                putFloat("longitude", -104.862030F)
                putFloat("altitude", 1649.7F)
                putFloat("offset", -6.0F)
                putString("zoneName", "America/Denver")
                putLong("date", Calendar.getInstance().timeInMillis)
                putBoolean("newLocation", true)
                apply()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun loadLocation() {
        latitude = settings.getFloat("latitude", -91.0f).toDouble()
    }
}
