package ru.newlevel.mycitroenc5x7.app

import ru.newlevel.mycitroenc5x7.models.CanInfoModel
import ru.newlevel.mycitroenc5x7.ui.home.UiTripModel


fun CanInfoModel.mapToUiTripModel(): UiTripModel{
    return UiTripModel(
        avgSpeedTrip1 = this.trip1.avgSpeed.toString() + " км/ч",
        avgSpeedTrip2 = this.trip2.avgSpeed.toString()+ " км/ч",
        totalDistanceTrip1 = this.trip1.totalDistance.toString() + " км",
        totalDistanceTrip2 = this.trip2.totalDistance.toString() + " км",
        litersPer100kmTrip1 = this.trip1.litersPer100km.toString() + " л/100",
        litersPer100kmTrip2 = this.trip2.litersPer100km.toString() + " л/100",
        litersPer100km = this.momentTrip.litersPer100km.toString() + " л/100",
        totalDistance = this.momentTrip.totalDistance.toString() + " км",
        totalDistanceFinish = this.momentTrip.totalDistanceFinish.toString() + " км"
    )
}
