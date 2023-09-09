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
