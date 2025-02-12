package ru.newlevel.mycitroenc5x7.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.newlevel.mycitroenc5x7.app.mapToUiTripModel
import ru.newlevel.mycitroenc5x7.repository.CanRepo
import ru.newlevel.mycitroenc5x7.repository.DayTripData
import ru.newlevel.mycitroenc5x7.repository.DayTripRepository

class HomeViewModel(private val canRepo: CanRepo, private val dayTripRepository: DayTripRepository) : ViewModel() {

    private val tripState = MutableStateFlow(UiTripModel())
    val state: StateFlow<UiTripModel> = tripState.asStateFlow()
    val dayTripDataFlow: StateFlow<DayTripData> = dayTripRepository.dayTripFlow.stateIn(viewModelScope, SharingStarted.Eagerly, DayTripData(0, 0))
    val uiState = MutableStateFlow(1)

    init {
        setupStateUpdate()
    }


    fun updateDay1() {
        viewModelScope.launch {
            if (state.value.odometer != 0)
                dayTripRepository.updateDay1(state.value.odometer)
        }
    }

    fun updateDay2() {
        viewModelScope.launch {
            if (state.value.odometer != 0)
                dayTripRepository.updateDay2(state.value.odometer)
        }
    }

    fun setUi(id: Int) {
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