package planets.position.database.timezone

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface TimeZoneDAO {
    @Query("SELECT * FROM time_zone WHERE zone_name = :zone AND time_start <= :time ORDER BY time_start DESC LIMIT 1")
    fun getZoneOffset(zone: String, time: Long): LiveData<TimeZone>

    @Query("SELECT * FROM country WHERE country_name = :country")
    fun getCountry(country: String): LiveData<Country>

    @Query("SELECT * FROM states WHERE country = :cc ORDER BY state")
    fun getStateList(cc: String): LiveData<List<State>>

    @Query("SELECT * FROM worldcities WHERE state = :state ORDER BY city")
    fun getCityList(state: String): LiveData<List<WorldCity>>

    @Query("SELECT * FROM worldcities WHERE _id = :id")
    fun getCityData(id: Int): LiveData<WorldCity>
}
