package planets.position.database.lunar

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lunar_table")
data class Lunar(
    @PrimaryKey
    var id: Int,
    var localType: Int,
    var globalType: Int,
    var local: Int,
    var offset: Double,
    var umbralMag: Double,
    var penumbralMag: Double,
    var moonAz: Double,
    var moonAlt: Double,
    var moonRise: Double,
    var moonSet: Double,
    var sarosNum: Int,
    var sarosMemNum: Int,
    var maxEclipse: Double,
    var penumbralBegin: Double,
    var penumbralEnd: Double,
    var partialBegin: Double,
    var partialEnd: Double,
    var totalBegin: Double,
    var totalEnd: Double,
    var eclipseDate: Double,
    var eclipseType: String?
)
