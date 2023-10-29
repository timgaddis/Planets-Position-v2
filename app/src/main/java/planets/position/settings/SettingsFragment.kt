package planets.position.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import planets.position.R
import planets.position.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var viewModel: SettingsViewModel
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var settings: SharedPreferences
    private lateinit var navController: NavController
    private lateinit var raStrings: List<String>
    private lateinit var decStrings: List<String>
    private lateinit var azStrings: List<String>
    private lateinit var altStrings: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        raStrings = listOf<String>(*resources.getStringArray(R.array.pref_ra_titles))
        decStrings = listOf<String>(*resources.getStringArray(R.array.pref_dec_titles))
        azStrings = listOf<String>(*resources.getStringArray(R.array.pref_az_titles))
        altStrings = listOf<String>(*resources.getStringArray(R.array.pref_alt_titles))
        settings = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val navigationHost =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navigationHost.navController

        viewModel.setRAIndex(settings.getInt("raIndex", 0))
        viewModel.setDecIndex(settings.getInt("decIndex", 0))
        viewModel.setAzIndex(settings.getInt("azIndex", 0))
        viewModel.setAltIndex(settings.getInt("altIndex", 0))
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navController.popBackStack(R.id.nav_main, false)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            callback
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val raText = binding.textRAFormat
        val decText = binding.textDecFormat
        val azText = binding.textAzFormat
        val altText = binding.textAltFormat

        raText.setOnClickListener {
            val b = Bundle()
            b.putInt("settingID", 0)
            b.putInt("arrayID", R.array.pref_ra_titles)
            b.putInt("textID", R.string.pref_title_ra_format)
            navController.navigate(R.id.action_nav_settings_to_nav_settings_dialog, b)
        }
        viewModel.getRAIndex().observe(viewLifecycleOwner) {
            it.let {
                raText.text = raStrings[it]
            }
        }

        decText.setOnClickListener {
            val b = Bundle()
            b.putInt("settingID", 1)
            b.putInt("arrayID", R.array.pref_dec_titles)
            b.putInt("textID", R.string.pref_title_dec_format)
            navController.navigate(R.id.action_nav_settings_to_nav_settings_dialog, b)
        }
        viewModel.getDecIndex().observe(viewLifecycleOwner) {
            it.let {
                decText.text = decStrings[it]
            }
        }

        azText.setOnClickListener {
            val b = Bundle()
            b.putInt("settingID", 2)
            b.putInt("arrayID", R.array.pref_az_titles)
            b.putInt("textID", R.string.pref_title_az_format)
            navController.navigate(R.id.action_nav_settings_to_nav_settings_dialog, b)
        }
        viewModel.getAzIndex().observe(viewLifecycleOwner) {
            it.let {
                azText.text = azStrings[it]
            }
        }

        altText.setOnClickListener {
            val b = Bundle()
            b.putInt("settingID", 3)
            b.putInt("arrayID", R.array.pref_alt_titles)
            b.putInt("textID", R.string.pref_title_alt_format)
            navController.navigate(R.id.action_nav_settings_to_nav_settings_dialog, b)
        }
        viewModel.getAltIndex().observe(viewLifecycleOwner) {
            it.let {
                altText.text = altStrings[it]
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments?.containsKey("settingID") == true) {
            val id = requireArguments().getInt("stringID", 0)
            val s = requireArguments().getString("settingString")
            when (requireArguments().getInt("settingID", 0)) {
                0 -> {
                    // ra
                    with(settings.edit()) {
                        this?.putString("ra_format", s)
                        this?.putInt("raIndex", id)
                        this?.apply()
                    }
                    viewModel.setRAIndex(id)
                }

                1 -> {
                    // dec
                    with(settings.edit()) {
                        this?.putString("dec_format", s)
                        this?.putInt("decIndex", id)
                        this?.apply()
                    }
                    viewModel.setDecIndex(id)
                }

                2 -> {
                    // az
                    with(settings.edit()) {
                        this?.putString("az_format", s)
                        this?.putInt("azIndex", id)
                        this?.apply()
                    }
                    viewModel.setAzIndex(id)
                }

                3 -> {
                    // alt
                    with(settings.edit()) {
                        this?.putString("alt_format", s)
                        this?.putInt("altIndex", id)
                        this?.apply()
                    }
                    viewModel.setAltIndex(id)
                }
            }
        }
    }
}
