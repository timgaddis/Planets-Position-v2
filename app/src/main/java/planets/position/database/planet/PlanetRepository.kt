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

import kotlinx.coroutines.flow.Flow

class PlanetRepository(private val planetDAO: PlanetDAO) {

    fun getPlanetsList(index: Int): Flow<List<Planet>> {
        return when (index) {
            0 -> {
                planetDAO.getRisePlanets()
            }
            1 -> {
                planetDAO.getSetPlanets()
            }
            else -> {
                planetDAO.getAllPlanets()
            }
        }
    }

    fun getPlanet(id: Int): Flow<Planet> {
        return planetDAO.getPlanet(id)
    }

    suspend fun deleteAllPlanets() {
        planetDAO.deleteAll()
    }

    suspend fun insert(planet: Planet) {
        planetDAO.insert(planet)
    }

}
