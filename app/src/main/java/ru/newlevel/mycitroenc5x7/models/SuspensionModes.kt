package ru.newlevel.mycitroenc5x7.models

enum class Mode {
    LOW_TO_NORMAL(),
    LOW_TO_MED(),
    LOW_TO_HIGH(),
    NORMAL_TO_HIGH(),
    MED_TO_HIGH(),
    NORMAL_TO_MED(),
    NORMAL_TO_LOW(),
    MED_TO_NORMAL(),
    MED_TO_LOW(),
    HIGH_TO_MED(),
    HIGH_TO_NORMAL(),
    HIGH_TO_LOW(),
    NOT_GRANTED(),
    HIGH(),
    LOW(),
    MID(),
    NORMAL(),
    NONE(),
}
//    00 00 28 40 00 00 00	h	 low to normal 28 = 10 1000   40 = 100 0000
//    00 00 29 40 00 00 00	h	low to med     29 = 10 1001   40 = 100 0000
//    00 00 2B 40 00 00 00	h	low to high    2b = 10 1011   40 = 100 0000
//    00 00 2B 00 00 00 00	h	normal to high 2b = 10 1011   40 = 100 0000
//    00 00 2B 20 00 00 00	h	med to high    2b = 10 1011   28 = 010 1000
//    00 00 29 00 00 00 00	h	normal to med  29 = 10 1001   00-

//    00 00 32 00 00 00 00	 l	normal to low  32 = 11 0010   00-
//    00 00 30 20 00 00 00	 l	med to normal  30 = 11 0000   20 = 010 0000
//    00 00 32 20 00 00 00	l	med to low     32 = 11 0010   20 = 010 0000
//    00 00 34 70 00 00 00	l	high to med    34 = 11 0100   70 = 111 0000
//    00 00 30 60 00 00 00	l	high to normal 30 = 11 0000   60 = 110 0000
//    00 00 32 60 00 00 00	l	high to low    32 = 11 0010   60 = 110 0000
//
//    00 00 38 00 00 00 00		not granted   38 = 11 1000    00-
//
//    00 00 20 0C 00 00 00		high          20 = 10 0000    0C = 000 1100
//    00 00 20 08 00 00 00		low           20 = 10 0000    08 = 000 1000
//    00 00 20 04 00 00 00		mid           20 = 10 0000    04 = 000 0100
//    00 00 20 00 00 00 00		normal        20 = 10 0000    00-