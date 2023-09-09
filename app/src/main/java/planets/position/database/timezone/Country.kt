package planets.position.database.timezone

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "country")
data class Country(
    @PrimaryKey()
    var _id: Int,
    var country_code: String,
    var country_name: String
)
