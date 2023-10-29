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

package planets.position.database.timezone

import androidx.lifecycle.LiveData

class TimeZoneRepository(private var timeZoneDAO: TimeZoneDAO) {

    fun getZoneOffset(zone: String, time: Long): LiveData<TimeZone> {
        return timeZoneDAO.getZoneOffset(zone, time)
    }

    fun getZone(zone: String, time: Long): TimeZone {
        return timeZoneDAO.getZone(zone, time)
    }

    fun getCounrty(country: String): LiveData<Country> {
        return timeZoneDAO.getCountry(country)
    }

    fun getStateList(cc: String): LiveData<List<State>> {
        return timeZoneDAO.getStateList(cc)
    }

    fun getCityList(state: String): LiveData<List<WorldCity>> {
        return timeZoneDAO.getCityList(state)
    }

    fun getCityData(id: Int): LiveData<WorldCity> {
        return timeZoneDAO.getCityData(id)
    }
}
