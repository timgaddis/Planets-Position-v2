package planets.position.database.timezone

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase

@Database(
    entities = [Country::class, State::class, TimeZone::class, WorldCity::class],
    version = 1,
    exportSchema = false
)
abstract class TimeZoneDatabase : RoomDatabase() {
    abstract fun timeZoneDAO(): TimeZoneDAO

    companion object {
        @Volatile
        private var INSTANCE: TimeZoneDatabase? = null

        fun getDatabase(context: Context): TimeZoneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = databaseBuilder(
                    context.applicationContext,
                    TimeZoneDatabase::class.java,
                    "timezone.db"
                ).createFromAsset("timezone.db").build()
                INSTANCE = instance
                instance
            }
        }
    }
}
