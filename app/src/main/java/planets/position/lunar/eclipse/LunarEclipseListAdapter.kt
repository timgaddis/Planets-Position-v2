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

package planets.position.lunar.eclipse

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
import planets.position.database.lunar.Lunar
import planets.position.util.JDUTC
import planets.position.util.PositionFormat
import java.text.DateFormat
import java.util.Calendar
import java.util.GregorianCalendar

class LunarEclipseListAdapter(private val onClick: (Lunar) -> Unit) :
    ListAdapter<Lunar, LunarEclipseListAdapter.LunarViewHolder>(LunarDiffCallback) {

    class LunarViewHolder(v: View, val onClick: (Lunar) -> Unit) : RecyclerView.ViewHolder(v) {
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
        private var currentLunar: Lunar? = null

        init {
            v.setOnClickListener {
                currentLunar?.let {
                    onClick(it)
                }
            }

            pf = PositionFormat(v.context as Activity)
            settings = PreferenceManager.getDefaultSharedPreferences(v.context as Activity)
            mDateFormat = android.text.format.DateFormat.getDateFormat(v.context)
            mTimeFormat = android.text.format.DateFormat.getTimeFormat(v.context)
            offset = settings!!.getFloat("offset", 0f).toDouble()
        }

        fun bind(lunar: Lunar) {
            currentLunar = lunar
            // Type image
            val t: Int = lunar.globalType
            if (t and 4 == 4) // LE_ECL_TOTAL
                eclipseImage.setImageResource(R.drawable.ic_lunar_total)
            else if (t and 64 == 64) // LE_ECL_PENUMBRAL
                eclipseImage.setImageResource(R.drawable.ic_lunar_penumbral)
            else if (t and 16 == 16) // LE_ECL_PARTIAL
                eclipseImage.setImageResource(R.drawable.ic_lunar_partial)
            else eclipseImage.setImageResource(R.drawable.ic_planet_moon)
            // eclipse date
            val date = lunar.eclipseDate
            val gc: Calendar = GregorianCalendar()
            gc.timeInMillis = jdUTC.jdmills(date)
            eclipseDate.text = mDateFormat?.format(gc.time)
            // eclipse type
            val type = lunar.eclipseType
            if (type!!.contains("|")) {
                eclipseType.text =
                    type.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            } else {
                eclipseType.text = type
            }
            // eclipse local
            val l = lunar.local
            if (l > 0) eclipseLocal.visibility = View.VISIBLE
            else eclipseLocal.visibility = View.INVISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LunarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.eclipse_list_item, parent, false)
        return LunarViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: LunarViewHolder, position: Int) {
        val lunar = getItem(position)
        holder.bind(lunar)
    }

}

object LunarDiffCallback : DiffUtil.ItemCallback<Lunar>() {
    override fun areItemsTheSame(oldItem: Lunar, newItem: Lunar): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Lunar, newItem: Lunar): Boolean {
        return oldItem.id == newItem.id
    }

}
