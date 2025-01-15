package ru.newlevel.mycitroenc5x7.models

data class TripData(val avgSpeed: Int =0, val totalDistance: Int =0, val litersPer100km: Int =0)

data class MomentTripData(val totalDistanceFinish: Int = 0, val totalDistance: Int = 0, val litersPer100km: Int = 0)

data class PersonSettingsStatus(val adaptiveLighting: Boolean = false, val guideMeHome: Boolean = false,
    val durationGuide: Int = 0, val parktronics: Boolean = false, val driverWelcome: Boolean = false,
    val automaticHandbrake: Boolean = false, val cmbBrightness: Int = 0, val isDay: Boolean = true)