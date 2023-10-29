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

package planets.position.riseset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import planets.position.R
import planets.position.database.planet.Planet
import planets.position.databinding.FragmentRiseSetDataBinding
import planets.position.util.JDUTC
import planets.position.util.PositionFormat
import java.text.DateFormat
import java.util.Calendar
import java.util.Locale

class RiseSetDataFragment : Fragment() {

    private val riseSetDataViewModel: RiseSetDataViewModel by viewModels { RiseSetDataViewModel.Factory }
    private var _binding: FragmentRiseSetDataBinding? = null
    private val binding get() = _binding!!
    private lateinit var nameText: TextView
    private lateinit var dateText: TextView
    private lateinit var timeText: TextView
    private lateinit var azText: TextView
    private lateinit var altText: TextView
    private lateinit var raText: TextView
    private lateinit var decText: TextView
    private lateinit var distanceText: TextView
    private lateinit var magText: TextView
    private lateinit var setText: TextView
    private lateinit var rsText: TextView
    private lateinit var transitText: TextView
    private lateinit var mDateFormat: DateFormat
    private lateinit var mTimeFormat: DateFormat
    private lateinit var pf: PositionFormat
    private val jdUTC: JDUTC = JDUTC()
    private var lastUpdate: Long = 0
    private var offset = 0.0
    private var planetID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        planetID = requireArguments().getInt("planetID", 0)
        lastUpdate = requireArguments().getLong("lastUpdate", 0)
        offset = requireArguments().getDouble("offset", 0.0)
        mDateFormat = android.text.format.DateFormat
            .getDateFormat(requireActivity().applicationContext)
        mTimeFormat = android.text.format.DateFormat
            .getTimeFormat(requireActivity().applicationContext)
        pf = PositionFormat(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRiseSetDataBinding.inflate(inflater, container, false)
        val root: View = binding.root

        nameText = binding.dataNameText
        dateText = binding.dataDateText
        timeText = binding.dataTimeText
        azText = binding.dataAzText
        altText = binding.dataAltText
        raText = binding.dataRaText
        decText = binding.dataDecText
        distanceText = binding.dataDisText
        magText = binding.dataMagText
        setText = binding.dataSetTimeText
        transitText = binding.dataTransitTimeText
        rsText = binding.dataSetTime

        riseSetDataViewModel.getPlanet()?.observe(viewLifecycleOwner) {
            loadPlanet(it)
        }

        riseSetDataViewModel.savePlanetID(planetID)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadPlanet(p: Planet) {
        var time: Double
        val c = Calendar.getInstance()
        c.clear()
        c.timeInMillis = lastUpdate
        dateText.text = mDateFormat.format(c.time)
        timeText.text = mTimeFormat.format(c.time)
        nameText.text = p.cName
        raText.text = pf.formatRA(p.cRA)
        decText.text = pf.formatDec(p.cDec)
        azText.text = pf.formatAZ(p.cAz)
        val alt: Double = p.cAlt
        altText.text = pf.formatALT(alt)
        distanceText.text =
            if (p.cID == 1) String.format(Locale.getDefault(), "%.4f AU", p.cDistance)
            else
                String.format(Locale.getDefault(), "%.2f AU", p.cDistance)
        magText.text = String.format(
            Locale.getDefault(), "%.2f", p.cMagnitude
        )
        if (alt > 0) {
            time = p.cSetTime
            rsText.setText(R.string.data_set)
        } else {
            time = p.cRiseTime
            rsText.setText(R.string.data_rise)
        }
        c.clear()
        c.timeInMillis = jdUTC.jdmills(time, offset * 60.0)
        setText.text = String.format(
            "%s %s", mDateFormat.format(c.time), mTimeFormat.format(c.time)
        )
        time = p.cTransit
        c.clear()
        c.timeInMillis = jdUTC.jdmills(time, offset * 60.0)
        transitText.text = String.format(
            "%s %s", mDateFormat.format(c.time), mTimeFormat.format(c.time)
        )
    }
}
