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
    private val odoKey = intPreferencesKey("odometer")

    val dayTripFlow: Flow<DayTripData> = context.dataStore.data.map { prefs ->
        val odometer = prefs[odoKey] ?: 0
        val day1Start = prefs[day1key] ?: odometer
        val day2Start = prefs[day2key] ?: odometer
        DayTripData(
            day1Trip = odometer - day1Start,
            day2Trip = odometer - day2Start
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
    suspend fun updateOdometer(newMileage: Int) {
        context.dataStore.edit { prefs ->
            prefs[odoKey] = newMileage
        }
    }
}
