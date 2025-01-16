package ru.newlevel.mycitroenc5x7.repository

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import ru.newlevel.mycitroenc5x7.app.TAG
import ru.newlevel.mycitroenc5x7.models.CanInfoModel


class CanRepo(private val canUtils: CanUtils) {

    private val _canDataFlow = MutableSharedFlow<CanData>()
    val canDataFlow: SharedFlow<CanData> = _canDataFlow

    private val _canDataInfoFlow = MutableStateFlow<CanInfoModel>(CanInfoModel())
    val canDataInfoFlow: SharedFlow<CanInfoModel> = _canDataInfoFlow

    private val backgroundState = MutableStateFlow<Boolean>(false)


    private val _logger = MutableSharedFlow<String>()
    val logger: SharedFlow<String> = _logger

    suspend fun putLog(text: String) {
        _logger.emit(text)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun setToBackground(isBackground: Boolean){
        backgroundState.value = isBackground
        val uByteArray = ubyteArrayOf(
            0xFF.toUByte(),
            0xFF.toUByte(),
            0xFF.toUByte(),
            0xFF.toUByte(),
            0xFF.toUByte(),
            0xFF.toUByte(),
            0xFF.toUByte()
        )
        _canDataInfoFlow.value = canUtils.checkCanId(canData = CanData(canId = 0x120, dlc = 8, uByteArray), CanInfoModel() )
    }

    fun getIsBackground(): Boolean = backgroundState.value


    @OptIn(ExperimentalUnsignedTypes::class)
    suspend fun processCanMessage(data: ByteArray) {
        data.let { packet ->
            val packetBuffer = packet.toUByteArray()
            if (packetBuffer.size >= 4) { // Должно быть хотя бы 4 байта для ID и DLC
                // Извлекаем CAN ID (2 байта)
                val canId = (packetBuffer[0].toInt() shl 8) or packetBuffer[1].toInt()
                // Извлекаем DLC (1 байт)
                val dlc = packetBuffer[2].toInt()
                // Проверка, чтобы DLC не выходило за границы массива
                if (packetBuffer.size >= 3 + dlc + 1) {
                    // Извлекаем данные (dlc байтов)
                    val canData = packetBuffer.sliceArray(3 until 3 + dlc)

                    // Извлекаем CRC (последний байт)
                    val crcReceived = packetBuffer[3 + dlc]

                    // Вычисление контрольной суммы и проверка
                    val crcCalculated = calculateCRC(canId, dlc, canData)

                    if (crcReceived == crcCalculated) {
                        val can = CanData(canId = canId, data = canData, dlc = dlc)
                        _canDataFlow.emit(can)
                        _canDataInfoFlow.value = canUtils.checkCanId(canData = can, _canDataInfoFlow.value)
                        Log.e(
                            TAG, "ID: 0x${canId.toString(16).uppercase()}, DLC: $dlc Data: ${
                                canData.joinToString(", ") {
                                    "0x${it.toString(16).uppercase()}"
                                }
                            }")
                    } else {
                        Log.e(TAG, "CRC error")
                    }
                } else {
                    Log.e(TAG, "Invalid DLC or missing data")
                }
            } else {
                Log.e(TAG, "Invalid packet size")
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun calculateCRC(canId: Int, dlc: Int, data: UByteArray): UByte {
        var crc = canId xor dlc
        for (byte in data) {
            crc = crc xor byte.toInt()
        }
        return crc.toUByte()
    }

}

@OptIn(ExperimentalUnsignedTypes::class)
data class CanData(val canId: Int, val dlc: Int, val data: UByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CanData

        if (canId != other.canId) return false
        if (dlc != other.dlc) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = canId
        result = 31 * result + dlc
        result = 31 * result + data.contentHashCode()
        return result
    }
}
