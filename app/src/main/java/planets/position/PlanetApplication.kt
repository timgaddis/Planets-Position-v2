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
