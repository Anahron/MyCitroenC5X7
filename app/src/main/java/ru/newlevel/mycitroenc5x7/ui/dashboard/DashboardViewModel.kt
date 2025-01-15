package ru.newlevel.mycitroenc5x7.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.newlevel.mycitroenc5x7.models.PersonSettingsStatus
import ru.newlevel.mycitroenc5x7.repository.CanRepo

class DashboardViewModel(private val canRepo: CanRepo) : ViewModel() {

    private val _state = MutableStateFlow(PersonSettingsStatus())
    val state: StateFlow<PersonSettingsStatus> = _state.asStateFlow()


    init {
        setupSuspensionStateUpdate()
    }

    private fun setupSuspensionStateUpdate() {
        viewModelScope.launch {
            canRepo.canDataInfoFlow.collect { entity ->
                _state.value = entity.personSettingsStatus
            }
        }
    }
}