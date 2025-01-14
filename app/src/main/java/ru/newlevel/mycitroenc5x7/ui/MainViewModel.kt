package ru.newlevel.mycitroenc5x7.ui

import androidx.core.view.TintableBackgroundView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.newlevel.mycitroenc5x7.models.CanInfoModel
import ru.newlevel.mycitroenc5x7.repository.CanRepo

class MainViewModel(private val canRepo: CanRepo) : ViewModel() {

    private val _suspensionState = MutableStateFlow(CanInfoModel())
    val suspensionState: StateFlow<CanInfoModel> = _suspensionState.asStateFlow()


    init {
        setupSuspensionStateUpdate()
    }

    fun setToBackground(isBackground: Boolean){
        canRepo.setToBackground(isBackground)
    }

    private fun setupSuspensionStateUpdate() {
        viewModelScope.launch {
            canRepo.canDataInfoFlow.collect { entity ->
                _suspensionState.value = entity
            }
        }
    }
}