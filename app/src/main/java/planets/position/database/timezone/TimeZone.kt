package planets.position.database.timezone

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_zone")
data class TimeZone(
    @PrimaryKey
    var _id: Int,
    var zone_name: String,
    var country_code: String,
    var abbreviation: String,
    var time_start: Int,
    var gmt_offset: Int,
    var dst: Int
) {
    override fun toString(): String {
        return zone_name
    }
}
