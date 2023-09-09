package planets.position

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import planets.position.databinding.FragmentMainBinding

class MainFragment : Fragment() {

//    private val sharedViewModel: PreferenceViewModel by activityViewModels()
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navigationHost =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navigationHost.navController
//        Log.d("PlanetsPosition", "Main onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        Log.d("PlanetsPosition", "Main onCreateView")
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val solarEclipse: Button = binding.buttonSEMain
//        val lunarEclipse: Button = binding.buttonLEMain
//        val lunarOccult: Button = binding.buttonLOMain
        val skyPosition: Button = binding.buttonSky
        val riseSet: Button = binding.buttonRiseSet
//        val jupiter: Button = binding.buttonJupiter
//        val saturn: Button = binding.buttonSaturn

        skyPosition.setOnClickListener {
//            navController.navigate(R.id.action_nav_main_to_nav_sky_position)
        }

        riseSet.setOnClickListener {
//            navController.navigate(R.id.action_nav_main_to_nav_rise_set)
        }

//        sharedViewModel.getLocation().observe(viewLifecycleOwner) {
//            if (it.latitude < -90.0) {
//                Log.d("PlanetsPosition", "Main onCreate < -91.0")
//                Toast.makeText(context, "main lat < -90.0", Toast.LENGTH_LONG).show()
//            } else {
//                Toast.makeText(context, "main lat:${it.latitude} ", Toast.LENGTH_LONG).show()
//            }
//        }

        return root
    }
}
