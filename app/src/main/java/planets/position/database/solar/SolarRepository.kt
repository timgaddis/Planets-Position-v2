package planets.position.database.solar

import kotlinx.coroutines.flow.Flow

class SolarRepository(private val solarDAO: SolarDAO) {

    fun getSolarEclipse(id: Int): Flow<Solar> {
        return solarDAO.getSolarEclipse(id)
    }

    fun getSolarEclipseList(date: Double): Flow<List<Solar>> {
        return solarDAO.getSolarEclipseList(date)
    }

    suspend fun insertSolarEclipse(solar: Solar) {
        solarDAO.insert(solar)
    }

    suspend fun deleteAllSolarEclipse() {
        solarDAO.deleteAll()
    }
}
