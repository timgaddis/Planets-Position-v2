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
    var cDec: Double,
    var cAz: Double,
    var cAlt: Double,
    var cDistance: Double,
    var cMagnitude: Double,
    var cSetTime: Double,
    var cRiseTime: Double,
    var cTransit: Double
)
