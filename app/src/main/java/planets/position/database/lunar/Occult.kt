package planets.position.database.lunar

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "occult_table")
data class Occult(
    @PrimaryKey
    var id: Int,
    var localType: Int,
    var globalType: Int,
    var local: Int,
    var offset: Double,
    var localMax: Double,
    var localFirst: Double,
    var localSecond: Double,
    var localThird: Double,
    var localFourth: Double,
    var moonAzStart: Double,
    var moonAltStart: Double,
    var moonAzEnd: Double,
    var moonAltEnd: Double,
    var moonRise: Double,
    var moonSet: Double,
    var globalMax: Double,
    var globalBegin: Double,
    var globalEnd: Double,
    var globalTotalBegin: Double,
    var globalTotalEnd: Double,
    var globalCenterBegin: Double,
    var globalCenterEnd: Double,
    var occultDate: Double,
    var occultPlanet: Int
)
