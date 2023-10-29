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

package planets.position.lunar.occult

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
import planets.position.database.lunar.LunarRepository
import planets.position.database.lunar.Occult
import planets.position.database.timezone.TimeZoneRepository
import planets.position.util.JDUTC
import planets.position.util.RiseSetTransit
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Calendar
import kotlin.coroutines.coroutineContext
import kotlin.math.abs

class LunarOccultViewModel(
    application: Application,
    private val lunarRepository: LunarRepository,
    private val timeZoneRepository: TimeZoneRepository
) : ViewModel() {
    private var occult: LiveData<List<Occult>>
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
        occult = lunarRepository.getLunarOccultList().asLiveData()
    }

    // c function prototypes
    private external fun lunarOccultLocal(
        d2: Double,
        loc: DoubleArray?,
        planet: Int,
        back: Int
    ): DoubleArray?

    private external fun lunarOccultGlobal(d2: Double, planet: Int, back: Int): DoubleArray?

    fun getOccultList(): LiveData<List<Occult>> {
        return occult
    }

    fun launchTask(x: Int, time: Long, p: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            val oList: List<Occult>
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
                // compute occults
                oList = computeOccults(t!![0], 0, p)
            } else if (x > 0) {
                // next occults
                val o = lunarRepository.getLastOccult()
                oList = computeOccults(o.globalEnd, 0, p)
            } else {
                // previous occults
                val o = lunarRepository.getFirstOccult()
                oList = computeOccults(o.globalBegin, 1, p)
            }
            withContext(Dispatchers.IO) {
                //save list to db
                lunarRepository.deleteAllLunarOccult()
                for (o in oList) {
                    lunarRepository.insertLunarOccult(o)
                }
            }
        }
    }

    private suspend fun computeOccults(d: Double, back: Int, p: Int): List<Occult> {
        var startDate = d
        var data1: DoubleArray?
        var data2: DoubleArray?
        var moonrise: Double
        var moonset: Double
        val occultList = mutableListOf<Occult>()

        if (p > 1) {
            // compute occults for the given planet
            // Mercury=2 -> Pluto=9
            data2 = findLocalOccult(startDate, p, back) ?: return mutableListOf()

            for (i in 0..9) {
                // check if canceled
                if (!coroutineContext.isActive) return mutableListOf()

                data1 = findGlobalOccult(startDate, p, back) ?: return mutableListOf()

                if (abs(data2!![1] - data1[1]) <= 1.0) {
                    // if local eclipse time is within one day of the global
                    // time, then eclipse is visible locally
                    moonset = riseSetTransit.getSet(data1[3], 1)
                    moonrise = riseSetTransit.getRise(moonset - 1, 1)
                    val off = findOffset(jdUTC.jdmills(data2[1]))

                    val occult = saveData(i, data1, data2, moonrise, moonset, p, off)
                    occultList.add(occult)

                    startDate = if (back == 0) data1[1] + 2.0 else data1[1] - 2.0

                    data2 = findLocalOccult(startDate, p, back) ?: return mutableListOf()
                } else {
                    // Global Occult
                    val occult = saveData(i, data1, null, -1.0, -1.0, p, -1.0)
                    occultList.add(occult)
                    startDate = if (back == 0) data1[1] + 2.0 else data1[1] - 2.0
                }
            }
        } else {
            // compute occults for all planets
            for (i in 2..9) {
                // check if canceled
                if (!coroutineContext.isActive) return mutableListOf()

                data2 = findLocalOccult(startDate, i, back) ?: return mutableListOf()
                data1 = findGlobalOccult(startDate, i, back) ?: return mutableListOf()

                if (abs(data2[1] - data1[1]) <= 1.0) {
                    // if local occultation time is within one day of the
                    // global time, then occultation is visible locally
                    moonset = riseSetTransit.getSet(data1[3], 1)
                    moonrise = riseSetTransit.getRise(moonset - 1, 1)
                    val off = findOffset(jdUTC.jdmills(data2[1]))
                    val occult = saveData(i, data1, data2, moonrise, moonset, i, off)
                    occultList.add(occult)
                } else {
                    // Global Occultation
                    val occult = saveData(i, data1, null, -1.0, -1.0, i, -1.0)
                    occultList.add(occult)
                }
            }
        }
        return occultList
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

    private fun findLocalOccult(d: Double, p: Int, b: Int): DoubleArray? {
        val data: DoubleArray? = lunarOccultLocal(d, g, p, b)
        if (data == null) {
            Log.e(
                "Local LunarEclipse error", "LunarEclipseTask - findLocalEclipse error"
            )
            return null
        }
        return data
    }

    private fun findGlobalOccult(d: Double, p: Int, b: Int): DoubleArray? {
        val data: DoubleArray? = lunarOccultGlobal(d, p, b)
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
        planet: Int,
        offset: Double
    ): Occult {
        return Occult(
            id,
            if (data2 == null) -1 else data2[0].toInt(),
            data1[0].toInt(),
            if (data2 == null) 0 else 1,
            offset,
            if (data2 == null) -1.0 else data2[1],
            if (data2 == null) -1.0 else data2[2],
            if (data2 == null) -1.0 else data2[3],
            if (data2 == null) -1.0 else data2[4],
            if (data2 == null) -1.0 else data2[5],
            if (data2 == null) -1.0 else data2[11],
            if (data2 == null) -1.0 else data2[12],
            if (data2 == null) -1.0 else data2[13],
            if (data2 == null) -1.0 else data2[14],
            moonrise,
            moonset,
            data1[1],
            data1[3],
            data1[4],
            data1[5],
            data1[6],
            data1[7],
            data1[8],
            if (data2 == null) data1[1] else data2[1],
            planet
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

                return LunarOccultViewModel(
                    application,
                    (application as PlanetApplication).lunarRepository,
                    application.timeZoneRepository
                ) as T
            }
        }
    }
}
