package ru.newlevel.mycitroenc5x7.app

import ru.newlevel.mycitroenc5x7.models.CanInfoModel
import ru.newlevel.mycitroenc5x7.ui.home.UiTripModel
import java.util.Locale


fun CanInfoModel.mapToUiTripModel(): UiTripModel{
    return UiTripModel(
        avgSpeedTrip1 = String.format(Locale("ru", "RU"), "%.1f л/100",  this.trip1.litersPer100 /10.0),
        avgSpeedTrip2 = String.format(Locale("ru", "RU"), "%.1f л/100",  this.trip2.litersPer100 /10.0),           //л/100
        totalDistanceTrip1 = this.trip1.totalDistance.toString() + " км",
        totalDistanceTrip2 = this.trip2.totalDistance.toString() + " км",

        litersPer100kmTrip1 = this.trip1.avgSpeedKmh.toString() + " км/ч",
        litersPer100kmTrip2 = this.trip2.avgSpeedKmh.toString() + " км/ч", // км/ч


        litersPer100km =  if (this.momentTrip.litersPer100km > 400) "-- л/100" else String.format(Locale("ru", "RU"), "%.1f л/100", this.momentTrip.litersPer100km.coerceIn(0, 300) / 10.0),
        totalDistance = this.momentTrip.totalDistance.toString() + " км",
        totalDistanceFinish = String.format(Locale("ru", "RU"), "%.1f км", this.momentTrip.totalDistanceFinish/10.0)
    )
}
