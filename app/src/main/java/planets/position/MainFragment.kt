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

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navigationHost =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navigationHost.navController
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val solarEclipse: Button = binding.buttonSEMain
        val lunarEclipse: Button = binding.buttonLEMain
        val lunarOccult: Button = binding.buttonLOMain
        val skyPosition: Button = binding.buttonSky
        val riseSet: Button = binding.buttonRiseSet

        skyPosition.setOnClickListener {
            navController.navigate(R.id.action_nav_main_to_nav_sky_position)
        }

        riseSet.setOnClickListener {
            navController.navigate(R.id.action_nav_main_to_nav_rise_set)
        }

        solarEclipse.setOnClickListener {
            navController.navigate(R.id.action_nav_main_to_nav_solar_eclipse)
        }

        lunarEclipse.setOnClickListener {
            navController.navigate(R.id.action_nav_main_to_nav_lunar_eclipse)
        }

        lunarOccult.setOnClickListener {
            navController.navigate(R.id.action_nav_main_to_nav_lunar_occult)
        }

        return root
    }
}
