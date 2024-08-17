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

package planets.position.riseset

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import planets.position.PlanetApplication
import planets.position.R
import planets.position.database.planet.Planet
import planets.position.database.planet.PlanetRepository
import planets.position.database.timezone.TimeZone
import planets.position.database.timezone.TimeZoneRepository
import planets.position.util.JDUTC
import planets.position.util.RiseSetTransit
import java.util.Calendar

class RiseSetViewModel(
    application: Application,
    private val planetRepository: PlanetRepository,
    private val timeZoneRepository: TimeZoneRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var planets: LiveData<List<Planet>>? = null
    private var zone: LiveData<TimeZone>
    private var zoneName: String = ""
    private var offset = 0.0
    private var g = DoubleArray(3)
    private val jdUTC: JDUTC = JDUTC()
    private var riseSetTransit: RiseSetTransit
    private var settings: SharedPreferences
    private var planetNames: List<String>
    private val images = intArrayOf(
        R.drawable.ic_planet_sun,
        R.drawable.ic_planet_moon, R.drawable.ic_planet_mercury,
        R.drawable.ic_planet_venus, R.drawable.ic_planet_mars,
        R.drawable.ic_planet_jupiter, R.drawable.ic_planet_saturn,
        R.drawable.ic_planet_uranus, R.drawable.ic_planet_neptune,
        R.drawable.ic_planet_pluto
    )

    // c function prototypes
    private external fun planetUpData(
        d1: Double, d2: Double, p: Int, loc: DoubleArray
    ): DoubleArray?

    init {
        settings = PreferenceManager.getDefaultSharedPreferences(application)
        g[1] = settings.getFloat("latitude", (-91.0).toFloat()).toDouble()
        g[0] = settings.getFloat("longitude", 0f).toDouble()
        g[2] = settings.getFloat("altitude", 0f).toDouble()
        zoneName = settings.getString("zoneName", "").toString()
        planets = savedStateHandle.getLiveData("planetView", 1).switchMap { x ->
            planetRepository.getPlanetsList(x).asLiveData()
        }
        zone = savedStateHandle.getLiveData("riseTime", 0L).switchMap { x ->
            timeZoneRepository.getZoneOffset(zoneName, x)
        }
        riseSetTransit = RiseSetTransit(g)
        planetNames = application.resources.getStringArray(R.array.planets_array).toList()
    }

    fun saveTime(time: Long) {
        savedStateHandle["riseTime"] = time
    }

    fun getZone(): LiveData<TimeZone> {
        return zone
    }

    fun saveView(i: Int) {
        savedStateHandle["planetView"] = i
    }

    fun setOffset(offset: Double) {
        savedStateHandle["offset"] = offset
        val t = savedStateHandle["riseTime"] ?: 0L
        val cal = Calendar.getInstance()
        cal.clear()
        cal.timeInMillis = t
        cal.add(Calendar.MINUTE, (offset * -60.0).toInt())

        val tt = jdUTC.utcjd(
            cal[Calendar.MONTH] + 1,
            cal[Calendar.DAY_OF_MONTH], cal[Calendar.YEAR],
            cal[Calendar.HOUR_OF_DAY], cal[Calendar.MINUTE],
            cal[Calendar.SECOND].toDouble()
        )
        viewModelScope.launch {
            // compute list
            val pList = computePlanets(tt!!)
            withContext(Dispatchers.IO) {
                //save list to db
                insertAllPlanets(pList)
            }
        }
    }

    fun getPlanets(): LiveData<List<Planet>>? {
        return planets
    }

    suspend fun insert(planet: Planet) {
        planetRepository.insert(planet)
    }

    private suspend fun insertAllPlanets(planetList: List<Planet>) {
        planetRepository.deleteAllPlanets()
        for (p in planetList) {
            planetRepository.insert(p)
        }
    }

    private suspend fun computePlanets(time: DoubleArray): List<Planet> {
        val planetList = mutableListOf<Planet>()
        val c: Calendar = Calendar.getInstance()
        var data: DoubleArray?
        val rst = DoubleArray(3)
        var t: Double
        var ra: Double
        for (i in 0..9) {
            data = planetUpData(time[0], time[1], i, g)
            if (data == null) {
                Log.e(
                    "Position error",
                    "PlanetCompute - computePlanets error"
                )
                break
            }
            t = riseSetTransit.getSet(time[1], i)
            if (t < 0) {
                Log.e("Position error", "computePlanets set error")
                break
            }
            rst[1] = t

            t = riseSetTransit.getRise(time[1], i)
            if (t < 0) {
                Log.e("Position error", "computePlanets rise error")
                break
            }
            rst[0] = t

            t = riseSetTransit.getTransit(time[1], i)
            if (t < 0) {
                Log.e("Position error", "computePlanets transit error")
                break
            }
            rst[2] = t

            ra = data[0]
            // convert ra to hours
            ra /= 15

            c.clear()
            c.timeInMillis = if (data[4] > 0)
                jdUTC.jdmills(rst[1], offset * 60.0)
            else
                jdUTC.jdmills(rst[0], offset * 60.0)

            val planet = Planet(
                i,
                planetNames[i],
                images[i],
                if (data[4] > 0) 1 else -1,
                ra,
                data[1],
                data[3],
                data[4],
                data[2],
                data[5],
                rst[1],
                rst[0],
                rst[2]
            )
            planetList.add(planet)
        }
        return planetList
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
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val savedStateHandle = extras.createSavedStateHandle()

                return RiseSetViewModel(
                    application,
                    (application as PlanetApplication).planetRepository,
                    application.timeZoneRepository,
                    savedStateHandle
                ) as T
            }
        }
    }
}
