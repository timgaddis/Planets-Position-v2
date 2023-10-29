package planets.position.database.timezone

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "worldcities")
data class WorldCity(
    @PrimaryKey
    var _id: Int,
    var city: String,
    var lat: Double,
    var lng: Double,
    var country: String,
    var country_code: String,
    var state: String,
    var altitude: Double,
    var timezone: String,
    var offset: Double,
) {
    override fun toString(): String {
        return city
    }
}
