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

package planets.position.util

import android.app.Activity
import android.content.SharedPreferences
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import java.util.Locale

class PositionFormat(activity: Activity) {
    private val settings: SharedPreferences

    init {
        settings = getDefaultSharedPreferences(activity)
    }
    // Input value examples
    // RA: 5.458967510328336
    // DEC: 23.2260666095222
    // AZ: 298.3351453874998
    // ALT: 33.81055373204086
    /**
     * Format right ascension based on user preference.
     *
     * @param value Calculated right ascension value.
     * @return Formated string.
     */
    fun formatRA(value: Double): String {
        var output = ""
        val locale = Locale.getDefault()
        var ra: Double
        val ras: Double
        val rah: Int
        val ram: Int
        when (settings.getString("ra_format", "HH MM SS")) {
            "HH MM SS" -> {
                ra = value
                rah = ra.toInt()
                ra -= rah.toDouble()
                ra *= 60.0
                ram = ra.toInt()
                ra -= ram.toDouble()
                ras = ra * 60
                output = String.format(locale, "%dh %dm %.0fs", rah, ram, ras)
            }

            "HH MM.MM" -> {
                ra = value
                rah = ra.toInt()
                ra -= rah.toDouble()
                ra *= 60.0
                output = String.format(locale, "%dh %.2fm", rah, ra)
            }

            "HH.HHHHHH" -> {
                output = String.format(locale, "%.6f\u00b0", value)
            }
        }
        return output
    }

    /**
     * Format declination based on user preference.
     *
     * @param value Calculated declination value.
     * @return Formated string.
     */
    fun formatDec(value: Double): String {
        var output = ""
        val locale = Locale.getDefault()
        var dec: Double
        val decs: Double
        val decd: Int
        val decm: Int
        val decSign: Char
        when (settings.getString("dec_format", "DD MM SS")) {
            "DD MM SS" -> {
                dec = value
                if (dec < 0) {
                    decSign = '-'
                    dec *= -1.0
                } else {
                    decSign = '+'
                }
                decd = dec.toInt()
                dec -= decd.toDouble()
                dec *= 60.0
                decm = dec.toInt()
                dec -= decm.toDouble()
                decs = dec * 60
                output = String.format(
                    locale, "%c%d\u00b0 %d' %.0f\"", decSign,
                    decd, decm, decs
                )
            }

            "DD MM.MM" -> {
                dec = value
                if (dec < 0) {
                    decSign = '-'
                    dec *= -1.0
                } else {
                    decSign = '+'
                }
                decd = dec.toInt()
                dec -= decd.toDouble()
                dec *= 60.0
                decm = dec.toInt()
                dec -= decm.toDouble()
                dec *= 60.0
                output = String.format(
                    locale, "%c%d\u00b0 %.2f'", decSign, decd,
                    dec
                )
            }

            "DD.DDDDDD" -> {
                dec = value
                if (dec < 0) {
                    decSign = '-'
                    dec *= -1.0
                } else {
                    decSign = '+'
                }
                output = String.format(locale, "%c%.6f\u00b0", decSign, dec)
            }
        }
        return output
    }

    /**
     * Format azimuth based on user preference.
     *
     * @param value Calculated azimuth value.
     * @return Formated string.
     */
    fun formatAZ(value: Double): String {
        var output = ""
        var az: Double
        val azs: Double
        val azd: Int
        val azm: Int
        val locale = Locale.getDefault()
        when (settings.getString("az_format", "DDD MM SS")) {
            "DDD MM SS" -> {
                az = value
                azd = az.toInt()
                az -= azd.toDouble()
                az *= 60.0
                azm = az.toInt()
                az -= azm.toDouble()
                azs = az * 60
                output = String.format(locale, "%d\u00b0 %dm %.0fs", azd, azm, azs)
            }

            "DDD MM.MM" -> {
                az = value
                azd = az.toInt()
                az -= azd.toDouble()
                az *= 60.0
                output = String.format(locale, "%d\u00b0 %.2fm", azd, az)
            }

            "DDD.DDDDDD" -> output = String.format(locale, "%.6f\u00b0", value)
            "QDD.DQ" -> {
                val bearing: Double
                val q1: Char
                val q2: Char
                if (value > 90.0 && value < 270.0) {
                    q1 = 'S'
                    if (value <= 180.0) {
                        // Q2
                        bearing = 180.0 - value
                        q2 = 'E'
                    } else {
                        // Q3
                        bearing = value - 180.0
                        q2 = 'W'
                    }
                } else {
                    q1 = 'N'
                    if (value <= 90.0) {
                        // Q1
                        bearing = value
                        q2 = 'E'
                    } else {
                        // Q4
                        bearing = 360.0 - value
                        q2 = 'W'
                    }
                }
                output = String.format(locale, "%c %.1f\u00b0 %c", q1, bearing, q2)
            }
        }
        return output
    }

    /**
     * Format altitude based on user preference.
     *
     * @param value Calculated altitude value.
     * @return Formated string.
     */
    fun formatALT(value: Double): String {
        var output = ""
        val locale = Locale.getDefault()
        var alt: Double
        val alts: Double
        val altd: Int
        val altm: Int
        when (settings.getString("alt_format", "DD MM SS")) {
            "DD MM SS" -> {
                alt = value
                altd = alt.toInt()
                alt -= altd.toDouble()
                alt *= 60.0
                altm = alt.toInt()
                alt -= altm.toDouble()
                alts = alt * 60
                output = String.format(
                    locale, "%d\u00b0 %d' %.0f\"", altd, altm,
                    alts
                )
            }

            "DD MM.MM" -> {
                alt = value
                altd = alt.toInt()
                alt -= altd.toDouble()
                alt *= 60.0
                altm = alt.toInt()
                alt -= altm.toDouble()
                alt *= 60.0
                output = String.format(locale, "%d\u00b0 %.2f'", altd, alt)
            }

            "DD.DDDDDD" -> output = String.format(locale, "%.6f\u00b0", value)
        }
        return output
    }
}
