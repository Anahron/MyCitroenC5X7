package ru.newlevel.mycitroenc5x7.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.newlevel.mycitroenc5x7.models.CanInfoModel
import ru.newlevel.mycitroenc5x7.repository.CanData
import ru.newlevel.mycitroenc5x7.repository.CanRepo

class AlertsViewModel(private val canRepo: CanRepo) : ViewModel() {


    private val _alertsState = MutableStateFlow(CanInfoModel())
    val alertsState: StateFlow<CanInfoModel> = _alertsState.asStateFlow()

    private val _canDataFlow = MutableSharedFlow<CanData>()
    val canDataFlow: SharedFlow<CanData> = _canDataFlow

    private val _logFlow = MutableSharedFlow<String>()
    val  logFlow: SharedFlow<String> = _logFlow

    init {
        setupAlertsStateUpdate()
        setupCanFlowUpdate()
        setupLogUpdate()
    }

    private fun setupLogUpdate() {
        viewModelScope.launch {
                canRepo.logger.collect { entity ->
                    _logFlow.emit(entity)
                }
            }
    }

    private fun setupAlertsStateUpdate() {
        viewModelScope.launch {
            canRepo.canDataInfoFlow.collect { entity ->
                _alertsState.value = entity
            }
        }
    }
    private fun setupCanFlowUpdate() {
        viewModelScope.launch {
            canRepo.canDataFlow.collect { entity ->
                _canDataFlow.emit(entity)
            }
        }
    }

}