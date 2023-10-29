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

package planets.position.skyposition

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import planets.position.R
import planets.position.databinding.FragmentLivePositionBinding
import planets.position.util.JDUTC
import planets.position.util.PositionFormat
import java.text.DateFormat
import java.util.Calendar
import java.util.Locale

class LivePositionFragment : Fragment() {

    private val livePositionViewModel: LivePositionViewModel by viewModels { LivePositionViewModel.Factory }
    private var _binding: FragmentLivePositionBinding? = null
    private val binding get() = _binding!!
    private lateinit var settings: SharedPreferences
    private lateinit var mDateFormat: DateFormat
    private lateinit var mTimeFormat: DateFormat
    private lateinit var planetNames: List<String>
    private lateinit var planetText: TextView
    private lateinit var dateText: TextView
    private lateinit var timeText: TextView
    private lateinit var azText: TextView
    private lateinit var altText: TextView
    private lateinit var raText: TextView
    private lateinit var decText: TextView
    private lateinit var distanceText: TextView
    private lateinit var magText: TextView
    private lateinit var riseText: TextView
    private lateinit var transitText: TextView
    private lateinit var belowText: TextView
    private lateinit var pf: PositionFormat
    private val jdUTC: JDUTC = JDUTC()
    private var planetNum = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDateFormat = android.text.format.DateFormat
            .getDateFormat(requireActivity().applicationContext)
        mTimeFormat = android.text.format.DateFormat
            .getTimeFormat(requireActivity().applicationContext)
        settings = PreferenceManager.getDefaultSharedPreferences(requireContext())
        planetNames = listOf<String>(*resources.getStringArray(R.array.planets_array))
        pf = PositionFormat(requireActivity())

        if (arguments?.containsKey("live_planet") == true) {
            planetNum = requireArguments().getInt("live_planet", 0)
            livePositionViewModel.setPlanet(planetNum)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLivePositionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        planetText = binding.liveNameText
        dateText = binding.liveDateText
        timeText = binding.liveTimeText
        azText = binding.liveAzText
        altText = binding.liveAltText
        raText = binding.liveRaText
        decText = binding.liveDecText
        distanceText = binding.liveDisText
        magText = binding.liveMagText
        riseText = binding.liveRiseTimeText
        transitText = binding.liveTransitTimeText
        belowText = binding.liveBelowText

        livePositionViewModel.getPlanetData().observe(viewLifecycleOwner) {
//            Log.d("PlanetsPosition", "getPlanetData observer:$it")
            if (it != null)
                updatePlanetData(it)
        }

        return root
    }

    private fun updatePlanetData(p: PlanetData) {
        val offset = p.name.toDouble()
        val utc: Calendar = Calendar.getInstance()
        utc.clear()

        planetText.text = planetNames[p.number]
        utc.timeInMillis = p.time
        dateText.text = mDateFormat.format(utc.time)
        timeText.text = mTimeFormat.format(utc.time)
        raText.text = pf.formatRA(p.ra)
        decText.text = pf.formatDec(p.dec)
        azText.text = pf.formatAZ(p.az)
        altText.text = pf.formatALT(p.alt)

        if (p.number == 1) distanceText.text =
            String.format(Locale.getDefault(), "%.4f AU", p.distance)
        else distanceText.text =
            String.format(Locale.getDefault(), "%.2f AU", p.distance)

        magText.text = String.format(Locale.getDefault(), "%.2f", p.magnitude)

        if (p.rise <= 0) {
            riseText.setText(R.string.data_rise)
            belowText.visibility = View.VISIBLE
            utc.timeInMillis = jdUTC.jdmills(p.riseTime, offset * 60.0)
        } else {
            riseText.setText(R.string.data_set)
            belowText.visibility = View.GONE
            utc.timeInMillis = jdUTC.jdmills(p.setTime, offset * 60.0)
        }
        riseText.text = String.format(
            "%s %s", mDateFormat.format(utc.time), mTimeFormat.format(utc.time)
        )

        utc.timeInMillis = jdUTC.jdmills(p.transit, offset * 60.0)
        transitText.text = String.format(
            "%s\n%s", mDateFormat.format(utc.time), mTimeFormat.format(utc.time)
        )
    }
}
