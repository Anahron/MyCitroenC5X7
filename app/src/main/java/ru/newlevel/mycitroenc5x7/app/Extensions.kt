package ru.newlevel.mycitroenc5x7.app

import ru.newlevel.mycitroenc5x7.models.CanInfoModel
import ru.newlevel.mycitroenc5x7.ui.home.UiTripModel
import java.util.Locale


fun CanInfoModel.mapToUiTripModel(): UiTripModel{
    return UiTripModel(
        avgSpeedTrip1 = this.trip1.avgSpeed.toString() + " км/ч",
        avgSpeedTrip2 = this.trip2.avgSpeed.toString()+ " км/ч",
        totalDistanceTrip1 = this.trip1.totalDistance.toString() + " км",
        totalDistanceTrip2 = this.trip2.totalDistance.toString() + " км",
        litersPer100kmTrip1 = String.format(Locale("ru", "RU"), "%.1f л/100", this.trip1.litersPer100km / 10.0),
        litersPer100kmTrip2 = String.format(Locale("ru", "RU"), "%.1f л/100", this.trip2.litersPer100km / 10.0),
        litersPer100km = String.format(Locale("ru", "RU"), "%.1f л/100", this.momentTrip.litersPer100km / 10.0),
        totalDistance = this.momentTrip.totalDistance.toString() + " км",
        totalDistanceFinish = String.format(Locale("ru", "RU"), "%.1f км", this.momentTrip.totalDistanceFinish/10.0)
    )
}
