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

package planets.position.database.lunar

import kotlinx.coroutines.flow.Flow

class LunarRepository(private val lunarDAO: LunarDAO) {

    fun getLunarEclipse(id: Int): Flow<Lunar> {
        return lunarDAO.getLunarEclipse(id)
    }

    fun getLunarEclipseList(): Flow<List<Lunar>> {
        return lunarDAO.getLunarEclipseList()
    }

    fun getFirstEclipse(): Lunar {
        return lunarDAO.getFirstEclipse()
    }

    fun getLastEclipse(): Lunar {
        return lunarDAO.getLastEclipse()
    }

    suspend fun insertLunarEclipse(lunar: Lunar) {
        lunarDAO.insert(lunar)
    }

    suspend fun deleteAllLunarEclipse() {
        lunarDAO.deleteAllEclipse()
    }

    fun getLunarOccult(id: Int): Flow<Occult> {
        return lunarDAO.getLunarOccult(id)
    }

    fun getLunarOccultList(): Flow<List<Occult>> {
        return lunarDAO.getLunarOccultList()
    }

    fun getFirstOccult(): Occult {
        return lunarDAO.getFirstOccult()
    }

    fun getLastOccult(): Occult {
        return lunarDAO.getLastOccult()
    }

    fun insertLunarOccult(occult: Occult) {
        lunarDAO.insert(occult)
    }

    fun deleteAllLunarOccult() {
        lunarDAO.deleteAllOccult()
    }
}
