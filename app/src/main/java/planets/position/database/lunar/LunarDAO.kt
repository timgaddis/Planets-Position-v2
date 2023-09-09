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

    @Query("SELECT * FROM lunar_table WHERE penumbralBegin >= :date ORDER BY penumbralBegin")
    fun getLunarEclipseList(date: Double): Flow<List<Lunar>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(occult: Occult)

    @Query("DELETE FROM occult_table")
    fun deleteAllOccult()

    @Query("SELECT * FROM occult_table WHERE globalBegin >= :date ORDER BY globalBegin")
    fun getLunarOccultList(date: Double): Flow<List<Occult>>

    @Query("SELECT * FROM occult_table WHERE id = :id")
    fun getLunarOccult(id: Int): Flow<Occult>
}
