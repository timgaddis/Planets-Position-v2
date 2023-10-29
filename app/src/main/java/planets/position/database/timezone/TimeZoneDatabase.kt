/*
 * Planet's Position
 * A program to calculate the position of the planets in the night sky
 * based on a given location on Earth.
 * Copyright (c) 2023 Tim Gaddis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
