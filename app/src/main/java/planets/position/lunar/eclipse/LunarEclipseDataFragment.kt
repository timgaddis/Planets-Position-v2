/*
 * Planet's Position
 * A program to calculate the position of the planets in the night sky
 * based on a given location on Earth.
 * Copyright (c) 2023 Tim Gaddis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package planets.position.lunar.eclipse

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
import planets.position.database.lunar.Lunar
import planets.position.databinding.FragmentLunarEclipseDataBinding
import planets.position.util.JDUTC
import planets.position.util.PositionFormat
import java.text.DateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

class LunarEclipseDataFragment : Fragment() {

    private val lunarEclipseDataViewModel: LunarEclipseDataViewModel by viewModels { LunarEclipseDataViewModel.Factory }
    private var _binding: FragmentLunarEclipseDataBinding? = null
    private val binding get() = _binding!!
    private lateinit var leDateText: TextView
    private lateinit var leTypeText: TextView
    private lateinit var leStartText: TextView
    private lateinit var leTStartText: TextView
    private lateinit var leLocalVisible: TextView
    private lateinit var lePStartText: TextView
    private lateinit var leMaxText: TextView
    private lateinit var leTEndText: TextView
    private lateinit var lePEndText: TextView
    private lateinit var leEndText: TextView
    private lateinit var leMoonRise: TextView
    private lateinit var leMoonSet: TextView
    private lateinit var leAzText: TextView
    private lateinit var leAltText: TextView
    private lateinit var lePMagText: TextView
    private lateinit var leUMag: TextView
    private lateinit var leUMagText: TextView
    private lateinit var leSarosText: TextView
    private lateinit var leSarosMText: TextView
    private lateinit var leLocalTime: TextView
    private lateinit var leLocalLayout: ConstraintLayout
    private lateinit var leMoonLayout: ConstraintLayout
    private lateinit var lePartialLayout: ConstraintLayout
    private lateinit var leTotalLayout: ConstraintLayout
    private lateinit var mDateFormat: DateFormat
    private lateinit var mTimeFormat: DateFormat
    private lateinit var navController: NavController
    private lateinit var pf: PositionFormat
    private val jdUTC: JDUTC = JDUTC()
    private var lunarID = 0
    private var mag = 0.0
    private var eclType = ""
    private var local = false
    private var eclStart = 0L
    private var eclEnd = 0L

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

        if (arguments?.containsKey("lunarID") == true) {
            lunarID = requireArguments().getInt("lunarID", 0)
            lunarEclipseDataViewModel.saveLunarEclipse(lunarID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLunarEclipseDataBinding.inflate(inflater, container, false)
        val root: View = binding.root

        leDateText = binding.leDate
        leTypeText = binding.leType
        leLocalTime = binding.leLocalTime
        leStartText = binding.leStartText
        lePStartText = binding.lePstartText
        leTStartText = binding.leTstartText
        leMaxText = binding.leMaxText
        leTEndText = binding.leTendText
        lePEndText = binding.lePendText
        leEndText = binding.leEndText
        leMoonRise = binding.leMoonriseText
        leMoonSet = binding.leMoonsetText
        leAzText = binding.leMoonAzText
        leAltText = binding.leMoonAltText
        lePMagText = binding.lePmagText
        leUMag = binding.leUmag
        leUMagText = binding.leUmagText
        leSarosText = binding.leSarosText
        leSarosMText = binding.leSarosmText
        leLocalVisible = binding.leNoVisible
        leMoonLayout = binding.leMoonLayout
        leLocalLayout = binding.leDataLayout
        lePartialLayout = binding.lePartialLayout
        leTotalLayout = binding.leTotalLayout

        setupMenu()

        lunarEclipseDataViewModel.getLunarEclipse()?.observe(viewLifecycleOwner) {
            it.let {
                if (it != null)
                    loadLunarEclipse(it)
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
                menuInflater.inflate(R.menu.calendar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.action_calendar) {
                    // add eclipse to calendar
                    val intent = Intent(Intent.ACTION_INSERT)
                    intent.data = CalendarContract.Events.CONTENT_URI
                    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, eclStart)
                    intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, eclEnd)
                    intent.putExtra(CalendarContract.Events.TITLE, "$eclType Lunar Eclipse")
                    if (local) {
                        val desc = "Magnitude: " + String.format(Locale.getDefault(), "%.2f", mag)
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

    private fun loadLunarEclipse(lunar: Lunar) {
        var total = false
        var partial: Boolean
        var temp: Double
        var offset = 0.0
        val gc: Calendar = GregorianCalendar()
        val planetColor =
            ContextCompat.getColor(requireActivity().applicationContext, R.color.planet_set_color)

        val max = jdUTC.jdmills(lunar.eclipseDate)
        gc.timeInMillis = max
        leDateText.text = mDateFormat.format(gc.time)

        val type = lunar.eclipseType
        partial = type == "Partial"
        eclType = type!!
        if (type == "Total") {
            total = true
            partial = true
        }
        leTypeText.text = type
        local = lunar.local > 0

        if (local)
            offset = lunar.offset * 60.0

        val moonRise: Double = lunar.moonRise
        if (moonRise > 0) {
            gc.timeInMillis = jdUTC.jdmills(moonRise, offset)
            leMoonRise.text = String.format(
                "%s\n%s", mDateFormat.format(gc.time),
                mTimeFormat.format(gc.time)
            )
        } else {
            leMoonRise.text = ""
        }
        val moonSet: Double = lunar.moonSet
        if (moonSet > 0) {
            gc.timeInMillis = jdUTC.jdmills(moonSet, offset)
            leMoonSet.text = String.format(
                "%s\n%s", mDateFormat.format(gc.time),
                mTimeFormat.format(gc.time)
            )
        } else {
            leMoonSet.text = ""
        }
        temp = lunar.penumbralBegin
        if (temp > 0) {
            eclStart = jdUTC.jdmills(temp, offset)
            gc.timeInMillis = eclStart
            leStartText.text = String.format(
                "%s\n%s", mDateFormat.format(gc.time),
                mTimeFormat.format(gc.time)
            )
            if (local) if (temp < moonRise || temp > moonSet) {
                leStartText.setTextColor(planetColor)
            }
        } else {
            leStartText.text = "\n"
        }
        temp = lunar.partialBegin
        if (temp > 0) {
            gc.timeInMillis = jdUTC.jdmills(temp, offset)
            lePStartText.text = String.format(
                "%s\n%s", mDateFormat.format(gc.time),
                mTimeFormat.format(gc.time)
            )
            if (local) if (temp < moonRise || temp > moonSet) {
                lePStartText.setTextColor(planetColor)
            }
        }
        temp = lunar.totalBegin
        if (temp > 0) {
            gc.timeInMillis = jdUTC.jdmills(temp, offset)
            leTStartText.text = String.format(
                "%s\n%s", mDateFormat.format(gc.time),
                mTimeFormat.format(gc.time)
            )
            if (local) if (temp < moonRise || temp > moonSet) {
                leTStartText.setTextColor(planetColor)
            }
        }
        temp = lunar.maxEclipse
        if (temp > 0) {
            gc.timeInMillis = jdUTC.jdmills(temp, offset)
            leMaxText.text = String.format(
                "%s\n%s", mDateFormat.format(gc.time),
                mTimeFormat.format(gc.time)
            )
            if (local) if (temp < moonRise || temp > moonSet) {
                leMaxText.setTextColor(planetColor)
            }
        } else {
            leMaxText.text = "\n"
        }
        temp = lunar.totalEnd
        if (temp > 0) {
            gc.timeInMillis = jdUTC.jdmills(temp, offset)
            leTEndText.text = String.format(
                "%s\n%s", mDateFormat.format(gc.time),
                mTimeFormat.format(gc.time)
            )
            if (local) if (temp < moonRise || temp > moonSet) {
                leTEndText.setTextColor(planetColor)
            }
        }
        temp = lunar.partialEnd
        if (temp > 0) {
            gc.timeInMillis = jdUTC.jdmills(temp, offset)
            lePEndText.text = String.format(
                "%s\n%s", mDateFormat.format(gc.time),
                mTimeFormat.format(gc.time)
            )
            if (local) if (temp < moonRise || temp > moonSet) {
                lePEndText.setTextColor(planetColor)
            }
        }
        temp = lunar.penumbralEnd
        if (temp > 0) {
            eclEnd = jdUTC.jdmills(temp, offset)
            gc.timeInMillis = eclEnd
            leEndText.text = String.format(
                "%s\n%s", mDateFormat.format(gc.time),
                mTimeFormat.format(gc.time)
            )
            if (local) if (temp < moonRise || temp > moonSet) {
                leEndText.setTextColor(planetColor)
            }
        } else {
            leEndText.text = "\n"
        }
        if (!partial) lePartialLayout.visibility = View.GONE
        if (!total) leTotalLayout.visibility = View.GONE
        if (local) {
            // local eclipse
            leLocalTime.setText(R.string.ecl_local)
            temp = lunar.moonAz
            if (temp > 0) {
                leAzText.text = pf.formatAZ(temp)
            } else {
                leAzText.text = ""
            }
            temp = lunar.moonAlt
            if (temp > 0) {
                leAltText.text = pf.formatALT(temp)
            } else {
                leAltText.text = ""
            }
            // penumbral magnitude
            temp = lunar.penumbralMag
            if (temp > 0) {
                mag = temp
                lePMagText.text = String.format(Locale.getDefault(), "%.2f", temp)
            } else {
                mag = 0.0
                lePMagText.text = ""
            }
            // umbral magnitude
            temp = lunar.umbralMag
            if (temp > 0) {
                mag = temp
                leUMagText.text = String.format(Locale.getDefault(), "%.2f", temp)
            } else {
                mag = 0.0
                leUMag.visibility = View.GONE
                leUMagText.visibility = View.GONE
            }
            leSarosText.text = lunar.sarosNum.toString()
            leSarosMText.text = lunar.sarosMemNum.toString()
        } else {
            // global eclipse
            leLocalTime.setText(R.string.ecl_universal)
            leLocalLayout.visibility = View.GONE
            leMoonLayout.visibility = View.GONE
            leLocalVisible.visibility = View.VISIBLE
        }
    }
}
