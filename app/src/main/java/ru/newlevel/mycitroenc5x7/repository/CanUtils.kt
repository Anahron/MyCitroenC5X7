package ru.newlevel.mycitroenc5x7.repository

import ru.newlevel.mycitroenc5x7.models.CanInfoModel
import ru.newlevel.mycitroenc5x7.models.MomentTripData
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
        }
        return canInfoModel
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

