/*
 * Planet's Position
 * A program to calculate the position of the planets in the night sky
 * based on a given location on Earth.
 * Copyright (c) 2023-2024 Tim Gaddis
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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    exportSchema = true
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
                ).addMigrations(MIGRATION_221_1).build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_221_1 = object : Migration(221, 1) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE planets RENAME COLUMN _id TO cID")
                db.execSQL("ALTER TABLE planets RENAME COLUMN name TO cName")
                db.execSQL("ALTER TABLE planets RENAME COLUMN number TO cNumber")
                db.execSQL("ALTER TABLE planets RENAME COLUMN rise TO cRise")
                db.execSQL("ALTER TABLE planets RENAME COLUMN ra TO cRA")
                db.execSQL("ALTER TABLE planets RENAME COLUMN dec TO cDec")
                db.execSQL("ALTER TABLE planets RENAME COLUMN az TO cAz")
                db.execSQL("ALTER TABLE planets RENAME COLUMN alt TO cAlt")
                db.execSQL("ALTER TABLE planets RENAME COLUMN dis TO cDistance")
                db.execSQL("ALTER TABLE planets RENAME COLUMN mag TO cMagnitude")
                db.execSQL("ALTER TABLE planets RENAME COLUMN setT TO cSetTime")
                db.execSQL("ALTER TABLE planets RENAME COLUMN riseT TO cRiseTime")
                db.execSQL("ALTER TABLE planets RENAME COLUMN transit TO cTransit")
                db.execSQL("ALTER TABLE planets RENAME TO planets_table")
                db.execSQL("ALTER TABLE solarEclipse RENAME COLUMN _id TO id")
                db.execSQL("ALTER TABLE solarEclipse ADD COLUMN offset DOUBLE")
                db.execSQL("ALTER TABLE solarEclipse RENAME TO solar_table")
                db.execSQL("ALTER TABLE lunarEclipse RENAME COLUMN _id TO id")
                db.execSQL("ALTER TABLE lunarEclipse ADD COLUMN offset DOUBLE")
                db.execSQL("ALTER TABLE lunarEclipse RENAME TO lunar_table")
                db.execSQL("ALTER TABLE lunarOccult RENAME COLUMN _id TO id")
                db.execSQL("ALTER TABLE lunarOccult ADD COLUMN offset DOUBLE")
                db.execSQL("ALTER TABLE lunarOccult RENAME TO occult_table")
            }
        }
    }
}
