package ru.newlevel.mycitroenc5x7.models

data class TripData(val litersPer100: Int =0, val totalDistance: Int =0, val avgSpeedKmh: Int =0)

data class MomentTripData(val totalDistanceFinish: Int = 0, val totalDistance: Int = 0, val litersPer100km: Int = 0)

data class PersonSettingsStatus(val adaptiveLighting: Boolean = false, val guideMeHome: Boolean = false,
    val durationGuide: Int = 0, val parktronics: Boolean = false, val driverWelcome: Boolean = false,
    val automaticHandbrake: Boolean = false, val cmbBrightness: Int = 0, val isDay: Boolean = true, val espStatus: Boolean = false
, val colorNormal:CmbColor = CmbColor.COLOR_YELLOW, val colorSport:CmbColor = CmbColor.COLOR_YELLOW,
                                val cmbThemeLeft: Int = 0, val cmbThemeRight: Int = 0)

data class Alert(
    val message: String,
    val importance: Importance
)
enum class Importance {
    INFORMATION,
    WARNING,
    STOP
}
enum class CmbColor {
    COLOR_YELLOW,
    COLOR_RED,
    COLOR_BLUE
}