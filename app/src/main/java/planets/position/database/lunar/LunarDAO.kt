package planets.position.database.lunar

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LunarDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lunar: Lunar)

    @Query("DELETE FROM lunar_table")
    suspend fun deleteAllEclipse()

    @Query("SELECT * FROM lunar_table WHERE id = :id")
    fun getLunarEclipse(id: Int): Flow<Lunar>

    @Query("SELECT * FROM lunar_table ORDER BY penumbralBegin")
    fun getLunarEclipseList(): Flow<List<Lunar>>

    @Query("SELECT * FROM lunar_table ORDER BY penumbralBegin LIMIT 1")
    fun getFirstEclipse(): Lunar

    @Query("SELECT * FROM lunar_table ORDER BY penumbralBegin DESC LIMIT 1")
    fun getLastEclipse(): Lunar

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(occult: Occult)

    @Query("DELETE FROM occult_table")
    fun deleteAllOccult()

    @Query("SELECT * FROM occult_table WHERE id = :id")
    fun getLunarOccult(id: Int): Flow<Occult>

    @Query("SELECT * FROM occult_table ORDER BY occultPlanet, globalBegin")
    fun getLunarOccultList(): Flow<List<Occult>>

    @Query("SELECT * FROM occult_table ORDER BY globalBegin LIMIT 1")
    fun getFirstOccult(): Occult

    @Query("SELECT * FROM occult_table ORDER BY globalBegin DESC LIMIT 1")
    fun getLastOccult(): Occult
}
