package planets.position.skyposition

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import planets.position.R
import planets.position.databinding.FragmentSkyPositionBinding
import planets.position.util.JDUTC
import planets.position.util.PositionFormat
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

class SkyPositionFragment : Fragment() {

    private val skyPositionViewModel: SkyPositionViewModel by viewModels { SkyPositionViewModel.Factory }
    private var _binding: FragmentSkyPositionBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var settings: SharedPreferences
    private lateinit var mDateFormat: DateFormat
    private lateinit var mTimeFormat: DateFormat
    private lateinit var planetNames: List<String>
    private lateinit var timeButton: Button
    private lateinit var dateButton: Button
    private lateinit var planetButton: Button
    private lateinit var azText: TextView
    private lateinit var altText: TextView
    private lateinit var raText: TextView
    private lateinit var decText: TextView
    private lateinit var distanceText: TextView
    private lateinit var magText: TextView
    private lateinit var riseText: TextView
    private lateinit var setText: TextView
    private lateinit var transitText: TextView
    private lateinit var belowText: TextView
    private lateinit var pf: PositionFormat
    private val jdUTC: JDUTC = JDUTC()
    private var planetNum: Int = 0
    private var offset = 0.0
    private var skyYear = 0
    private var skyMonth = 0
    private var skyDay = 0
    private var skyHour = 0
    private var skyMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDateFormat = android.text.format.DateFormat
            .getDateFormat(requireActivity().applicationContext)
        mTimeFormat = android.text.format.DateFormat
            .getTimeFormat(requireActivity().applicationContext)
        settings = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val navigationHost =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navigationHost.navController
        planetNames = listOf<String>(*resources.getStringArray(R.array.planets_array))
        pf = PositionFormat(requireActivity())
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
        _binding = FragmentSkyPositionBinding.inflate(inflater, container, false)
        val root: View = binding.root
//        Log.d("PlanetsPosition", "fragment onCreateView")
        timeButton = binding.timeButton
        dateButton = binding.dateButton
        planetButton = binding.planetButton
        azText = binding.posAzText
        altText = binding.posAltText
        raText = binding.posRaText
        decText = binding.posDecText
        distanceText = binding.posDisText
        magText = binding.posMagText
        riseText = binding.posRiseTimeText
        setText = binding.posSetTimeText
        transitText = binding.posTransitTimeText
        belowText = binding.posBelowText

        setupMenu()

        timeButton.setOnClickListener {
            val c = Calendar.getInstance()
            val timePicker =
                MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(c[Calendar.HOUR_OF_DAY])
                    .setMinute(c[Calendar.MINUTE])
                    .setTitleText("Select time")
                    .setTheme(R.style.MaterialTimeTheme)
                    .build()
            timePicker.show(parentFragmentManager, timePicker.toString())
            timePicker.addOnPositiveButtonClickListener {
                skyHour = timePicker.hour
                skyMinute = timePicker.minute
                saveSettings()
                updateButtons()
                val gc = loadDateTime(false)
                skyPositionViewModel.saveTime(gc.timeInMillis / 1000L)
//                Log.d(
//                    "PlanetsPosition",
//                    "onTimeSet hour:${timePicker.hour} minute:${timePicker.minute}"
//                )
            }
        }

        dateButton.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .setTheme(R.style.MaterialCalendarTheme)
                    .build()
            datePicker.show(parentFragmentManager, datePicker.toString())
            datePicker.addOnPositiveButtonClickListener {
                val c: Calendar = Calendar.getInstance()
                c.clear()
                c.timeInMillis = datePicker.selection!!.toLong()
                skyYear = c[Calendar.YEAR]
                skyMonth = c[Calendar.MONTH] + 1
                skyDay = c[Calendar.DAY_OF_MONTH]
                saveSettings()
                updateButtons()
                val gc = loadDateTime(false)
                skyPositionViewModel.saveTime(gc.timeInMillis / 1000L)
                Log.d("Planets Position", "onDateSet1:${it}")
//                Log.d("Planets Position", "onDateSet2:${datePicker.selection!!.toLong()}")
//                Log.d("PlanetsPosition", "onDateSet year:${c[Calendar.YEAR]}")
            }
        }

        planetButton.setOnClickListener {
            navController.navigate(R.id.action_nav_sky_position_to_nav_planet_dialog)
        }

        skyPositionViewModel.getZone().observe(viewLifecycleOwner) {
            it.let {
                if (it != null) {
                    val df = DecimalFormat("##.#")
                    df.roundingMode = RoundingMode.DOWN
                    offset = df.format(it.gmt_offset / 3600.0).toDouble()
                    Log.d("PlanetsPosition", "getZone observer:off${offset}")
                    saveSettings()
                    skyPositionViewModel.setOffset(offset)
                }
            }
        }

        skyPositionViewModel.getPlanetData().observe(viewLifecycleOwner) {
            it.let {
                if (it != null) {
                    updatePlanetData(it)
                }
            }
        }

        loadSettings()

        if (planetNum < 0) {
            // set the current date, time
            val cal = loadDateTime(true)
            skyYear = cal.get(Calendar.YEAR)
            skyMonth = cal[Calendar.MONTH]
            skyDay = cal[Calendar.DAY_OF_MONTH]
            skyHour = cal[Calendar.HOUR_OF_DAY]
            skyMinute = cal[Calendar.MINUTE]
            planetNum = 0
            saveSettings()
            updateButtons()
        } else {
            updateButtons()
        }

        val gc = loadDateTime(false)
        skyPositionViewModel.saveTime(gc.timeInMillis / 1000L)
        skyPositionViewModel.setPlanet(planetNum)

//        if (g[1] < -90) {
//            navController.navigate(R.id.nav_location_dialog)
//        } else {
//            val gc = loadDateTime(false)
//            skyPositionViewModel.saveTime(gc.timeInMillis / 1000L)
//            updateButtons()
//            skyPositionViewModel.setPlanet(planetNum)
//        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments?.containsKey("planet") == true) {
            // planet list
            planetNum = requireArguments().getInt("planet", 0)
            Log.d("PlanetsPosition", "onViewCreated planet:${planetNum}")
            saveSettings()
            updateButtons()
            skyPositionViewModel.setPlanet(planetNum)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.sky_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.action_live_position) {
                    val bundle = Bundle()
                    bundle.putInt("live_planet", planetNum)
//                    navController.navigate(
//                        R.id.action_nav_sky_position_to_nav_live_position,
//                        bundle
//                    )
                    return true
                }
                return false
            }
        }, viewLifecycleOwner)
    }

    private fun loadSettings() {
        skyYear = settings.getInt("sky_year", 0)
        skyMonth = settings.getInt("sky_month", 0)
        skyDay = settings.getInt("sky_day", 0)
        skyHour = settings.getInt("sky_hour", 0)
        skyMinute = settings.getInt("sky_minute", 0)
        planetNum = settings.getInt("sky_planet", -1)
        offset = settings.getFloat("sky_offset", 0f).toDouble()
    }

    private fun saveSettings() {
        with(settings.edit()) {
            putInt("sky_year", skyYear)
            putInt("sky_month", skyMonth)
            putInt("sky_day", skyDay)
            putInt("sky_hour", skyHour)
            putInt("sky_minute", skyMinute)
            putInt("sky_planet", planetNum)
            putFloat("sky_offset", offset.toFloat())
            apply()
        }
    }

    private fun loadDateTime(now: Boolean): Calendar {
        return if (now) Calendar.getInstance() else {
            GregorianCalendar(
                settings.getInt("sky_year", 0),
                settings.getInt("sky_month", 0),
                settings.getInt("sky_day", 0),
                settings.getInt("sky_hour", 0),
                settings.getInt("sky_minute", 0)
            )
        }
    }

    // updates the date and time on the Buttons
    private fun updateButtons() {
        val gc = loadDateTime(false)
        Log.d("PlanetsPosition", "in updateButtons:${gc.timeInMillis}")
        dateButton.text = mDateFormat.format(gc.time)
        timeButton.text = mTimeFormat.format(gc.time)
        planetButton.text = planetNames[planetNum]
    }

    private fun updatePlanetData(p: PlanetData) {
//        Log.d("PlanetsPosition", "in updatePlanetData:${offset}")
        val utc: Calendar = Calendar.getInstance()
        utc.clear()

        raText.text = pf.formatRA(p.ra)
        decText.text = pf.formatDec(p.dec)
        azText.text = pf.formatAZ(p.az)
        altText.text = pf.formatALT(p.alt)

        if (p.number == 1) distanceText.text =
            String.format(Locale.getDefault(), "%.4f AU", p.distance)
        else distanceText.text =
            String.format(Locale.getDefault(), "%.2f AU", p.distance)

        magText.text = String.format(Locale.getDefault(), "%.2f", p.magnitude)

        utc.timeInMillis = jdUTC.jdmills(p.setTime, offset * 60.0)
        setText.text = String.format(
            "%s\n%s", mDateFormat.format(utc.time), mTimeFormat.format(utc.time)
        )

        utc.timeInMillis = jdUTC.jdmills(p.riseTime, offset * 60.0)
        riseText.text = String.format(
            "%s\n%s", mDateFormat.format(utc.time), mTimeFormat.format(utc.time)
        )

        utc.timeInMillis = jdUTC.jdmills(p.transit, offset * 60.0)
        transitText.text = String.format(
            "%s\n%s", mDateFormat.format(utc.time), mTimeFormat.format(utc.time)
        )

        if (p.rise <= 0) {
            // below horizon
            belowText.visibility = View.VISIBLE
        } else {
            // above horizon
            belowText.visibility = View.GONE
        }
    }
}
