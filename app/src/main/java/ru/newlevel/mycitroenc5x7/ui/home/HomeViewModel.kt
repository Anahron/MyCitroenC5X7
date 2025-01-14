package ru.newlevel.mycitroenc5x7.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.newlevel.mycitroenc5x7.app.mapToUiTripModel
import ru.newlevel.mycitroenc5x7.repository.CanRepo

class HomeViewModel(private val canRepo: CanRepo) : ViewModel() {

    private val tripState = MutableStateFlow(UiTripModel())
    val state: StateFlow<UiTripModel> = tripState.asStateFlow()

    val uiState = MutableStateFlow(1)

    init {
        setupStateUpdate()
    }

    fun setUi(id: Int){
        uiState.value = id
    }
    private fun setupStateUpdate() {
        viewModelScope.launch {
            canRepo.canDataInfoFlow.collect { entity ->
                tripState.value = entity.mapToUiTripModel()
            }
        }
    }
}