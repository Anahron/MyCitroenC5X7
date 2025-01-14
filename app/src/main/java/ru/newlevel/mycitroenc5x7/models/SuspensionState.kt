package ru.newlevel.mycitroenc5x7.models

data class SuspensionState(
    val mode: Mode = Mode.NORMAL,
    val isSport: Boolean = false
)