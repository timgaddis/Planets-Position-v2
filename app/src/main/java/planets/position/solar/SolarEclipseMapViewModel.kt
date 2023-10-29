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

package planets.position.solar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SolarEclipseMapViewModel : ViewModel() {

    private var path = MutableLiveData<List<LatLng>>()

    companion object {
        // load c library
        init {
            System.loadLibrary("planets_swiss")
        }
    }

    // c function prototypes
    private external fun solarMapPos(d2: Double): DoubleArray?

    fun getSEPath(): MutableLiveData<List<LatLng>> {
        return path
    }

    fun computePath(start: Double, end: Double) {
        var data: DoubleArray
        val num = 80
        val interval = (end - start) / num
        var date = start
        val sePath = mutableListOf<LatLng>()
        viewModelScope.launch(Dispatchers.Default) {
            for (x in 0..num) {
                data = solarMapPos(date)!!
                sePath.add(LatLng(data[1], data[0]))
                date += interval
            }
            withContext(Dispatchers.Main) {
                path.value = sePath
            }
        }
    }
}
