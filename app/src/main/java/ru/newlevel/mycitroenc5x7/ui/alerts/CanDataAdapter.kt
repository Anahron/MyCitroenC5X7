package ru.newlevel.mycitroenc5x7.ui.alerts

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.newlevel.mycitroenc5x7.R
import ru.newlevel.mycitroenc5x7.repository.CanData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CanDataAdapter : RecyclerView.Adapter<CanDataAdapter.CanDataViewHolder>() {

    private var canDataList: MutableList<CanData> = mutableListOf()
    val bannedCanIds: MutableSet<Int> = mutableSetOf()
    private var selectedCanId: Int = -1

    @SuppressLint("NotifyDataSetChanged")
    fun addCanData(newCanData: CanData) {
        if (selectedCanId == -1) {
            val existingPosition = canDataList.indexOfFirst { it.canId == newCanData.canId }
            if (existingPosition != -1) {
                canDataList[existingPosition] = newCanData
                notifyItemChanged(existingPosition)
            } else {
                canDataList.add(newCanData)
                notifyItemInserted(canDataList.size - 1)
            }
        } else if(newCanData.canId == selectedCanId){
            if (canDataList.removeAll { it.canId != selectedCanId })
                notifyDataSetChanged()
            canDataList.add(newCanData)
            notifyItemInserted(canDataList.size - 1)
        }
    }
    fun banCanId(canId: Int) {
        bannedCanIds.add(canId)
        // Удалить данные с этим canId из списка
        val indexToRemove = canDataList.indexOfFirst { it.canId == canId }
        if (indexToRemove != -1) {
            canDataList.removeAt(indexToRemove)
            notifyItemRemoved(indexToRemove)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CanDataViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        return CanDataViewHolder(view, adapter = this)
    }

    override fun onBindViewHolder(holder: CanDataViewHolder, position: Int) {
        val canData = canDataList[position]
        holder.bind(canData)
    }

    override fun getItemCount(): Int {
        return canDataList.size
    }

    class CanDataViewHolder(itemView: View, private val adapter: CanDataAdapter) : RecyclerView.ViewHolder(itemView) {
        private val tvId: TextView = itemView.findViewById(R.id.tv_id)
        private val tvDlc: TextView = itemView.findViewById(R.id.tv_dlc)
        private val tvData0: TextView = itemView.findViewById(R.id.tv_data0)
        private val tvData1: TextView = itemView.findViewById(R.id.tv_data1)
        private val tvData2: TextView = itemView.findViewById(R.id.tv_data2)
        private val tvData3: TextView = itemView.findViewById(R.id.tv_data3)
        private val tvData4: TextView = itemView.findViewById(R.id.tv_data4)
        private val tvData5: TextView = itemView.findViewById(R.id.tv_data5)
        private val tvData6: TextView = itemView.findViewById(R.id.tv_data6)
        private val tvData7: TextView = itemView.findViewById(R.id.tv_data7)
        private val tvBan: TextView = itemView.findViewById(R.id.tv_ban)
        private val sdf = SimpleDateFormat("ss.SSS", Locale.getDefault())

        @OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
        fun bind(canData: CanData) {
            tvId.text = canData.canId.toString(16).uppercase()
            tvDlc.text = canData.dlc.toString()
            tvBan.setOnClickListener {
                adapter.banCanId(canData.canId)
            }
            if (adapter.selectedCanId != -1){
                tvBan.text = sdf.format(Date(canData.time))
            } else
                tvBan.text = "Ban"
            tvData0.text = ""
            tvData1.text = ""
            tvData2.text = ""
            tvData3.text = ""
            tvData4.text = ""
            tvData5.text = ""
            tvData6.text = ""
            tvData7.text = ""
            itemView.setOnClickListener {
                adapter.selectedCanId = canData.canId
            }

            canData.data.take(canData.dlc).forEachIndexed { index, byte ->
                when (index) {
                    0 -> tvData0.text = byte.toHexString().uppercase(Locale.ROOT)
                    1 -> tvData1.text =byte.toHexString().uppercase(Locale.ROOT)
                    2 -> tvData2.text = byte.toHexString().uppercase(Locale.ROOT)
                    3 -> tvData3.text = byte.toHexString().uppercase(Locale.ROOT)
                    4 -> tvData4.text = byte.toHexString().uppercase(Locale.ROOT)
                    5 -> tvData5.text = byte.toHexString().uppercase(Locale.ROOT)
                    6 -> tvData6.text = byte.toHexString().uppercase(Locale.ROOT)
                    7 -> tvData7.text = byte.toHexString().uppercase(Locale.ROOT)
                }
            }
        }
    }
}
