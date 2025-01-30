package ru.newlevel.mycitroenc5x7.repository

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.newlevel.mycitroenc5x7.app.TAG
import ru.newlevel.mycitroenc5x7.models.CanInfoModel
import ru.newlevel.mycitroenc5x7.models.MusicModel


class CanRepo(private val canUtils: CanUtils, private val coroutineScope: CoroutineScope) {

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
    private val _musicFlow = MutableSharedFlow<List<ByteArray>>()
    val musicFlow : SharedFlow<List<ByteArray>> = _musicFlow

//    init {
//        coroutineScope.launch {
//            while (true) {
//                delay(8000)  // Задержка в 8 секунд
////                val resultDecode1 = canUtils.encodeTextForCAN("asd.mp3")
////                val modifiedResultDecode1 = resultDecode1.map { packet ->
////                    val newPacket = packet.toMutableList()
////                    newPacket.add(0, newPacket.size.toByte())
////                    newPacket.add(0, (0xFF).toByte())
////                    while (newPacket.size < 10) {
////                        newPacket.add(0x00.toByte())  // Добавляем 0x00 в конец
////                    }
////                    newPacket.toByteArray()
////                }
////                _musicFlow.emit(modifiedResultDecode1)
////                delay(150)
//                val resultDecode = canUtils.encodeTextForCAN("Получилось, епта")
//                val modifiedResultDecode = resultDecode.map { packet ->
//                    val newPacket = packet.toMutableList()
//                    newPacket.add(0, newPacket.size.toByte())
//                    newPacket.add(0, (0xFF).toByte())
//                    while (newPacket.size < 10) {
//                        newPacket.add(0x00.toByte())  // Добавляем 0x00 в конец
//                    }
//                    newPacket.toByteArray()
//                }
//                _musicFlow.emit(modifiedResultDecode)
//            }
//        }
//    }

    fun removeSpecialCharacters(input: String): String {
        return input.replace("[^\\w\\s]".toRegex(), "").trim()
    }
    @OptIn(ExperimentalUnsignedTypes::class)
    fun setMusic(musicModel: MusicModel) {
        coroutineScope.launch {
            val resultText = removeSpecialCharacters(musicModel.artist.toString()) + " " + removeSpecialCharacters(musicModel.title.toString())
            val resultDecode = canUtils.encodeTextForCAN(resultText)
            val modifiedResultDecode = resultDecode.map { packet ->
                val newPacket = packet.toMutableList()
                newPacket.add(0, newPacket.size.toByte())
                newPacket.add(0, (0xFF).toByte())
                while (newPacket.size < 10) {
                    newPacket.add(0x00.toByte())  // Добавляем 0x00 в конец
                }
                newPacket.toByteArray()
            }
            _musicFlow.emit(modifiedResultDecode)
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun setToBackground(isBackground: Boolean) {
        backgroundState.value = isBackground
    }

    fun getIsBackground(): Boolean = backgroundState.value

    @OptIn(ExperimentalUnsignedTypes::class)
    suspend fun processCanMessage(data: ByteArray) {
        try {
            // Преобразуем байтовый массив в строку, как она передается с устройства
            val packetString = data.decodeToString()
            // Проверяем, соответствует ли строка формату "FRAME:ID=123:LEN=4:DE:34:56:78"
            val regex = """ID=([0-9A-Fa-f]+):LEN=(\d+):((?:[0-9A-Fa-f]{2}:?)+)""".toRegex()
            val matchResult = regex.find(packetString)
            if (matchResult != null) {
                // Извлекаем ID и длину
                val canId = matchResult.groupValues[1].toInt(16)
                val len = matchResult.groupValues[2].toInt()

                // Извлекаем данные
                val dataString = matchResult.groupValues[3] // Должна быть "0E:00:06:00:01:00:00:A0"
                val canData = dataString.trim().split(':').map { it.toInt(16).toByte() }.toByteArray()

                val can = CanData(canId = canId, data = canData.toUByteArray(), dlc = len, time = System.currentTimeMillis())
                // Эмитируем данные
                _canDataFlow.emit(can)
                _canDataInfoFlow.value = canUtils.checkCanId(canData = can, _canDataInfoFlow.value)
            } else {
                Log.e(TAG, "Invalid packet format")
                _logger.emit("Invalid packet = $packetString")
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
data class CanData(val canId: Int, val dlc: Int, val data: UByteArray, val time: Long = 0) {
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
