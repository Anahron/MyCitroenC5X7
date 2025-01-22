package ru.newlevel.mycitroenc5x7.ui.alerts

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.mycitroenc5x7.R
import ru.newlevel.mycitroenc5x7.app.TAG
import ru.newlevel.mycitroenc5x7.databinding.FragmentAlertsBinding

class AlertsFragment() : Fragment(R.layout.fragment_alerts) {


    private val binding: FragmentAlertsBinding by viewBinding()
    private val alertsViewModel by viewModel<AlertsViewModel>()
    private lateinit var alertsAdapter: AlertsAdapter
    private lateinit var canDataAdapter: CanDataAdapter

    @OptIn(ExperimentalUnsignedTypes::class)
//    private val canDataList = listOf(
//        CanData(122, 8, ubyteArrayOf(0xFFu, 0xFu, 0x0Fu, 0xF1u, 0x05u, 0xF6u, 0xAAu, 0x18u)),
//        CanData(217, 6, ubyteArrayOf(0x1Au, 0x12u, 0x13u, 0x14u, 0x15u, 0x16u))
//    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alertsAdapter = AlertsAdapter()
        binding.rvAlerts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlerts.adapter = alertsAdapter
        binding.rvLogs.layoutManager = LinearLayoutManager(requireContext())
        canDataAdapter = CanDataAdapter()
        binding.rvLogs.adapter = canDataAdapter
        collectUiState()
        binding.textAlertJournalTitle.setOnLongClickListener {
            if (binding.rvAlerts.isVisible) {
                binding.textAlertJournalTitle.text = resources.getString(R.string.can_monitor)
                binding.rvAlerts.visibility = View.GONE
                binding.linearRVlog.visibility = View.VISIBLE

            } else{
                binding.textAlertJournalTitle.text = resources.getString(R.string.alerts_journal)
                binding.rvAlerts.visibility = View.VISIBLE
                binding.linearRVlog.visibility = View.GONE
            }
            true
        }
    }

    private fun collectUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            alertsViewModel.alertsState.collect { alerts ->
                Log.e(TAG, "alerts = ${alerts.alerts}")
                alertsAdapter.updateAlerts(alerts.alerts.sortedByDescending { it.importance })
            }
        }
//        viewLifecycleOwner.lifecycleScope.launch {
//            alertsViewModel.logFlow.collect { alerts ->
//                val text = binding.textView.text.toString()
//                binding.textView.text = text + "\n" + alerts
//            }
//        }

        viewLifecycleOwner.lifecycleScope.launch {
//            delay(400)
//            canDataAdapter.addCanData(canDataList[0])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[1])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[0])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[1])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[0])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[1])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[0])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[1])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[0])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[1])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[0])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[1])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[0])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[1])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[0])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[1])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[0])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[1])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[0])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[1])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[0])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[1])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[0])
//            delay(400)
//            canDataAdapter.addCanData(canDataList[1])
            alertsViewModel.canDataFlow.collect { log ->
                canDataAdapter.addCanData(log)
            }
        }
    }
}