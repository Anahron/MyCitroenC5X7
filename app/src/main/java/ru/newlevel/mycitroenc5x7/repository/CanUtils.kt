package ru.newlevel.mycitroenc5x7.repository

import ru.newlevel.mycitroenc5x7.models.CanInfoModel
import ru.newlevel.mycitroenc5x7.models.Mode
import ru.newlevel.mycitroenc5x7.models.MomentTripData
import ru.newlevel.mycitroenc5x7.models.SuspensionState
import ru.newlevel.mycitroenc5x7.models.TripData

@OptIn(ExperimentalUnsignedTypes::class)
class CanUtils {
    // 0x2A1 первый слот трип 0x261  второй слот 0x221 мгновенный расход
    fun checkCanId(canData: CanData, canInfoModel: CanInfoModel): CanInfoModel {
        when (canData.canId) {
            0x261 -> { //trip2
                return canInfoModel.copy(trip2 = decodeTrip(canData))
            }

            0x2A1 -> { //trip1
                return canInfoModel.copy(trip1 = decodeTrip(canData))
            }

            0x221 -> { // moment trip
                return canInfoModel.copy(momentTrip = decodeTripMomentData(canData))
            }

            0x0E8 -> { // suspension status
                return canInfoModel.copy(suspensionState = checkSuspensionStatus(canData, canInfoModel.suspensionState))
            }
        }
        return canInfoModel
    }


//    00 00 28 40 00 00 00	h	 low to normal 28 = 10 1000   40 = 100 0000 +
//    00 00 29 40 00 00 00	h	low to med     29 = 10 1001   40 = 100 0000 +
//    00 00 2B 40 00 00 00	h	low to high    2b = 10 1011   40 = 100 0000 +
//    00 00 2B 00 00 00 00	h	normal to high 2b = 10 1011   00 = 000 0000 +
//    00 00 2B 20 00 00 00	h	med to high    2b = 10 1011   20 = 010 0000 +
//    00 00 29 00 00 00 00	h	normal to med  29 = 10 1001   00-           +

//    00 00 32 00 00 00 00	 l	normal to low  32 = 11 0010   00-           +
//    00 00 30 20 00 00 00	 l	med to normal  30 = 11 0000   20 = 010 0000 +
//    00 00 32 20 00 00 00	l	med to low     32 = 11 0010   20 = 010 0000 +
//    00 00 34 60 00 00 00	l	high to med    34 = 11 0100   60 = 110 0000 +
//    00 00 30 60 00 00 00	l	high to normal 30 = 11 0000   60 = 110 0000 +
//    00 00 32 60 00 00 00	l	high to low    32 = 11 0010   60 = 110 0000 +
//
//    00 00 38 00 00 00 00		not granted   38 = 11 1000    00-           +
//
//    00 00 20 0C 00 00 00		high          20 = 10 0000    0C = 000 1100 +
//    00 00 20 08 00 00 00		low           20 = 10 0000    08 = 000 1000 +
//    00 00 20 04 00 00 00		mid           20 = 10 0000    04 = 000 0100 +
//    00 00 20 00 00 00 00		normal        20 = 10 0000    00-           +
    private fun checkSuspensionStatus(canData: CanData, suspensionState: SuspensionState): SuspensionState {
        val isSport = checkBit(canData.data[3], 1)

        if (checkBit(canData.data[2], 5)) {
            val f28 = checkBit(canData.data[2], 5) && !checkBit(canData.data[2], 4) && checkBit(canData.data[2], 3) && !checkBit(canData.data[2], 2) && !checkBit(canData.data[2], 1) && !checkBit(canData.data[2], 0)
            val f29 = checkBit(canData.data[2], 5) && !checkBit(canData.data[2], 4) && checkBit(canData.data[2], 3) && !checkBit(canData.data[2], 2) && !checkBit(canData.data[2], 1) && checkBit(canData.data[2], 0)
            val f2b = checkBit(canData.data[2], 5) && !checkBit(canData.data[2], 4) && checkBit(canData.data[2], 3) && !checkBit(canData.data[2], 2) && checkBit(canData.data[2], 1) && checkBit(canData.data[2], 0)
            val f32 = checkBit(canData.data[2], 5) && checkBit(canData.data[2], 4) && !checkBit(canData.data[2], 3) && !checkBit(canData.data[2], 2) && checkBit(canData.data[2], 1) && !checkBit(canData.data[2], 0)
            val f30 = checkBit(canData.data[2], 5) && checkBit(canData.data[2], 4) && !checkBit(canData.data[2], 3) && !checkBit(canData.data[2], 2) && !checkBit(canData.data[2], 1) && !checkBit(canData.data[2], 0)
            val f34 = checkBit(canData.data[2], 5) && checkBit(canData.data[2], 4) && !checkBit(canData.data[2], 3) && checkBit(canData.data[2], 2) && !checkBit(canData.data[2], 1) && !checkBit(canData.data[2], 0)
            val f38 = checkBit(canData.data[2], 5) && checkBit(canData.data[2], 4) && checkBit(canData.data[2], 3) && !checkBit(canData.data[2], 2) && !checkBit(canData.data[2], 1) && !checkBit(canData.data[2], 0)
            val f20 = checkBit(canData.data[2], 5) && !checkBit(canData.data[2], 4) && !checkBit(canData.data[2], 3) && !checkBit(canData.data[2], 2) && !checkBit(canData.data[2], 1) && !checkBit(canData.data[2], 0)

            val s40 = checkBit(canData.data[3], 6) && !checkBit(canData.data[3], 5)
            val s00 = !checkBit(canData.data[3], 6) && !checkBit(canData.data[3], 5)
            val s20 = !checkBit(canData.data[3], 6) && checkBit(canData.data[3], 5)
            val s60 = checkBit(canData.data[3], 6) && checkBit(canData.data[3], 5)

            val s0c = checkBit(canData.data[3], 2) && checkBit(canData.data[3], 1)
            val s08 = checkBit(canData.data[3], 2) && !checkBit(canData.data[3], 1)
            val s04 = !checkBit(canData.data[3], 2) && checkBit(canData.data[3], 1)
            val s00n = !checkBit(canData.data[3], 2) && !checkBit(canData.data[3], 1)

            val mode = when {
                f38 -> Mode.NOT_GRANTED
                f20 && s00n -> Mode.NORMAL
                f20 && s04 -> Mode.MID
                f20 && s08 -> Mode.LOW
                f20 && s0c -> Mode.HIGH
                f28 && s40 -> Mode.LOW_TO_NORMAL
                f29 && s40 -> Mode.LOW_TO_MED
                f2b && s40 -> Mode.LOW_TO_HIGH
                f2b && s00 -> Mode.NORMAL_TO_HIGH
                f2b && s20 -> Mode.MED_TO_HIGH
                f29 && s00 -> Mode.NORMAL_TO_MED
                f32 && s00 -> Mode.NORMAL_TO_LOW
                f30 && s20 -> Mode.MED_TO_NORMAL
                f32 && s20 -> Mode.MED_TO_LOW
                f34 && s60 -> Mode.HIGH_TO_MED
                f30 && s60 -> Mode.HIGH_TO_NORMAL
                f32 && s60 -> Mode.HIGH_TO_LOW
                else -> Mode.NORMAL
            }

            return suspensionState.copy(mode = mode, isSport =  isSport)
        }

        return suspensionState.copy(isSport = isSport)
    }

    fun checkBit(byte: UByte, bitIndex: Int): Boolean {
        return (byte.toInt() and (1 shl bitIndex)) != 0
    }

    private fun decodeTrip(canData: CanData): TripData {
        val litersPer100km = canData.data[0].toInt()

        // Пробег автомобиля после сброса байт 1 и 2 00YYYYYY YYYYYYYY
        val highBits = (canData.data[1].toInt() and 0x3F) // Маска 0x3F = 00111111
        val lowBits = canData.data[2].toInt()
        val totalDistance = (highBits shl 8) or lowBits

        // Средняя скорость байт 3 и 4
        val highByte = canData.data[3].toInt()
        val lowByte = canData.data[4].toInt()

        // Объединяем два байта в 16-битное значение
        val avgSpeed = (highByte shl 8) or lowByte

        return TripData(
            litersPer100km = litersPer100km,
            totalDistance = totalDistance,
            avgSpeed = avgSpeed
        )
    }


    //на nac только остаток пробега и средний расход + тут же трип бтн byte[0] 4
    fun decodeTripMomentData(canData: CanData): MomentTripData {

        val litersPer100km = (canData.data[1].toInt() shl 8) or canData.data[2].toInt()

        val totalDistance =
            (canData.data[3].toInt() shl 8) or canData.data[4].toInt() // Rest of run on current fuel level видимо остаток пробега на баке max 2000

        val totalDistanceFinish =
            (canData.data[5].toInt() shl 8) or canData.data[6].toInt()  // /10 Rest of run to finish  [хз]  max 60000 == 6000.0

        return MomentTripData(
            totalDistanceFinish = totalDistanceFinish,
            totalDistance = totalDistance,
            litersPer100km = litersPer100km
        )
    }
}

