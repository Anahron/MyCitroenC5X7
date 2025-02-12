package ru.newlevel.mycitroenc5x7.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("mileage_prefs")

data class DayTripData(val day1Trip: Int, val day2Trip: Int)

class DayTripRepository(private val context: Context) {
    private val day1key = intPreferencesKey("day1_trip")
    private val day2key = intPreferencesKey("day2_trip")

    val dayTripFlow: Flow<DayTripData> = context.dataStore.data.map { prefs ->
        DayTripData(
            day1Trip = prefs[day1key] ?: 0,
            day2Trip = prefs[day2key] ?: 0
        )
    }

    suspend fun updateDay1(newMileage: Int) {
        context.dataStore.edit { prefs ->
            prefs[day1key] = newMileage
        }
    }
    suspend fun updateDay2(newMileage: Int) {
        context.dataStore.edit { prefs ->
            prefs[day2key] = newMileage
        }
    }
}
