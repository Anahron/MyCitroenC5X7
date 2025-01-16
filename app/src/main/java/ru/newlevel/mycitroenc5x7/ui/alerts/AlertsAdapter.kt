package ru.newlevel.mycitroenc5x7.ui.alerts

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.newlevel.mycitroenc5x7.R
import ru.newlevel.mycitroenc5x7.models.Alert
import ru.newlevel.mycitroenc5x7.models.Importance

class AlertsAdapter : RecyclerView.Adapter<AlertsAdapter.AlertViewHolder>() {

    private val alerts = mutableListOf<Alert>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(alerts[position])
    }

    override fun getItemCount(): Int = alerts.size


    @SuppressLint("NotifyDataSetChanged")
    fun updateAlerts(newAlerts: List<Alert>) {
        if (alerts.size != newAlerts.size || !alerts.containsAll(newAlerts)) {
            alerts.clear()
            alerts.addAll(newAlerts)
            notifyDataSetChanged()
        }
    }

    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAlert: TextView = itemView.findViewById(R.id.tv_alert)
        private val ivAlert: ImageView = itemView.findViewById(R.id.iv_alert)
        fun bind(alert: Alert) {
            tvAlert.text = alert.message
            when (alert.importance){
                Importance.INFORMATION ->  ivAlert.setImageResource(R.drawable.vector_alert_info)
                Importance.WARNING ->  ivAlert.setImageResource(R.drawable.vector_alert)
                Importance.STOP ->  ivAlert.setImageResource(R.drawable.vector_alert_warning)
            }

        }
    }
}
