package planets.position.database.solar

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solar_table")
data class Solar(
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
    var sunRise: Double,
    var sunSet: Double,
    var ratio: Double,
    var fractionCovered: Double,
    var sunAz: Double,
    var sunAlt: Double,
    var localMag: Double,
    var sarosNum: Int,
    var sarosMemNum: Int,
    var moonAz: Double,
    var moonAlt: Double,
    var globalMax: Double,
    var globalBegin: Double,
    var globalEnd: Double,
    var globalTotalBegin: Double,
    var globalTotalEnd: Double,
    var globalCenterBegin: Double,
    var globalCenterEnd: Double,
    var eclipseDate: Double,
    var eclipseType: String?
)
