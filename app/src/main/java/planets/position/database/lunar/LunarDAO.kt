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

package planets.position.database.lunar

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LunarDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lunar: Lunar)

    @Query("DELETE FROM lunar_table")
    suspend fun deleteAllEclipse()

    @Query("SELECT * FROM lunar_table WHERE id = :id")
    fun getLunarEclipse(id: Int): Flow<Lunar>

    @Query("SELECT * FROM lunar_table ORDER BY penumbralBegin")
    fun getLunarEclipseList(): Flow<List<Lunar>>

    @Query("SELECT * FROM lunar_table ORDER BY penumbralBegin LIMIT 1")
    fun getFirstEclipse(): Lunar

    @Query("SELECT * FROM lunar_table ORDER BY penumbralBegin DESC LIMIT 1")
    fun getLastEclipse(): Lunar

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(occult: Occult)

    @Query("DELETE FROM occult_table")
    fun deleteAllOccult()

    @Query("SELECT * FROM occult_table WHERE id = :id")
    fun getLunarOccult(id: Int): Flow<Occult>

    @Query("SELECT * FROM occult_table ORDER BY occultPlanet, globalBegin")
    fun getLunarOccultList(): Flow<List<Occult>>

    @Query("SELECT * FROM occult_table ORDER BY globalBegin LIMIT 1")
    fun getFirstOccult(): Occult

    @Query("SELECT * FROM occult_table ORDER BY globalBegin DESC LIMIT 1")
    fun getLastOccult(): Occult
}
