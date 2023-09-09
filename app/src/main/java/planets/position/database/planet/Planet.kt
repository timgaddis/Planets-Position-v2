package planets.position.database.planet

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planets_table")
data class Planet(
    @PrimaryKey
    var cID: Int,
    var cName: String?,
    var cNumber: Int,
    var cRise: Int,
    var cRA: Double,
    var cRAString: String,
    var cDec: Double,
    var cDecString: String,
    var cAz: Double,
    var cAzString: String,
    var cAlt: Double,
    var cAltString: String,
    var cDistance: Double,
    var cMagnitude: Double,
    var cSetTime: Double,
    var cRiseTime: Double,
    var cRSTimeString: String,
    var cTransit: Double
)
