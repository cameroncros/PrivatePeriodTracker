package com.cross.privateperiodtracker

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.cross.privateperiodtracker.data.EventType
import com.cross.privateperiodtracker.data.EventType.*
import com.cross.privateperiodtracker.data.PeriodEvent
import java.time.format.DateTimeFormatter

class EventListAdapter(val deleteEventCallback: (periodEvent: PeriodEvent) -> Unit) :
    RecyclerView.Adapter<EventListAdapter.ViewHolder>() {
    private var dataSet: ArrayList<PeriodEvent>? = null;
    private var pos: Int = 0

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(data: ArrayList<PeriodEvent>)
    {
        dataSet = data
        notifyDataSetChanged()
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val icon: ImageView
        val deleteButton: ImageView
        val context: Context

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.textView)
            icon = view.findViewById(R.id.imageView)
            deleteButton = view.findViewById(R.id.imageButton)
            context = view.context
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.event_row_item, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (dataSet == null)
        {
            return;
        }
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val sb = StringBuilder()
        sb.append(dataSet!![position].time.format(DateTimeFormatter.BASIC_ISO_DATE))
        sb.append(" - ")
        when (dataSet!![position].type) {
            PeriodStart -> sb.append("Period Start")
            PeriodEnd -> sb.append("Period End")
            PregnancyStart -> sb.append("Pregnancy Start")
            PregnancyEnd -> sb.append("Pregnancy End")
            TamponStart -> sb.append("Tampon Start")
            TamponEnd -> sb.append("Tampon End")
            Painkiller -> sb.append("Painkiller")
        }
        viewHolder.textView.text = sb.toString()

        viewHolder.deleteButton.setOnClickListener {
            deleteEventCallback(dataSet!![position])
        }

        when (dataSet!![position].type) {
            PeriodStart -> {
                viewHolder.icon.setImageDrawable(getDrawable(viewHolder.context, R.drawable.icon_period_start))
            }
            PeriodEnd  -> {
                viewHolder.icon.setImageDrawable(getDrawable(viewHolder.context, R.drawable.icon_period_end))
            }
            PregnancyStart -> {
                viewHolder.icon.setImageDrawable(getDrawable(viewHolder.context, R.drawable.icon_pregnancy_start))
            }
            PregnancyEnd -> {
                viewHolder.icon.setImageDrawable(getDrawable(viewHolder.context, R.drawable.icon_pregnancy_end))
            }
            TamponStart -> {
                viewHolder.icon.setImageDrawable(getDrawable(viewHolder.context, R.drawable.icon_tampon_start))
            }
            TamponEnd -> {
                viewHolder.icon.setImageDrawable(getDrawable(viewHolder.context, R.drawable.icon_tampon_end))
            }
            Painkiller -> {
                viewHolder.icon.setImageDrawable(getDrawable(viewHolder.context, R.drawable.icon_painkiller))
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        if (dataSet == null)
        {
            return 0
        }
        return dataSet!!.size
    }

}