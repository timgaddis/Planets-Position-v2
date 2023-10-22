package planets.position.lunar.occult

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
import planets.position.database.lunar.Occult
import planets.position.util.JDUTC
import planets.position.util.PositionFormat
import java.text.DateFormat
import java.util.Calendar
import java.util.GregorianCalendar

class LunarOccultListAdapter(private val onClick: (Occult) -> Unit) :
    ListAdapter<Occult, LunarOccultListAdapter.OccultViewHolder>(OccultDiffCallback) {

    class OccultViewHolder(v: View, val onClick: (Occult) -> Unit) : RecyclerView.ViewHolder(v) {
        private val jdUTC: JDUTC = JDUTC()
        private var pf: PositionFormat? = null
        private var settings: SharedPreferences? = null
        private var mDateFormat: DateFormat? = null
        private var offset: Double = 0.0
        private var planetArray: List<String>? = null
        private var currentOccult: Occult? = null
        private var occultLocal: ImageView = v.findViewById(R.id.rowOccLocal)
        private var occultDate: TextView = v.findViewById(R.id.rowOccDate)
        private var occultPlanet: TextView = v.findViewById(R.id.rowOccPlanet)

        init {
            v.setOnClickListener {
                currentOccult?.let {
                    onClick(it)
                }
            }
            planetArray =
                (v.context as Activity).resources.getStringArray(R.array.occult_array).toList()
            pf = PositionFormat(v.context as Activity)
            settings = PreferenceManager.getDefaultSharedPreferences(v.context as Activity)
            mDateFormat = android.text.format.DateFormat.getDateFormat(v.context)
            offset = settings!!.getFloat("offset", 0f).toDouble()
        }

        fun bind(occult: Occult) {
            currentOccult = occult
            // occult date
            val date = occult.occultDate
            val gc: Calendar = GregorianCalendar()
            gc.timeInMillis = jdUTC.jdmills(date)
            occultDate.text = mDateFormat?.format(gc.time)
            // occult planet
            val i = occult.occultPlanet
            occultPlanet.text = planetArray!![i - 1]
            // local
            val l = occult.local
            if (l > 0) occultLocal.visibility = View.VISIBLE else occultLocal.visibility =
                View.INVISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OccultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.occult_list_item, parent, false)
        return OccultViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: OccultViewHolder, position: Int) {
        val occult = getItem(position)
        holder.bind(occult)
    }

}

object OccultDiffCallback : DiffUtil.ItemCallback<Occult>() {
    override fun areItemsTheSame(oldItem: Occult, newItem: Occult): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Occult, newItem: Occult): Boolean {
        return oldItem.id == newItem.id
    }

}
