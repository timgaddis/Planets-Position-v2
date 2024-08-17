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

package planets.position.database.planet

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanetDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(planet: Planet)

    @Query("DELETE FROM planets_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM planets_table WHERE cID = :id")
    fun getPlanet(id: Int): Flow<Planet>

    @Query("SELECT * FROM planets_table")
    fun getAllPlanets(): Flow<List<Planet>>

    @Query("SELECT * FROM planets_table WHERE cAlt <= 0.0")
    fun getRisePlanets(): Flow<List<Planet>>

    @Query("SELECT * FROM planets_table WHERE cAlt > 0.0")
    fun getSetPlanets(): Flow<List<Planet>>
}
