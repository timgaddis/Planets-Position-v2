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

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import planets.position.PlanetApplication
import planets.position.database.timezone.TimeZone
import planets.position.database.timezone.TimeZoneRepository
import planets.position.util.JDUTC
import planets.position.util.RiseSetTransit
import java.util.Calendar

class SkyPositionViewModel(
    application: Application,
    private val timeZoneRepository: TimeZoneRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var zone: LiveData<TimeZone>
    private var zoneName: String = ""
    private var g = DoubleArray(3)
    private val jdUTC: JDUTC = JDUTC()
    private var riseSetTransit: RiseSetTransit
    private var settings: SharedPreferences

    init {
        settings = PreferenceManager.getDefaultSharedPreferences(application)
        g[1] = settings.getFloat("latitude", (-91.0).toFloat()).toDouble()
        g[0] = settings.getFloat("longitude", 0f).toDouble()
        g[2] = settings.getFloat("altitude", 0f).toDouble()
        zoneName = settings.getString("zoneName", "").toString()

        zone = savedStateHandle.getLiveData("skyTime", 0L).switchMap { x ->
            timeZoneRepository.getZoneOffset(zoneName, x)
        }
        riseSetTransit = RiseSetTransit(g)
    }

    // c function prototypes
    private external fun planetPosData(
        d1: Double, d2: Double, p: Int, loc: DoubleArray
    ): DoubleArray?

    fun saveTime(time: Long) {
        savedStateHandle["skyTime"] = time
    }

    fun getZone(): LiveData<TimeZone> {
        return zone
    }

    fun setOffset(offset: Double) {
        savedStateHandle["offset"] = offset
    }

    fun setPlanet(p: Int) {
        savedStateHandle["skyPlanet"] = p
    }

    fun getPlanetData(): LiveData<PlanetData> {

        val off = savedStateHandle.getLiveData("offset", -13.0)
        val planet = savedStateHandle.getLiveData("skyPlanet", -1)

        val result = MediatorLiveData<PlanetData>()

        result.addSource(off) {
            result.value = computePlanet(off, planet)
        }
        result.addSource(planet) {
            result.value = computePlanet(off, planet)
        }

        return result
    }

    private fun computePlanet(offset: LiveData<Double>, planet: LiveData<Int>): PlanetData? {

        val p = planet.value
        val off = offset.value
        if (p != null && p < 0)
            return null
        if (off != null && off < -12.0)
            return null
        var pld: PlanetData? = null
        val tt = savedStateHandle["skyTime"] ?: 0L
        Log.d("PlanetPosition", "computePlanet off:${off} tt:${tt}")
        val cal = Calendar.getInstance()
        cal.clear()
        cal.timeInMillis = tt * 1000L
//        Log.d("PlanetsPosition", "computePlanet,cal1:${cal.time},${off!! * -60.0}")
        cal.add(Calendar.MINUTE, (off!! * -60.0).toInt())
//        Log.d("PlanetsPosition", "computePlanet,cal2:${cal.time}")
        val t = jdUTC.utcjd(
            cal[Calendar.MONTH] + 1,
            cal[Calendar.DAY_OF_MONTH], cal[Calendar.YEAR],
            cal[Calendar.HOUR_OF_DAY], cal[Calendar.MINUTE],
            cal[Calendar.SECOND].toDouble()
        )
        viewModelScope.launch {
            val pd = computeLocation(p!!, t!!)
            withContext(Dispatchers.Main) {
                pld = pd
            }
        }
//        Log.d("Planets Position", "computePlanet:${pld}")
        return pld
    }

    private suspend fun computeLocation(planetNum: Int, time: DoubleArray): PlanetData? {
//        Log.d("Planets Position", "in computeLocation:[${time[0]},${time[1]}]")
        var ra: Double
        var t: Double
        val d: Double = time[1]
        val rst = DoubleArray(3)

        val data: DoubleArray? = planetPosData(time[0], time[1], planetNum, g)
        if (data == null) {
            Log.e("Position error", "planetPosData error")
            return null
        }
        ra = data[0]
        // convert ra to hours
        ra /= 15

        t = riseSetTransit.getSet(d, planetNum)
        if (t < 0) {
            Log.e("Position error", "planetPosData set error")
            return null
        }
        rst[1] = t

        t = riseSetTransit.getRise(d, planetNum)
        if (t < 0) {
            Log.e("Position error", "planetPosData rise error")
            return null
        }
        rst[0] = t

        t = riseSetTransit.getTransit(d, planetNum)
        if (t < 0) {
            Log.e("Position error", "planetPosData transit error")
            return null
        }
        rst[2] = t

        return PlanetData(
            "",
            planetNum,
            if (data[4] > 0) 1 else -1,
            ra,
            data[1],
            data[3],
            data[4],
            data[2],
            data[5],
            rst[1],
            rst[0],
            rst[2],
            0
        )
    }

    companion object {
        init {
            System.loadLibrary("planets_swiss")
        }

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                // Create a SavedStateHandle for this ViewModel from extras
                val savedStateHandle = extras.createSavedStateHandle()

                return SkyPositionViewModel(
                    application,
                    (application as PlanetApplication).timeZoneRepository,
                    savedStateHandle
                ) as T
            }
        }
    }
}
