package ru.newlevel.mycitroenc5x7.models

data class NaviServiceModel(
    val distance: Byte =  0xBB.toByte(),
    val turn: Byte = 0xBB.toByte(),
)