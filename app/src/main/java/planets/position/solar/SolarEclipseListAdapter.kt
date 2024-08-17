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

package planets.position.solar

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
import planets.position.database.solar.Solar
import planets.position.util.JDUTC
import planets.position.util.PositionFormat
import java.text.DateFormat
import java.util.Calendar
import java.util.GregorianCalendar

class SolarEclipseListAdapter(private val onClick: (Solar) -> Unit) :
    ListAdapter<Solar, SolarEclipseListAdapter.SolarViewHolder>(SolarDiffCallback) {

    class SolarViewHolder(v: View, val onClick: (Solar) -> Unit) : RecyclerView.ViewHolder(v) {
        private val jdUTC: JDUTC = JDUTC()
        private var pf: PositionFormat? = null
        private var settings: SharedPreferences? = null
        private var mDateFormat: DateFormat? = null
        private var mTimeFormat: DateFormat? = null
        private var offset: Double = 0.0
        private var eclipseImage: ImageView = v.findViewById(R.id.rowEclImage)
        private var eclipseLocal: ImageView = v.findViewById(R.id.rowEclLocal)
        private var eclipseDate: TextView = v.findViewById(R.id.rowEclDate)
        private var eclipseType: TextView = v.findViewById(R.id.rowEclType)
        private var currentSolar: Solar? = null

        init {
            v.setOnClickListener {
                currentSolar?.let {
                    onClick(it)
                }
            }

            pf = PositionFormat(v.context as Activity)
            settings = PreferenceManager.getDefaultSharedPreferences(v.context as Activity)
            mDateFormat = android.text.format.DateFormat.getDateFormat(v.context)
            mTimeFormat = android.text.format.DateFormat.getTimeFormat(v.context)
            offset = settings!!.getFloat("offset", 0f).toDouble()
        }

        fun bind(solar: Solar) {
            currentSolar=solar
            // Type image
            val t: Int = solar.globalType
            if (t and 4 == 4) // SE_ECL_TOTAL
                eclipseImage.setImageResource(R.drawable.ic_solar_total)
            else if (t and 8 == 8 || t and 32 == 32) // SE_ECL_ANNULAR
                eclipseImage.setImageResource(R.drawable.ic_solar_annular)
            else if (t and 16 == 16) // SE_ECL_PARTIAL
                eclipseImage.setImageResource(R.drawable.ic_solar_partial)
            else eclipseImage.setImageResource(R.drawable.ic_planet_sun)
            // eclipse date
            val date = solar.eclipseDate
            val gc: Calendar = GregorianCalendar()
            gc.timeInMillis = jdUTC.jdmills(date)
            eclipseDate.text = mDateFormat?.format(gc.time)
            // eclipse type
            val type = solar.eclipseType
            if (type!!.contains("|")) {
                eclipseType.text =
                    type.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            } else {
                eclipseType.text = type
            }
            // eclipse local
            val l = solar.local
            if (l > 0) eclipseLocal.visibility = View.VISIBLE
            else eclipseLocal.visibility = View.INVISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.eclipse_list_item, parent, false)
        return SolarViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: SolarViewHolder, position: Int) {
        val solar = getItem(position)
        holder.bind(solar)
    }

}

object SolarDiffCallback : DiffUtil.ItemCallback<Solar>() {
    override fun areItemsTheSame(oldItem: Solar, newItem: Solar): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Solar, newItem: Solar): Boolean {
        return oldItem.id == newItem.id
    }
}
