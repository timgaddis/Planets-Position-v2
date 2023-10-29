/*
 * Planet's Position
 * A program to calculate the position of the planets in the night sky
 * based on a given location on Earth.
 * Copyright (c) 2023 Tim Gaddis
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

package planets.position.riseset

import android.app.Activity
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import planets.position.R
import planets.position.database.planet.Planet
import planets.position.util.JDUTC
import planets.position.util.PositionFormat
import java.text.DateFormat
import java.util.Calendar

class RiseSetListAdapter(private val onClick: (Planet) -> Unit) :
    ListAdapter<Planet, RiseSetListAdapter.PlanetViewHolder>(PlanetDiffCallback) {

    class PlanetViewHolder(v: View, val onClick: (Planet) -> Unit) :
        RecyclerView.ViewHolder(v) {
        private val jdUTC: JDUTC = JDUTC()
        private var pf: PositionFormat? = null
        private var settings: SharedPreferences? = null
        private var mDateFormat: DateFormat? = null
        private var mTimeFormat: DateFormat? = null
        private var rowImage: ImageView = v.findViewById(R.id.rowImage)
        private var rowName: TextView = v.findViewById(R.id.rowName)
        private var rowAz: TextView = v.findViewById(R.id.rowAZ)
        private var rowAlt: TextView = v.findViewById(R.id.rowALT)
        private var rowDate: TextView = v.findViewById(R.id.rowRSDate)
        private var rowRiseSet: TextView = v.findViewById(R.id.rowRiseSet)
        private var currentPlanet: Planet? = null
        private var offset: Double = 0.0
//        private var lastupdate: Long = 0

        init {
            v.setOnClickListener {
                currentPlanet?.let {
                    onClick(it)
                }
            }
            pf = PositionFormat(v.context as Activity)
            settings = PreferenceManager.getDefaultSharedPreferences(v.context as Activity)
            mDateFormat = android.text.format.DateFormat.getDateFormat(v.context)
            mTimeFormat = android.text.format.DateFormat.getTimeFormat(v.context)
            offset = settings!!.getFloat("offset", 0f).toDouble()
//            lastupdate = settings!!.getLong("lastUpdate", 0)
        }

        fun bind(planet: Planet) {
            currentPlanet = planet
            val c: Calendar = Calendar.getInstance()
            rowImage.setImageResource(planet.cNumber)
            rowName.text = planet.cName
            rowAz.text = pf?.formatAZ(planet.cAz)
            rowAlt.text = pf?.formatALT(planet.cAlt)
            val time: Double = if (planet.cRise > 0.0) {
                rowRiseSet.setText(R.string.data_set)
                planet.cSetTime
            } else {
                rowRiseSet.setText(R.string.data_rise)
                planet.cRiseTime
            }
            c.clear()
            c.timeInMillis = jdUTC.jdmills(time, offset * 60.0)
            rowDate.text = String.format(
                "%s %s", mDateFormat?.format(c.time),
                mTimeFormat?.format(c.time)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.planet_list_item, parent, false)
        return PlanetViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: PlanetViewHolder, position: Int) {
        val planet = getItem(position)
        holder.bind(planet)
    }
}

object PlanetDiffCallback : DiffUtil.ItemCallback<Planet>() {
    override fun areItemsTheSame(oldItem: Planet, newItem: Planet): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Planet, newItem: Planet): Boolean {
        return oldItem.cName == newItem.cName
    }
}
