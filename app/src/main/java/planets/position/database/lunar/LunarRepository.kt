package planets.position.database.lunar

import kotlinx.coroutines.flow.Flow

class LunarRepository(private val lunarDAO: LunarDAO) {

    fun getLunarEclipse(id: Int): Flow<Lunar> {
        return lunarDAO.getLunarEclipse(id)
    }

    fun getLunarEclipseList(date: Double): Flow<List<Lunar>> {
        return lunarDAO.getLunarEclipseList(date)
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

    fun insertLunarOccult(occult: Occult) {
        lunarDAO.insert(occult)
    }

    fun deleteAllLunarOccult() {
        lunarDAO.deleteAllOccult()
    }
}
