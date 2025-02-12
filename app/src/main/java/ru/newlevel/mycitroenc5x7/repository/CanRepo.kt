package ru.newlevel.mycitroenc5x7.repository

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.newlevel.mycitroenc5x7.app.TAG
import ru.newlevel.mycitroenc5x7.models.CanInfoModel
import ru.newlevel.mycitroenc5x7.models.MusicModel
import ru.newlevel.mycitroenc5x7.models.NaviModel
import ru.newlevel.mycitroenc5x7.models.NaviServiceModel
import kotlin.math.roundToInt


class CanRepo(private val canUtils: CanUtils, private val coroutineScope: CoroutineScope) {

    private val _canDataFlow = MutableSharedFlow<CanData>()
    val canDataFlow: SharedFlow<CanData> = _canDataFlow

    private val _canDataInfoFlow = MutableStateFlow<CanInfoModel>(CanInfoModel())
    val canDataInfoFlow: StateFlow<CanInfoModel> = _canDataInfoFlow

    private val backgroundState = MutableStateFlow<Boolean>(false)
    private val table = listOf(
        0f, 10f, 20f, 30f, 40f, 50f, 60f, 70f, 80f, 90f, 100f, 150f, 200f, 250f, 300f, 350f,
        400f, 600f, 800f, 1000f, 1200f, 1400f, 1600f, 1800f, 2000f, 2200f, 2400f
    )
    private var musicJob: Job? = null

    private val _logger = MutableSharedFlow<String>()
    val logger: SharedFlow<String> = _logger

    suspend fun putLog(text: String) {
        _logger.emit(text)
    }

    private val _musicFlow = MutableSharedFlow<List<ByteArray>>()
    val musicFlow: SharedFlow<List<ByteArray>> = _musicFlow

    private val _naviFlow = MutableStateFlow(NaviServiceModel())
    val naviFlow: SharedFlow<NaviServiceModel> = _naviFlow

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
//                val resultDecode = canUtils.encodeTextForCAN("Получилось епта")
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
       // return input.trim()
       return input.replace("\\(.*?\\)".toRegex(), "").trim()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun setMusic(musicModel: MusicModel) {
        musicJob?.cancel()
        musicJob = coroutineScope.launch {
            val artist = "★ " + removeSpecialCharacters(musicModel.artist.toString()) + "\n"
            val title = "» " + removeSpecialCharacters(musicModel.title.toString()) + "\n"

            val resultArtist = trimToByteLimit(artist)
            val resultArtistDecode = canUtils.encodeTextForCAN(resultArtist)
            val modifiedResultArtistDecode = resultArtistDecode.map { packet ->
                val newPacket = packet.toMutableList()
                newPacket.add(0, newPacket.size.toByte())
                newPacket.add(0, (0xFF).toByte())
                while (newPacket.size < 10) {
                    newPacket.add(0x00.toByte())
                }
                newPacket.toByteArray()
            }
            val resultTitle = trimToByteLimit(title)
            val resultTitleDecode = canUtils.encodeTextForCAN(resultTitle)
            val modifiedResultTitleDecodeDecode = resultTitleDecode.map { packet ->
                val newPacket = packet.toMutableList()
                newPacket.add(0, newPacket.size.toByte())
                newPacket.add(0, (0xFF).toByte())
                while (newPacket.size < 10) {
                    newPacket.add(0x00.toByte())
                }
                newPacket.toByteArray()
            }
            val texts = listOf(modifiedResultArtistDecode, modifiedResultTitleDecodeDecode) // Список сообщений для отправки

            while (true) {
                for (text in texts) {
                    _musicFlow.emit(text)
                    delay(5000) // Ждем 3 секунды перед следующей отправкой
                }
            }
        }
    }
//    fun setMusic(musicModel: MusicModel) {
//        coroutineScope.launch {
//            val resultText = trimToByteLimit("${removeSpecialCharacters(musicModel.artist.toString())} - ${removeSpecialCharacters(musicModel.title.toString())}")
//            val resultDecode = canUtils.encodeTextForCAN(resultText)
//            val modifiedResultDecode = resultDecode.map { packet ->
//                val newPacket = packet.toMutableList()
//                newPacket.add(0, newPacket.size.toByte())
//                newPacket.add(0, (0xFF).toByte())
//                while (newPacket.size < 10) {
//                    newPacket.add(0x00.toByte())  // Добавляем 0x00 в конец
//                }
//                newPacket.toByteArray()
//            }
//            _musicFlow.emit(modifiedResultDecode)
//        }
//    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun setToBackground(isBackground: Boolean) {
        backgroundState.value = isBackground
    }

    fun trimToByteLimit(text: String, maxBytes: Int = 64): String {
        val bytes = text.toByteArray(Charsets.UTF_8)
        if (bytes.size <= maxBytes) return text // Если строка уже влезает, просто возвращаем

        var endIndex = text.length
        while (text.substring(0, endIndex).toByteArray(Charsets.UTF_8).size > maxBytes) {
            endIndex-- // Уменьшаем длину строки на 1 символ
        }

        return text.substring(0, endIndex) // Возвращаем корректно обрезанную строку
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

    fun setNavi(model: NaviModel) {
        try {
            var resultDistance = 0xBB.toByte()
            var resultTurn = 0xBB.toByte()
            if (model.distance != null) {
                if (model.distance.contains("запущен")) {
                    _naviFlow.value = NaviServiceModel()
                } else {
                    val regex = """(\d+(?:[.,]\d+)?)\s*(м|км)""".toRegex() // Теперь поддерживает запятую и точку
                    val match = regex.find(model.distance)
                    if (match != null) {
                        var value = match.groupValues[1].replace(",", ".").toFloat() // Заменяем запятую на точку перед парсингом
                        val unit = match.groupValues[2] // "м" или "км"
                        if (unit == "км") {
                            value *= 1000
                        }
                        Log.e(TAG, "Расстояние: $value $unit")
                        var closest = table.filter { it >= value.roundToInt() }.minOrNull()
                        resultDistance = when (closest) {
                            0f -> 0xA0.toByte()
                            10f -> 0xA1.toByte()
                            20f -> 0xA2.toByte()
                            30f -> 0xA3.toByte()
                            40f -> 0xA4.toByte()
                            50f -> 0xA5.toByte()
                            60f -> 0xA6.toByte()
                            70f -> 0xA7.toByte()
                            80f -> 0xA8.toByte()
                            90f -> 0xA9.toByte()
                            100f -> 0xAA.toByte()
                            150f -> 0xAB.toByte()
                            200f -> 0xAC.toByte()
                            250f -> 0xAD.toByte()
                            300f -> 0xAE.toByte()
                            350f -> 0xAF.toByte()
                            400f -> 0xB0.toByte()
                            600f -> 0xB1.toByte()
                            800f -> 0xB2.toByte()
                            1000f -> 0xB3.toByte()
                            1200f -> 0xB4.toByte()
                            1400f -> 0xB5.toByte()
                            1600f -> 0xB6.toByte()
                            1800f -> 0xB7.toByte()
                            2000f -> 0xB8.toByte()
                            2200f -> 0xB9.toByte()
                            2400f -> 0xBA.toByte()
                            else -> 0xA0.toByte()
                        }
                    }
                    model.turn?.let {
                        Log.e(TAG, "Поворот: ${model.turn}")
                        if (model.turn.contains("направо", ignoreCase = true) || model.turn.contains("круг", ignoreCase = true) || model.turn.contains("съезд", ignoreCase = true)) {
                            resultTurn = 0xC0.toByte()
                        } else if (model.turn.contains("налево", ignoreCase = true)) {
                            resultTurn = 0xC1.toByte()
                        } else if (model.turn.contains("прямо", ignoreCase = true)) {
                            resultTurn = 0xC2.toByte()
                        } else if (model.turn.contains("камера", ignoreCase = true) && model.turn.contains("перек", ignoreCase = true)) {
                            resultTurn = 0xC4.toByte()
                        } else if (model.turn.contains("камера", ignoreCase = true)) {
                            resultTurn = 0xC3.toByte()
                        }
                    }
                    _naviFlow.value = _naviFlow.value.copy(distance = resultDistance, turn = resultTurn)
                }
            } else {
                _naviFlow.value = _naviFlow.value.copy(distance = resultDistance, turn = resultTurn)
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
