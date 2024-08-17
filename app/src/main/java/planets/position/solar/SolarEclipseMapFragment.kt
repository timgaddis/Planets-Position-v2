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

package planets.position.solar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import planets.position.databinding.FragmentSolarEclipseMapBinding
import java.text.DateFormat
import java.util.GregorianCalendar

class SolarEclipseMapFragment : Fragment() {

    private var _binding: FragmentSolarEclipseMapBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SolarEclipseMapViewModel by viewModels()
    private lateinit var mDateFormat: DateFormat
    private lateinit var overlayTop: TextView
    private lateinit var overlayBottom: TextView
    private var latitude = 0.0
    private var longitude: Double = 0.0
    private var start: Double = 0.0
    private var end: Double = 0.0
    private var eclDate: Long = 0
    private var eclType: String = ""

    private val callback = OnMapReadyCallback { googleMap ->
        val gc = GregorianCalendar()
        gc.timeInMillis = eclDate

        viewModel.getSEPath().observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val path = PolylineOptions()
                for (p in it) {
                    path.add(p)
                }
                googleMap.addPolyline(path)
            }
        }

        viewModel.computePath(start, end)

        overlayTop.text = mDateFormat.format(gc.time)
        overlayBottom.text = String.format("%s Eclipse", eclType)

        val loc = LatLng(latitude, longitude)
        val options = MarkerOptions()
        options.position(loc)
        googleMap.addMarker(options)
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(loc))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDateFormat = android.text.format.DateFormat
            .getDateFormat(requireActivity().applicationContext)

        if (arguments?.containsKey("start") == true) {
            latitude = requireArguments().getDouble("latitude", 0.0)
            longitude = requireArguments().getDouble("longitude", 0.0)
            start = requireArguments().getDouble("start", 0.0)
            end = requireArguments().getDouble("end", 0.0)
            eclDate = requireArguments().getLong("date", 0)
            eclType = requireArguments().getString("type", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSolarEclipseMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        overlayTop = binding.overTop
        overlayBottom = binding.overBottom

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(binding.map.id) as SupportMapFragment
        mapFragment.getMapAsync(callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
