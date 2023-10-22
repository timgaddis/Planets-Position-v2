package planets.position.database.lunar

import kotlinx.coroutines.flow.Flow

class LunarRepository(private val lunarDAO: LunarDAO) {

    fun getLunarEclipse(id: Int): Flow<Lunar> {
        return lunarDAO.getLunarEclipse(id)
    }

    fun getLunarEclipseList(date: Double): Flow<List<Lunar>> {
        return lunarDAO.getLunarEclipseList(date)
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

    fun getLunarOccultList(date: Double): Flow<List<Occult>> {
        return lunarDAO.getLunarOccultList(date)
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
