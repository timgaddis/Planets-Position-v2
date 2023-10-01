package planets.position.database.solar

import kotlinx.coroutines.flow.Flow

class SolarRepository(private val solarDAO: SolarDAO) {

    fun getSolarEclipseLive(id: Int): Flow<Solar> {
        return solarDAO.getSolarEclipseLive(id)
    }

    fun getSolarEclipse(id: Int): Solar {
        return solarDAO.getSolarEclipse(id)
    }

    fun getFirstEclipse(): Solar {
        return solarDAO.getFirstEclipse()
    }

    fun getLastEclipse(): Solar {
        return solarDAO.getLastEclipse()
    }

    fun getSolarEclipseList(date: Double): Flow<List<Solar>> {
        return solarDAO.getSolarEclipseList(date)
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
