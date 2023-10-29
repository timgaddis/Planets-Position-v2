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
