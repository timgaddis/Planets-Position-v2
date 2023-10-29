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

package planets.position.database.solar

import kotlinx.coroutines.flow.Flow

class SolarRepository(private val solarDAO: SolarDAO) {

    fun getSolarEclipseLive(id: Int): Flow<Solar> {
        return solarDAO.getSolarEclipseLive(id)
    }

    fun getFirstEclipse(): Solar {
        return solarDAO.getFirstEclipse()
    }

    fun getLastEclipse(): Solar {
        return solarDAO.getLastEclipse()
    }

    fun getSolarEclipseList(): Flow<List<Solar>> {
        return solarDAO.getSolarEclipseList()
    }

    suspend fun insertSolarEclipse(solar: Solar) {
        solarDAO.insert(solar)
    }

    suspend fun deleteAllSolarEclipse() {
        solarDAO.deleteAll()
    }
}
