package planets.position.lunar.eclipse

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import planets.position.R
import planets.position.database.lunar.Lunar
import planets.position.databinding.FragmentLunarEclipseBinding
import java.util.Calendar
import java.util.TimeZone

class LunarEclipseFragment : Fragment() {

    private val lunarEclipseViewModel: LunarEclipseViewModel by viewModels { LunarEclipseViewModel.Factory }
    private var _binding: FragmentLunarEclipseBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var settings: SharedPreferences
    private lateinit var textLunar: TextView
    private lateinit var progressLunar: ProgressBar
    private lateinit var lunarRecyclerView: RecyclerView
    private val g = DoubleArray(3)
    private var offset = 0.0
    private var lastUpdate = 0L
    private var firstRun = false
    private var newLoc = false
    private var zoneName: String = ""

    private fun adapterOnClick(lunar: Lunar) {
        val bundle = Bundle()
        bundle.putInt("lunarID", lunar.id)
        navController.navigate(R.id.action_nav_lunar_eclipse_to_nav_lunar_eclipse_data, bundle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val navigationHost =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navigationHost.navController
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLunarEclipseBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupMenu()
        loadSettings()

        textLunar = binding.lunarText
        progressLunar = binding.lunarProgress

        val lunarAdapter = LunarEclipseListAdapter { lunar -> adapterOnClick(lunar) }
        lunarRecyclerView = binding.lunarList
        lunarRecyclerView.adapter = lunarAdapter

        lunarEclipseViewModel.getLunarList().observe(viewLifecycleOwner) {
            it.let {
                firstRun = false
                saveSettings()
                lunarAdapter.submitList(it)
                if (it.size == 10) {
                    lunarRecyclerView.visibility = View.VISIBLE
                    textLunar.visibility = View.GONE
                    progressLunar.visibility = View.GONE
                }
            }
        }

        if (g[1] < -90) {
            navController.navigate(R.id.nav_location_dialog)
        } else {
            if (firstRun) {
                lastUpdate = Calendar.getInstance().timeInMillis
                saveSettings()
                launchTask(lastUpdate, 0)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.eclipse_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.action_previous -> {
                        launchTask(lastUpdate, -1)
                        return true
                    }

                    R.id.action_next -> {
                        launchTask(lastUpdate, 1)
                        return true
                    }

                    R.id.action_calendar -> {
                        // material date picker
                        val datePicker =
                            MaterialDatePicker.Builder.datePicker()
                                .setTitleText("Select date")
                                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                                .setTheme(R.style.MaterialCalendarTheme)
                                .build()
                        datePicker.show(parentFragmentManager, datePicker.toString())
                        datePicker.addOnPositiveButtonClickListener {
                            val selectedUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            selectedUtc.timeInMillis = it
                            val selectedLocal = Calendar.getInstance()
                            selectedLocal.clear()
                            selectedLocal.set(
                                selectedUtc.get(Calendar.YEAR),
                                selectedUtc.get(Calendar.MONTH),
                                selectedUtc.get(Calendar.DAY_OF_MONTH),
                                0,
                                0,
                                0
                            )
                            lastUpdate = selectedLocal.timeInMillis
                            saveSettings()
                            launchTask(lastUpdate, 0)
                        }
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner)
    }

    private fun loadSettings() {
        // read settings from Shared Preferences
        g[1] = settings.getFloat("latitude", (-91.0).toFloat()).toDouble()
        g[0] = settings.getFloat("longitude", 0f).toDouble()
        g[2] = settings.getFloat("altitude", 0f).toDouble()
        offset = settings.getFloat("offset", 0f).toDouble()
        zoneName = settings.getString("zoneName", "").toString()
        newLoc = settings.getBoolean("newLocation", true)
        lastUpdate = settings.getLong("leLastUpdate", 0)
        firstRun = settings.getBoolean("leFirstRun", true)
    }

    private fun saveSettings() {
        with(settings.edit()) {
            putLong("leLastUpdate", lastUpdate)
            putBoolean("leFirstRun", firstRun)
            apply()
        }
    }

    private fun launchTask(time: Long, back: Int) {
        lunarRecyclerView.visibility = View.GONE
        textLunar.visibility = View.VISIBLE
        progressLunar.visibility = View.VISIBLE
        lunarEclipseViewModel.launchTask(back, time)
    }
}
