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

package planets.position.util

import java.util.Calendar

class JDUTC {

    // load c library
    companion object {
        init {
            System.loadLibrary("planets_swiss")
        }
    }

    // c function prototypes
    private external fun utc2jd(
        m: Int, d: Int, y: Int, hr: Int, min: Int,
        sec: Double
    ): DoubleArray?

    private external fun jd2utc(jdate: Double): String

    /**
     * Calls the utc2jd JNI c function. Converts from an utc date to a jullian
     * date.
     *
     * @param m   month
     * @param d   day
     * @param y   year
     * @param hr  hour
     * @param min minute
     * @param sec second
     * @return array of 2 jullian dates, [ut1,tt]
     */
    fun utcjd(m: Int, d: Int, y: Int, hr: Int, min: Int, sec: Double): DoubleArray? {
        return utc2jd(m, d, y, hr, min, sec)
    }

    /**
     * Returns the given Julian date in milliseconds in local time.
     *
     * @param jdate  The given date in Jullian format.
     * @param offset The UTC offset in minutes.
     * @return Long containing the milliseconds.
     */
    fun jdmills(jdate: Double, offset: Double): Long {
        val utc = Calendar.getInstance()
        val dateArr: Array<String> =
            jd2utc(jdate).split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        utc[dateArr[1].toInt(), dateArr[2].toInt() - 1, dateArr[3].toInt(), dateArr[4].toInt()] =
            dateArr[5].toInt()
        utc[Calendar.MILLISECOND] = (dateArr[6].toDouble() * 1000).toInt()
        // convert utc to local time
        utc.add(Calendar.MINUTE, offset.toInt())
        return utc.timeInMillis
    }

    /**
     * Returns the given Jullian date in milliseconds in UTC time.
     *
     * @param jdate The given date in Jullian format.
     * @return Long containing the milliseconds.
     */
    fun jdmills(jdate: Double): Long {
        val utc = Calendar.getInstance()
        val dateArr: Array<String> =
            jd2utc(jdate).split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        utc[dateArr[1].toInt(), dateArr[2].toInt() - 1, dateArr[3].toInt(), dateArr[4].toInt()] =
            dateArr[5].toInt()
        utc[Calendar.MILLISECOND] = (dateArr[6].toDouble() * 1000).toInt()
        return utc.timeInMillis
    }
}
