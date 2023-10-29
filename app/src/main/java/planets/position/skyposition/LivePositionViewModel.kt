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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import planets.position.PlanetApplication
import planets.position.database.timezone.TimeZoneRepository
import planets.position.util.JDUTC
import planets.position.util.RiseSetTransit
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Calendar

class LivePositionViewModel(
    application: Application,
    private val timeZoneRepository: TimeZoneRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var zoneName: String = ""
    private var g = DoubleArray(3)
    private var planetData: MutableLiveData<PlanetData> = MutableLiveData()
    private val jdUTC: JDUTC = JDUTC()
    private var riseSetTransit: RiseSetTransit
    private var settings: SharedPreferences

    init {
        settings = PreferenceManager.getDefaultSharedPreferences(application)
        g[1] = settings.getFloat("latitude", (-91.0).toFloat()).toDouble()
        g[0] = settings.getFloat("longitude", 0f).toDouble()
        g[2] = settings.getFloat("altitude", 0f).toDouble()
        zoneName = settings.getString("zoneName", "").toString()
        riseSetTransit = RiseSetTransit(g)
    }

    // c function prototypes
    private external fun planetLiveData(d2: Double, p: Int, loc: DoubleArray): DoubleArray?

    fun setPlanet(p: Int) {
        savedStateHandle["skyPlanet"] = p
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                val c = Calendar.getInstance()
                val tz = timeZoneRepository.getZone(zoneName, c.timeInMillis / 1000)
                val df = DecimalFormat("##.#")
                df.roundingMode = RoundingMode.DOWN
                val offset = df.format(tz.gmt_offset / 3600.0).toDouble()
                c.add(Calendar.MINUTE, (offset * -60.0).toInt())
                val t = jdUTC.utcjd(
                    c[Calendar.MONTH] + 1,
                    c[Calendar.DAY_OF_MONTH], c[Calendar.YEAR],
                    c[Calendar.HOUR_OF_DAY], c[Calendar.MINUTE],
                    c[Calendar.SECOND].toDouble()
                )
                // subtract gmt offset
                c.add(Calendar.MINUTE, (offset * -60.0 * -1.0).toInt())
                val pd = computeLocation(p, t!!, c.timeInMillis, offset)
                withContext(Dispatchers.Main) {
                    planetData.value = pd!!
                }
                delay(1000)
            }
        }
    }

    fun getPlanetData(): MutableLiveData<PlanetData> {
        return planetData
    }

    private suspend fun computeLocation(
        planetNum: Int,
        time: DoubleArray,
        cTime: Long,
        offset: Double
    ): PlanetData? {
        Log.d("Planets Position", "in live computeLocation:[${time[0]},${time[1]}]")
        var ra: Double
        var t: Double
        val d: Double = time[1]
        val rst = DoubleArray(3)

        val data: DoubleArray? = planetLiveData(d, planetNum, g)
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
            offset.toString(),
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
            cTime
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
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val savedStateHandle = extras.createSavedStateHandle()

                return LivePositionViewModel(
                    application,
                    (application as PlanetApplication).timeZoneRepository,
                    savedStateHandle
                ) as T
            }
        }
    }
}
