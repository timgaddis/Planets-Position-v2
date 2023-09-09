package planets.position.database.timezone

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "states")
data class State(
    @PrimaryKey
    var _id: Int,
    var country: String,
    var state: String
) {
    override fun toString(): String {
        return state
    }
}
