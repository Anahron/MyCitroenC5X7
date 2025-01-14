package ru.newlevel.mycitroenc5x7.models

data class CanInfoModel(
    val trip1: TripData = TripData(),
    val trip2: TripData = TripData(),
    val momentTrip: MomentTripData = MomentTripData(),
    val suspensionState: SuspensionState = SuspensionState()
)