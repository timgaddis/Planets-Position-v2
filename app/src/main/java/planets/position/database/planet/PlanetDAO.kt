package planets.position.database.planet

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanetDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(planet: Planet)

    @Query("DELETE FROM planets_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM planets_table WHERE cID = :id")
    fun getPlanet(id: Int): Flow<Planet>

    @Query("SELECT * FROM planets_table")
    fun getAllPlanets(): Flow<List<Planet>>

    @Query("SELECT * FROM planets_table WHERE cAlt <= 0.0")
    fun getRisePlanets(): Flow<List<Planet>>

    @Query("SELECT * FROM planets_table WHERE cAlt > 0.0")
    fun getSetPlanets(): Flow<List<Planet>>
}
