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

package planets.position

import android.app.Application
import planets.position.database.PlanetsRoomDatabase
import planets.position.database.lunar.LunarRepository
import planets.position.database.planet.PlanetRepository
import planets.position.database.solar.SolarRepository
import planets.position.database.timezone.TimeZoneDatabase
import planets.position.database.timezone.TimeZoneRepository

class PlanetApplication : Application() {
    val database by lazy { PlanetsRoomDatabase.getDatabase(this) }
    val planetRepository by lazy { PlanetRepository(database.planetDAO()) }
    val solarRepository by lazy { SolarRepository(database.solarDAO()) }
    val lunarRepository by lazy { LunarRepository(database.lunarDAO()) }
    private val tzDB by lazy { TimeZoneDatabase.getDatabase(this) }
    val timeZoneRepository by lazy { TimeZoneRepository(tzDB.timeZoneDAO()) }
}
