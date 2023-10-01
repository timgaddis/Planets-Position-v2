package planets.position.solar

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import planets.position.R
import planets.position.database.solar.Solar
import planets.position.databinding.FragmentSolarEclipseDataBinding
import planets.position.util.JDUTC
import planets.position.util.PositionFormat
import java.text.DateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

class SolarEclipseDataFragment : Fragment() {

    private val solarEclipseDataViewModel: SolarEclipseDataViewModel by viewModels { SolarEclipseDataViewModel.Factory }
    private var _binding: FragmentSolarEclipseDataBinding? = null
    private val binding get() = _binding!!
    private lateinit var seDateText: TextView
    private lateinit var seTypeText: TextView
    private lateinit var seStartText: TextView
    private lateinit var seTStartText: TextView
    private lateinit var seMaxText: TextView
    private lateinit var seTEndText: TextView
    private lateinit var seEndText: TextView
    private lateinit var seAzText: TextView
    private lateinit var seAltText: TextView
    private lateinit var seLocalTypeText: TextView
    private lateinit var seCoverText: TextView
    private lateinit var seMagText: TextView
    private lateinit var seSarosText: TextView
    private lateinit var seSarosMText: TextView
    private lateinit var seSunRiseText: TextView
    private lateinit var seSunSetText: TextView
    private lateinit var seLocalTime: TextView
    private lateinit var seLocalVisible: TextView
    private lateinit var seLocalLayout: ConstraintLayout
    private lateinit var seSunriseLayout: ConstraintLayout
    private lateinit var seTotalLayout: ConstraintLayout
    private lateinit var mDateFormat: DateFormat
    private lateinit var mTimeFormat: DateFormat
    private lateinit var navController: NavController
    private lateinit var pf: PositionFormat
    private val jdUTC: JDUTC = JDUTC()
    private var solarID = 0
    private var latitude = 0.0
    private var longitude: Double = 0.0
    private var eclType: String? = null
    private var eclLocalType: String? = null
    private var local = 0
    private var eclDate: Long = 0
    private var eclStart: Long = 0
    private var eclEnd: Long = 0
    private var centerBegin = 0.0
    private var centerEnd: Double = 0.0
    private var cover: Double = 0.0
    private var mag: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDateFormat = android.text.format.DateFormat
            .getDateFormat(requireActivity().applicationContext)
        mTimeFormat = android.text.format.DateFormat
            .getTimeFormat(requireActivity().applicationContext)
        pf = PositionFormat(requireActivity())
        val navigationHost =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navigationHost.navController

        if (arguments?.containsKey("solarID") == true) {
            solarID = requireArguments().getInt("solarID", 0)
            latitude = requireArguments().getDouble("latitude", 0.0)
            longitude = requireArguments().getDouble("longitude", 0.0)
            solarEclipseDataViewModel.saveSolarEclipse(solarID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSolarEclipseDataBinding.inflate(inflater, container, false)
        val root: View = binding.root

        seDateText = binding.seDate
        seTypeText = binding.seType
        seStartText = binding.seStartText
        seTStartText = binding.seTstartText
        seMaxText = binding.seMaxText
        seTEndText = binding.seTendText
        seEndText = binding.seEndText
        seAzText = binding.seSunAzText
        seAltText = binding.seSunAltText
        seLocalTypeText = binding.seLocal
        seCoverText = binding.seCoverText
        seMagText = binding.seMagText
        seSarosText = binding.seSarosText
        seSarosMText = binding.seSarosmText
        seSunRiseText = binding.seSunriseText
        seSunSetText = binding.seSunsetText
        seLocalTime = binding.seLocalTime
        seLocalVisible = binding.seNoVisible
        seLocalLayout = binding.seDataLayout
        seSunriseLayout = binding.seSunLayout
        seTotalLayout = binding.seTotalLayout

        setupMenu()

        solarEclipseDataViewModel.getSolarEclipse()?.observe(viewLifecycleOwner) {
            it.let {
                if (it != null) {
                    loadSolarEclipse(it)
                }
            }
        }

        return root
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.calendar_menu, menu)
                menuInflater.inflate(R.menu.map_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.action_map) {
                    val args = Bundle()
                    args.putDouble("latitude", latitude)
                    args.putDouble("longitude", longitude)
                    args.putDouble("start", centerBegin)
                    args.putDouble("end", centerEnd)
                    args.putLong("date", eclDate)
                    args.putString("type", eclType)
                    navController.navigate(R.id.action_nav_solar_eclipse_data_to_nav_solar_eclipse_map, args)
                    return true
                } else if (menuItem.itemId == R.id.action_calendar) {
                    // add eclipse to calendar
                    val intent = Intent(Intent.ACTION_INSERT)
                    intent.data = CalendarContract.Events.CONTENT_URI
                    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, eclStart)
                    intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, eclEnd)
                    intent.putExtra(CalendarContract.Events.TITLE, "$eclType Solar Eclipse")
                    if (local > 0) {
                        var desc = "Local Type: $eclLocalType"
                        desc += "\nSun Coverage: ${
                            String.format(
                                Locale.getDefault(), "%3.0f%%", cover * 100
                            )
                        }".trimIndent()
                        desc += "\nMagnitude: ${
                            String.format(
                                Locale.getDefault(), "%.2f", mag
                            )
                        }".trimIndent()
                        intent.putExtra(CalendarContract.Events.DESCRIPTION, desc)
                    } else {
                        intent.putExtra(
                            CalendarContract.Events.DESCRIPTION,
                            "This eclipse is not visible locally."
                        )
                    }
                    startActivity(intent)
                    return true
                }
                return false
            }
        }, viewLifecycleOwner)
    }

    private fun loadSolarEclipse(solar: Solar) {
        val lTotal: Boolean
        val gTotal: Boolean
        val sunrise: Double
        val sunset: Double
        var temp: Double
        val gc: Calendar = GregorianCalendar()
        val offset = solar.offset * 60.0
        val planetColor =
            ContextCompat.getColor(requireActivity().applicationContext, R.color.planet_set_color)

        val max = jdUTC.jdmills(solar.eclipseDate)
        gc.timeInMillis = max
        eclDate = gc.timeInMillis
        seDateText.text = mDateFormat.format(gc.time)

        val type = solar.eclipseType
        if (type!!.contains("|")) {
            gTotal =
                type.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0] == "Total"
            lTotal =
                type.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1] == "Total"
            eclType = type.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            eclLocalType =
                type.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            seTypeText.text = eclType
            seLocalTypeText.text = String.format(
                "%s %s", resources.getString(R.string.ecl_local_type),
                eclLocalType
            )
        } else {
            gTotal = type == "Total"
            lTotal = false
            eclType = type
            eclLocalType = ""
            seTypeText.text = type
        }

        centerBegin = solar.globalCenterBegin
        centerEnd = solar.globalCenterEnd
        // partial eclipse path start and end
        if (centerBegin == 0.0 || centerEnd == 0.0) {
            centerBegin = solar.globalBegin
            centerEnd = solar.globalEnd
        }

        local = solar.local
        if (local > 0) {
            // local eclipse
            seLocalTime.setText(R.string.ecl_local)
            if (!lTotal) {
                seTotalLayout.visibility = View.GONE
            }
            sunrise = solar.sunRise
            if (sunrise > 0) {
                gc.timeInMillis = jdUTC.jdmills(sunrise, offset)
                seSunRiseText.text = mTimeFormat.format(gc.time)
            } else {
                seSunRiseText.text = ""
            }
            sunset = solar.sunSet
            if (sunset > 0) {
                gc.timeInMillis = jdUTC.jdmills(sunset, offset)
                seSunSetText.text = mTimeFormat.format(gc.time)
            } else {
                seSunSetText.text = ""
            }
            temp = solar.localFirst
            if (temp > 0) {
                eclStart = jdUTC.jdmills(temp, offset)
                gc.timeInMillis = eclStart
                seStartText.text = mTimeFormat.format(gc.time)
                if (temp < sunrise || temp > sunset) seStartText.setTextColor(planetColor)
            } else {
                seStartText.text = ""
            }
            temp = solar.localSecond
            if (temp > 0 && lTotal) {
                gc.timeInMillis = jdUTC.jdmills(temp, offset)
                seTStartText.text = mTimeFormat.format(gc.time)
                if (temp < sunrise || temp > sunset) seTStartText.setTextColor(planetColor)
            } else {
                seTStartText.text = ""
            }
            temp = solar.localMax
            if (temp > 0) {
                gc.timeInMillis = jdUTC.jdmills(temp, offset)
                seMaxText.text = mTimeFormat.format(gc.time)
                if (temp < sunrise || temp > sunset) seMaxText.setTextColor(planetColor)
            } else {
                seMaxText.text = ""
            }
            temp = solar.localThird
            if (temp > 0 && lTotal) {
                gc.timeInMillis = jdUTC.jdmills(temp, offset)
                seTEndText.text = mTimeFormat.format(gc.time)
                if (temp < sunrise || temp > sunset) seTEndText.setTextColor(planetColor)
            } else {
                seTEndText.text = ""
            }
            temp = solar.localFourth
            if (temp > 0) {
                eclEnd = jdUTC.jdmills(temp, offset)
                gc.timeInMillis = eclEnd
                seEndText.text = mTimeFormat.format(gc.time)
                if (temp < sunrise || temp > sunset) seEndText.setTextColor(planetColor)
            } else {
                seEndText.text = ""
            }
            temp = solar.sunAz
//            Log.d("PlanetsPosition", "sunAZ:${temp}")
            if (temp > 0) {
                seAzText.text = pf.formatAZ(temp)
            } else {
                seAzText.text = ""
            }
            temp = solar.sunAlt
//            Log.d("PlanetsPosition", "sunALT:${temp}")
            if (temp > 0) {
                seAltText.text = pf.formatALT(temp)
            } else {
                seAltText.text = ""
            }
            temp = solar.fractionCovered
            if (temp > 0) {
                cover = temp
                seCoverText.text =
                    String.format(Locale.getDefault(), "%3.0f%%", temp * 100)
            } else {
                cover = 0.0
                seCoverText.text = ""
            }
            temp = solar.localMag
            if (temp > 0) {
                mag = temp
                seMagText.text = String.format(Locale.getDefault(), "%.2f", temp)
            } else {
                mag = 0.0
                seMagText.text = ""
            }
            seSarosText.text = solar.sarosNum.toString()
            seSarosMText.text = solar.sarosMemNum.toString()
        } else {
            // global eclipse
            seLocalTime.setText(R.string.ecl_universal)
            if (!gTotal) {
                seTotalLayout.visibility = View.GONE
            }
            temp = solar.globalBegin
            if (temp > 0) {
                eclStart = jdUTC.jdmills(temp)
                gc.timeInMillis = eclStart
                seStartText.text = mTimeFormat.format(gc.time)
            } else {
                seStartText.text = ""
            }
            temp = solar.globalTotalBegin
            if (temp > 0 && gTotal) {
                gc.timeInMillis = jdUTC.jdmills(temp)
                seTStartText.text = mTimeFormat.format(gc.time)
            } else {
                seTStartText.text = ""
            }
            temp = solar.globalMax
            if (temp > 0) {
                gc.timeInMillis = jdUTC.jdmills(temp)
                seMaxText.text = mTimeFormat.format(gc.time)
            } else {
                seMaxText.text = ""
            }
            temp = solar.globalTotalEnd
            if (temp > 0 && gTotal) {
                gc.timeInMillis = jdUTC.jdmills(temp)
                seTEndText.text = mTimeFormat.format(gc.time)
            } else {
                seTEndText.text = ""
            }
            temp = solar.globalEnd
            if (temp > 0) {
                eclEnd = jdUTC.jdmills(temp)
                gc.timeInMillis = eclEnd
                seEndText.text = mTimeFormat.format(gc.time)
            } else {
                seEndText.text = ""
            }
            seLocalTypeText.visibility = View.GONE
            seLocalLayout.visibility = View.GONE
            seSunriseLayout.visibility = View.GONE
            seLocalVisible.visibility = View.VISIBLE
        }
    }
}
