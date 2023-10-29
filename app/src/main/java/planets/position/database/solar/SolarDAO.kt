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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SolarDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(solar: Solar)

    @Query("DELETE FROM solar_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM solar_table ORDER BY globalBegin")
    fun getSolarEclipseList(): Flow<List<Solar>>

    @Query("SELECT * FROM solar_table WHERE id = :id")
    fun getSolarEclipseLive(id: Int): Flow<Solar>

    @Query("SELECT * FROM solar_table ORDER BY globalBegin LIMIT 1")
    fun getFirstEclipse(): Solar

    @Query("SELECT * FROM solar_table ORDER BY globalBegin DESC LIMIT 1")
    fun getLastEclipse(): Solar
}
