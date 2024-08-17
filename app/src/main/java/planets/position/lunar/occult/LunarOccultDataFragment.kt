/*
 * Planet's Position
 * A program to calculate the position of the planets in the night sky
 * based on a given location on Earth.
 * Copyright (c) 2023-2024 Tim Gaddis
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

package planets.position.lunar.occult

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
import planets.position.database.lunar.Occult
import planets.position.databinding.FragmentLunarOccultDataBinding
import planets.position.util.JDUTC
import planets.position.util.PositionFormat
import java.text.DateFormat
import java.util.Calendar
import java.util.GregorianCalendar

class LunarOccultDataFragment : Fragment() {

    private val lunarOccultDataViewModel: LunarOccultDataViewModel by viewModels { LunarOccultDataViewModel.Factory }
    private var _binding: FragmentLunarOccultDataBinding? = null
    private val binding get() = _binding!!
    private lateinit var loDateText: TextView
    private lateinit var loPlanetText: TextView
    private lateinit var loStartText: TextView
    private lateinit var loMaxText: TextView
    private lateinit var loEndText: TextView
    private lateinit var loMoonSAzText: TextView
    private lateinit var loMoonSAltText: TextView
    private lateinit var loMoonEAzText: TextView
    private lateinit var loLocalTime: TextView
    private lateinit var loMoonEAltText: TextView
    private lateinit var loMoonRiseText: TextView
    private lateinit var loMoonSetText: TextView
    private lateinit var loLocalVisible: TextView
    private lateinit var loLocalLayout: ConstraintLayout
    private lateinit var loMoonLayout: ConstraintLayout
    private lateinit var mDateFormat: DateFormat
    private lateinit var mTimeFormat: DateFormat
    private lateinit var navController: NavController
    private lateinit var pf: PositionFormat
    private val jdUTC: JDUTC = JDUTC()
    private var planetArray: List<String>? = null
    private var eclStart: Long = 0
    private var eclEnd: Long = 0
    private var local = 0
    private var planet = 0
    private var occultID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDateFormat = android.text.format.DateFormat
            .getDateFormat(requireActivity().applicationContext)
        mTimeFormat = android.text.format.DateFormat
            .getTimeFormat(requireActivity().applicationContext)
        pf = PositionFormat(requireActivity())
        planetArray = listOf<String>(*resources.getStringArray(R.array.planets_array))
        val navigationHost =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navigationHost.navController

        if (arguments?.containsKey("occultID") == true) {
            occultID = requireArguments().getInt("occultID", 0)
            lunarOccultDataViewModel.saveLunarOccult(occultID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLunarOccultDataBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupMenu()

        loDateText = binding.loDate
        loPlanetText = binding.loPlanet
        loLocalTime = binding.loLocalTime
        loStartText = binding.loStartText
        loMaxText = binding.loMaxText
        loEndText = binding.loEndText
        loMoonRiseText = binding.loMoonriseText
        loMoonSetText = binding.loMoonsetText
        loMoonSAzText = binding.loMoonsAzText
        loMoonSAltText = binding.loMoonsAltText
        loMoonEAzText = binding.loMooneAzText
        loMoonEAltText = binding.loMooneAltText
        loMoonLayout = binding.loMoonLayout
        loLocalVisible = binding.loNoVisible
        loLocalLayout = binding.loDataLayout

        lunarOccultDataViewModel.getLunarOccult()?.observe(viewLifecycleOwner) {
            it.let {
                if (it != null) {
                    loadLunarOccult(it)
                }
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
                    intent.putExtra(CalendarContract.Events.TITLE, "Lunar Occultation")
                    if (local > 0) {
                        intent.putExtra(
                            CalendarContract.Events.DESCRIPTION, "Lunar Occultation of "
                                    + planetArray?.get(planet)
                        )
                    } else {
                        intent.putExtra(
                            CalendarContract.Events.DESCRIPTION,
                            "This occultation is not visible locally."
                        )
                    }
                    startActivity(intent)
                    return true
                }
                return false
            }
        }, viewLifecycleOwner)
    }

    private fun loadLunarOccult(occult: Occult) {
        val moonRise: Double
        val moonSet: Double
        var temp: Double
        val gc: Calendar = GregorianCalendar()
        val offset: Double
        val planetColor =
            ContextCompat.getColor(requireActivity().applicationContext, R.color.planet_set_color)

        val max = jdUTC.jdmills(occult.occultDate)
        gc.timeInMillis = max
        loDateText.text = mDateFormat.format(gc.time)

        planet = occult.occultPlanet
        if (planet >= 0) loPlanetText.text = planetArray!![planet] else loPlanetText.text = ""

        local = occult.local
        if (local > 0) {
            // local occultation
            offset = occult.offset * 60.0

            loLocalTime.setText(R.string.ecl_local)
            moonRise = occult.moonRise
            if (moonRise > 0) {
                gc.timeInMillis = jdUTC.jdmills(moonRise, offset)
                loMoonRiseText.text = String.format(
                    "%s\n%s", mDateFormat.format(gc.time),
                    mTimeFormat.format(gc.time)
                )
            } else {
                loMoonRiseText.text = ""
            }
            moonSet = occult.moonSet
            if (moonSet > 0) {
                gc.timeInMillis = jdUTC.jdmills(moonSet, offset)
                loMoonSetText.text = String.format(
                    "%s\n%s", mDateFormat.format(gc.time),
                    mTimeFormat.format(gc.time)
                )
            } else {
                loMoonSetText.text = ""
            }
            temp = occult.localFirst
            if (temp > 0) {
                eclStart = jdUTC.jdmills(temp, offset)
                gc.timeInMillis = eclStart
                loStartText.text = String.format(
                    "%s\n%s", mDateFormat.format(gc.time), mTimeFormat.format(gc.time)
                )
                if (temp < moonRise || temp > moonSet) loStartText.setTextColor(planetColor)
            } else {
                loStartText.text = ""
            }
            temp = occult.localMax
            if (temp > 0) {
                gc.timeInMillis = jdUTC.jdmills(temp, offset)
                loMaxText.text = String.format(
                    "%s\n%s", mDateFormat.format(gc.time), mTimeFormat.format(gc.time)
                )
                if (temp < moonRise || temp > moonSet) loMaxText.setTextColor(planetColor)
            } else {
                loMaxText.text = ""
            }
            temp = occult.localFourth
            if (temp > 0) {
                eclEnd = jdUTC.jdmills(temp, offset)
                gc.timeInMillis = eclEnd
                loEndText.text = String.format(
                    "%s\n%s", mDateFormat.format(gc.time),
                    mTimeFormat.format(gc.time)
                )
                if (temp < moonRise || temp > moonSet) loEndText.setTextColor(planetColor)
            } else {
                loEndText.text = ""
            }
            temp = occult.moonAzStart
            loMoonSAzText.text = pf.formatAZ(temp)
            temp = occult.moonAltStart
            loMoonSAltText.text = pf.formatALT(temp)
            temp = occult.moonAzEnd
            loMoonEAzText.text = pf.formatAZ(temp)
            temp = occult.moonAltEnd
            loMoonEAltText.text = pf.formatALT(temp)
        } else {
            // global occultation
            loLocalTime.setText(R.string.ecl_universal)
            temp = occult.globalBegin
            if (temp > 0) {
                eclStart = jdUTC.jdmills(temp)
                gc.timeInMillis = eclStart
                loStartText.text = String.format(
                    "%s\n%s", mDateFormat.format(gc.time),
                    mTimeFormat.format(gc.time)
                )
            } else {
                loStartText.text = ""
            }
            temp = occult.globalMax
            if (temp > 0) {
                gc.timeInMillis = jdUTC.jdmills(temp)
                loMaxText.text = String.format(
                    "%s\n%s", mDateFormat.format(gc.time),
                    mTimeFormat.format(gc.time)
                )
            } else {
                loMaxText.text = ""
            }
            temp = occult.globalEnd
            if (temp > 0) {
                eclEnd = jdUTC.jdmills(temp)
                gc.timeInMillis = eclEnd
                loEndText.text = String.format(
                    "%s\n%s", mDateFormat.format(gc.time),
                    mTimeFormat.format(gc.time)
                )
            } else {
                loEndText.text = ""
            }
            loMoonLayout.visibility = View.GONE
            loLocalLayout.visibility = View.GONE
            loLocalVisible.visibility = View.VISIBLE
        }
    }
}
