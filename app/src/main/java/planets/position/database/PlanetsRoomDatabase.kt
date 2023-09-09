package planets.position.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import planets.position.database.lunar.Lunar
import planets.position.database.lunar.LunarDAO
import planets.position.database.lunar.Occult
import planets.position.database.planet.Planet
import planets.position.database.planet.PlanetDAO
import planets.position.database.solar.Solar
import planets.position.database.solar.SolarDAO

@Database(
    entities = [Planet::class, Solar::class, Lunar::class, Occult::class],
    version = 1,
    exportSchema = false
)
abstract class PlanetsRoomDatabase : RoomDatabase() {
    abstract fun planetDAO(): PlanetDAO
    abstract fun solarDAO(): SolarDAO
    abstract fun lunarDAO(): LunarDAO

    companion object {
        @Volatile
        private var INSTANCE: PlanetsRoomDatabase? = null

        fun getDatabase(context: Context): PlanetsRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlanetsRoomDatabase::class.java,
                    "planets_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
