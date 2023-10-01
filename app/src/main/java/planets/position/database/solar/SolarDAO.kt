package planets.position.database.solar

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SolarDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(solar: Solar?)

    @Query("DELETE FROM solar_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM solar_table WHERE globalBegin >= :date ORDER BY globalBegin")
    fun getSolarEclipseList(date: Double): Flow<List<Solar>>

    @Query("SELECT * FROM solar_table ORDER BY globalBegin")
    fun getSolarEclipseList(): Flow<List<Solar>>

    @Query("SELECT * FROM solar_table WHERE id = :id")
    fun getSolarEclipseLive(id: Int): Flow<Solar>

    @Query("SELECT * FROM solar_table WHERE id = :id")
    fun getSolarEclipse(id: Int): Solar

    @Query("SELECT * FROM solar_table ORDER BY globalBegin LIMIT 1")
    fun getFirstEclipse(): Solar

    @Query("SELECT * FROM solar_table ORDER BY globalBegin DESC LIMIT 1")
    fun getLastEclipse(): Solar
}
