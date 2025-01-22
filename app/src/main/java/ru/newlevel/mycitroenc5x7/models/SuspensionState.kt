package ru.newlevel.mycitroenc5x7.models

data class SuspensionState(
    val mode: Mode = Mode.NONE,
    val isSport: Boolean = false
)