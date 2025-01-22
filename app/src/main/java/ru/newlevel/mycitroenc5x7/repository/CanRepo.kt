package ru.newlevel.mycitroenc5x7.repository

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import ru.newlevel.mycitroenc5x7.app.TAG
import ru.newlevel.mycitroenc5x7.models.CanInfoModel


class CanRepo(private val canUtils: CanUtils) {

    private val _canDataFlow = MutableSharedFlow<CanData>()
    val canDataFlow: SharedFlow<CanData> = _canDataFlow

    private val _canDataInfoFlow = MutableStateFlow<CanInfoModel>(CanInfoModel())
    val canDataInfoFlow: StateFlow<CanInfoModel> = _canDataInfoFlow

    private val backgroundState = MutableStateFlow<Boolean>(false)


    private val _logger = MutableSharedFlow<String>()
    val logger: SharedFlow<String> = _logger

    suspend fun putLog(text: String) {
        _logger.emit(text)
    }

    fun setMusic(title: String?, artist: String?, progress: String?, progressMax: String?) {
        //TODO
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun setToBackground(isBackground: Boolean) {
        backgroundState.value = isBackground
//        val uByteArray = ubyteArrayOf(
//            0xFF.toUByte(),
//            0xFF.toUByte(),
//            0xFF.toUByte(),
//            0xFF.toUByte(),
//            0xFF.toUByte(),
//            0xFF.toUByte(),
//            0xFF.toUByte()
//        )
//        _canDataInfoFlow.value = canUtils.checkCanId(canData = CanData(canId = 0x120, dlc = 8, uByteArray), CanInfoModel())
    }

    fun getIsBackground(): Boolean = backgroundState.value

    @OptIn(ExperimentalUnsignedTypes::class)
    suspend fun processCanMessage(data: ByteArray) {
        try {
            // Преобразуем байтовый массив в строку, как она передается с устройства
            val packetString = data.decodeToString()
            _logger.emit("packetString = $packetString")
            // Проверяем, соответствует ли строка формату "FRAME:ID=123:LEN=4:DE:34:56:78"
            val regex = """ID=([0-9A-Fa-f]+):LEN=(\d+):((?:[0-9A-Fa-f]{2}:?)+)""".toRegex()
            val matchResult = regex.find(packetString)
            matchResult?.groupValues?.forEach {
                _logger.emit("Regex match result: ${it}")
            }
            if (matchResult != null) {
                // Извлекаем ID и длину
                val canId = matchResult.groupValues[1].toInt(16)
                val len = matchResult.groupValues[2].toInt()

                // Извлекаем данные
                val dataString = matchResult.groupValues[3] // Должна быть "0E:00:06:00:01:00:00:A0"
                val canData = dataString.trim().split(':').map { it.toInt(16).toByte() }.toByteArray()

                val can = CanData(canId = canId, data = canData.toUByteArray(), dlc = len)
                _logger.emit("can = $can")
                // Эмитируем данные
                _canDataFlow.emit(can)
                _canDataInfoFlow.value = canUtils.checkCanId(canData = can, _canDataInfoFlow.value)

                // Логируем информацию
                Log.e(TAG, "ID: 0x${canId.toString(16).uppercase()}, DLC: $len Data: ${
                    canData.joinToString(", ") { "0x${it.toString(16).uppercase()}" }
                }")
            } else {
                Log.e(TAG, "Invalid packet format")
                _logger.emit("Invalid packet format = $packetString")
            }
        } catch (e: Exception) {
            _logger.emit("Exception")
            Log.e(TAG, e.message.toString())
        }
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
