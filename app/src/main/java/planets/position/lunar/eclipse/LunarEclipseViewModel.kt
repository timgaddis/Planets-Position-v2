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

package planets.position.lunar.eclipse

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import planets.position.PlanetApplication
import planets.position.database.lunar.Lunar
import planets.position.database.lunar.LunarRepository
import planets.position.database.timezone.TimeZoneRepository
import planets.position.util.JDUTC
import planets.position.util.RiseSetTransit
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Calendar
import kotlin.coroutines.coroutineContext
import kotlin.math.abs

class LunarEclipseViewModel(
    application: Application,
    private val lunarRepository: LunarRepository,
    private val timeZoneRepository: TimeZoneRepository
) : ViewModel() {
    private var lunar: LiveData<List<Lunar>>
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
        riseSetTransit = RiseSetTransit(g)
        lunar = lunarRepository.getLunarEclipseList().asLiveData()
    }

    // c function prototypes
    private external suspend fun lunarDataLocal(
        d2: Double,
        loc: DoubleArray?,
        back: Int
    ): DoubleArray?

    private external suspend fun lunarDataGlobal(d2: Double, back: Int): DoubleArray?

    fun getLunarList(): LiveData<List<Lunar>> {
        return lunar
    }

    fun launchTask(x: Int, time: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            val lList: List<Lunar>
            if (x == 0) {
                // new date
                val c = Calendar.getInstance()
                c.clear()
                c.timeInMillis = time
                val offset = findOffset(time)
                // convert to utc
                c.add(Calendar.MINUTE, (offset * -60.0).toInt())
                val t = jdUTC.utcjd(
                    c[Calendar.MONTH] + 1,
                    c[Calendar.DAY_OF_MONTH], c[Calendar.YEAR],
                    c[Calendar.HOUR_OF_DAY], c[Calendar.MINUTE],
                    c[Calendar.SECOND].toDouble()
                )
                // compute eclipses
                lList = computeEclipses(t!![0], 0)
            } else if (x > 0) {
                // next eclipses
                val l = lunarRepository.getLastEclipse()
                lList = computeEclipses(l.penumbralEnd, 0)
            } else {
                // previous eclipses
                val l = lunarRepository.getFirstEclipse()
                lList = computeEclipses(l.penumbralBegin, 1)
            }
            withContext(Dispatchers.IO) {
                //save list to db
                lunarRepository.deleteAllLunarEclipse()
                for (l in lList) {
                    lunarRepository.insertLunarEclipse(l)
                }
            }
        }
    }

    private suspend fun computeEclipses(d: Double, back: Int): List<Lunar> {
        var startDate = d
        var data1: DoubleArray?
        var ecl: Int
        var eclType: String
        var moonrise: Double
        var moonset: Double
        val lunarList = mutableListOf<Lunar>()

        var data2: DoubleArray? = findLocalEclipse(startDate, back) ?: return mutableListOf()

        for (i in 1..10) {
            // check if canceled
            if (!coroutineContext.isActive) return mutableListOf()

            data1 = findGlobalEclipse(startDate, back) ?: return mutableListOf()

            // create type string use data1[0] (global type)
            ecl = data1[0].toInt()
            eclType = if (ecl and 4 == 4) { // LE_ECL_TOTAL
                "Total"
            } else if (ecl and 64 == 64) { // LE_ECL_PENUMBRAL
                "Penumbral"
            } else if (ecl and 16 == 16) { // LE_ECL_PARTIAL
                "Partial"
            } else {
                "Other"
            }

            if (abs(data2!![1] - data1[1]) <= 1.0) {
                // if local eclipse time is within one day of the global
                // time, then eclipse is visible locally

                moonset = riseSetTransit.getSet(data1[7], 1)
                moonrise = riseSetTransit.getRise(moonset - 1, 1)
                val off = findOffset(jdUTC.jdmills(data2[1]))

                val lunar = saveData(i, data1, data2, moonrise, moonset, eclType, off)
                lunarList.add(lunar)

                startDate = if (back == 0) data1[8] else data1[7]

                data2 = findLocalEclipse(startDate, back) ?: return mutableListOf()
            } else {
                // Global eclipse
                val lunar = saveData(i, data1, null, -1.0, -1.0, eclType, -1.0)
                lunarList.add(lunar)
                startDate = if (back == 0) data1[8] else data1[7]
            }
        }
        return lunarList
    }

    private fun findOffset(time: Long): Double {
        val c = Calendar.getInstance()
        c.clear()
        c.timeInMillis = time
        val tz = timeZoneRepository.getZone(zoneName, c.timeInMillis / 1000)
        val df = DecimalFormat("##.#")
        df.roundingMode = RoundingMode.DOWN
        return df.format(tz.gmt_offset / 3600.0).toDouble()
    }

    private suspend fun findLocalEclipse(d: Double, b: Int): DoubleArray? {
        val data: DoubleArray? = lunarDataLocal(d, g, b)
        if (data == null) {
            Log.e(
                "Local LunarEclipse error", "LunarEclipseTask - findLocalEclipse error"
            )
            return null
        }
        return data
    }

    private suspend fun findGlobalEclipse(d: Double, b: Int): DoubleArray? {
        val data: DoubleArray? = lunarDataGlobal(d, b)
        if (data == null) {
            Log.e(
                "Global LunarEclipse error", "LunarEclipseTask - findGlobalEclipse error"
            )
            return null
        }
        return data
    }

    private fun saveData(
        id: Int,
        data1: DoubleArray,
        data2: DoubleArray?,
        moonrise: Double,
        moonset: Double,
        type: String,
        offset: Double
    ): Lunar {
        return Lunar(
            id,
            if (data2 == null) -1 else data2[0].toInt(),
            data1[0].toInt(),
            if (data2 == null) 0 else 1,
            offset,
            if (data2 == null) -1.0 else data2[11],
            if (data2 == null) -1.0 else data2[12],
            if (data2 == null) -1.0 else data2[15],
            if (data2 == null) -1.0 else data2[17],
            moonrise,
            moonset,
            if (data2 == null) -1 else data2[20].toInt(),
            if (data2 == null) -1 else data2[21].toInt(),
            data1[1],
            data1[7],
            data1[8],
            data1[3],
            data1[4],
            data1[5],
            data1[6],
            data1[1],
            type
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

                return LunarEclipseViewModel(
                    application,
                    (application as PlanetApplication).lunarRepository,
                    application.timeZoneRepository
                ) as T
            }
        }
    }
}
