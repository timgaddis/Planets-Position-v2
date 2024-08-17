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

package planets.position.database.timezone

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface TimeZoneDAO {
    @Query("SELECT * FROM time_zone WHERE zone_name = :zone AND time_start <= :time ORDER BY time_start DESC LIMIT 1")
    fun getZoneOffset(zone: String, time: Long): LiveData<TimeZone>

    @Query("SELECT * FROM time_zone WHERE zone_name = :zone AND time_start <= :time ORDER BY time_start DESC LIMIT 1")
    fun getZone(zone: String, time: Long): TimeZone

    @Query("SELECT * FROM country WHERE country_name = :country")
    fun getCountry(country: String): LiveData<Country>

    @Query("SELECT * FROM states WHERE country = :cc ORDER BY state")
    fun getStateList(cc: String): LiveData<List<State>>

    @Query("SELECT * FROM worldcities WHERE state = :state ORDER BY city")
    fun getCityList(state: String): LiveData<List<WorldCity>>

    @Query("SELECT * FROM worldcities WHERE _id = :id")
    fun getCityData(id: Int): LiveData<WorldCity>
}
