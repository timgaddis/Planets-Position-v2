/*
 * Planet's Position
 * A program to calculate the position of the planets in the night sky
 * based on a given location on Earth.
 * Copyright (c) 2023-2024 Tim Gaddis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
