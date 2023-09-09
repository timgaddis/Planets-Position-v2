package planets.position.database.timezone

import androidx.lifecycle.LiveData

class TimeZoneRepository(private var timeZoneDAO: TimeZoneDAO) {

    fun getZoneOffset(zone: String, time: Long): LiveData<TimeZone> {
        return timeZoneDAO.getZoneOffset(zone, time)
    }

    fun getCounrty(country: String): LiveData<Country> {
        return timeZoneDAO.getCountry(country)
    }

    fun getStateList(cc: String): LiveData<List<State>> {
        return timeZoneDAO.getStateList(cc)
    }

    fun getCityList(state: String): LiveData<List<WorldCity>> {
        return timeZoneDAO.getCityList(state)
    }

    fun getCityData(id: Int): LiveData<WorldCity> {
        return timeZoneDAO.getCityData(id)
    }
}
