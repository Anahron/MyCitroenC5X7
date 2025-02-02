package ru.newlevel.mycitroenc5x7.repository

import android.util.Log
import ru.newlevel.mycitroenc5x7.app.TAG
import ru.newlevel.mycitroenc5x7.models.Alert
import ru.newlevel.mycitroenc5x7.models.CanInfoModel
import ru.newlevel.mycitroenc5x7.models.CmbColor
import ru.newlevel.mycitroenc5x7.models.CmbGlobalTheme
import ru.newlevel.mycitroenc5x7.models.Importance
import ru.newlevel.mycitroenc5x7.models.Mode
import ru.newlevel.mycitroenc5x7.models.MomentTripData
import ru.newlevel.mycitroenc5x7.models.PersonSettingsStatus
import ru.newlevel.mycitroenc5x7.models.SuspensionState
import ru.newlevel.mycitroenc5x7.models.TripData

@OptIn(ExperimentalUnsignedTypes::class)
class CanUtils {
    private var count: Byte = 0x01
    init {
    //TODO test
        // 328      4 03 10 00 00       тож выводится
        // 328      8 10 08 00 01 24 12 4D 43
        // 328      4 03 10 00 00
        // 328 8 07 28 01 00 02 FF FE 20 это было последним перед сообщением с текстом
    val text = "Получилось, епта"
    val packets = encodeTextForCAN(text)
        val canId = 0x328
        var canDlc: Byte = 0x00
    // Выводим пакеты в читаемом виде
    packets.forEachIndexed { index, packet ->
        canDlc = packet.size.toByte()
        Log.e(TAG, ("canId = " + canId.toString(16) + " can dlc = " + canDlc + "data ${index + 1}: " + packet.joinToString(" ") { String.format("%02X", it) }))
    }

    }
    fun checkCanId(canData: CanData, canInfoModel: CanInfoModel): CanInfoModel {
        when (canData.canId) {
            0x2A1 -> { // + trip1
                return canInfoModel.copy(trip1 = decodeTrip(canData))
            }

            0x261 -> { // + trip2
                return canInfoModel.copy(trip2 = decodeTrip(canData))
            }

            0x221 -> { // * moment trip
                return canInfoModel.copy(momentTrip = decodeTripMomentData(canData))
            }

            0x0E8 -> { // + suspension status
                return canInfoModel.copy(suspensionState = checkSuspensionStatus(canData, canInfoModel.suspensionState))
            }

            0x0F6 -> { // + odo, temp coolant, temp external, ignition, turns light, reverse data[6] Temperature = round(T/2.0 - 39.5) °C (0...250 = -40...+85) или canMsgRcv.data[5] >> 1) - 40 по людвигу
                return canInfoModel.copy(externalTemp = calculateTemperature(canData).toString())
            }

            0x260 -> { // + с кан0 получать Personalization settings status    0x361 Personalization menus availability т.е. только доступность
                return canInfoModel.copy(personSettingsStatus = decodePersonSettingsStatus(canData, canInfoModel.personSettingsStatus))
            }

            0x036 -> { // + brightness from BSI to all
                return canInfoModel.copy(personSettingsStatus = decodeBrightness(canData, canInfoModel.personSettingsStatus))
            }

            0x120 -> { // + alert journal
                return canInfoModel.copy(alerts = decodeAlertsJournal(canData))
            }

            0x227  -> { //  esp
                return canInfoModel.copy(personSettingsStatus = decodeCMBforESPStatus(canData, canInfoModel.personSettingsStatus))
            }
        }
        return canInfoModel
    }

    //byte colorNormal = 0x00; 00 - желтый, 40 - красный, 80 синий
    //byte colorSport = 0x00; 00 - желтый, 40 - красный, 80 синий
    //byte themeLeft = 0x01;
    //byte themeRight = 0x02;
    // global theme normal 0x00 performance 0x04?
    private fun decodeCMBforESPStatus(canData: CanData, personSettingsStatus: PersonSettingsStatus): PersonSettingsStatus {
        return personSettingsStatus.copy(
            espStatus = !checkBit(canData.data[0],4), // esp
            parktronics = !checkBit(canData.data[0],6),
            cmbGlobalTheme = if (checkBit(canData.data[0], 0)) CmbGlobalTheme.THEME_PERFORMANCE else CmbGlobalTheme.THEME_NORMAL,
            colorSport = when(canData.data[4].toInt()) {
                0x00 -> CmbColor.COLOR_YELLOW
                0x40 -> CmbColor.COLOR_RED
                0x80 -> CmbColor.COLOR_BLUE
                else -> {CmbColor.COLOR_YELLOW}
            },
            colorNormal = when(canData.data[3].toInt()) {
                0x00 -> CmbColor.COLOR_YELLOW
                0x40 -> CmbColor.COLOR_RED
                0x80 -> CmbColor.COLOR_BLUE
                else -> {CmbColor.COLOR_YELLOW}
            },
            cmbThemeLeft = when(canData.data[1].toInt()) {
                0x16 -> 1
                0x0C -> 2
                0x17 -> 3
                0xA9 -> 4
                0x14 -> 5
                0x05 -> 6
                else -> {0}
            },
            cmbThemeRight = when(canData.data[2].toInt()) {
                0x5A -> 1
                0x30 -> 2
                0x17 -> 3
                0x29 -> 4
                0x50 -> 5
                0x11 -> 6
                else -> {0}
            },
        )
    }

    private fun decodeAlertsJournal(canData: CanData): List<Alert> {
        val canMsgRcv = canData.data
        val descriptions = mutableListOf<Alert>()
        //     if (!checkBit(canMsgRcv[0], 7) && checkBit(canMsgRcv[0], 6)) {
        if (checkBit(canMsgRcv[1], 7)) descriptions.add(Alert("Низкое давления моторного масла: остановите автомобиль", Importance.STOP))
        if (checkBit(canMsgRcv[1], 6)) descriptions.add(Alert("Высокая температура двигателя: остановите автомобиль", Importance.STOP))
        if (checkBit(canMsgRcv[1], 5)) descriptions.add(Alert("Неисправность системы зарядки: требуется ремонт", Importance.WARNING))
        if (checkBit(canMsgRcv[1], 4)) descriptions.add(Alert("Неисправность тормозной системы: остановите автомобиль", Importance.STOP))
        if (checkBit(canMsgRcv[1], 2)) descriptions.add(Alert("Неисправность рулевого управления: остановите автомобиль", Importance.STOP))
        if (checkBit(canMsgRcv[2], 7)) descriptions.add(Alert("Низкий уровень моторного масла", Importance.WARNING))
        //  if (checkBit(canMsgRcv[2], 5)) descriptions.add("Правая передняя дверь")
        //  if (checkBit(canMsgRcv[2], 4)) descriptions.add("Левая передняя дверь")
        //  if (checkBit(canMsgRcv[2], 3)) descriptions.add("Правая задняя дверь")
        // if (checkBit(canMsgRcv[2], 2)) descriptions.add("Левая задняя дверь")
        if (checkBit(canMsgRcv[3], 6)) descriptions.add(Alert("Неисправность системы ESP/ASR, отремонтируйте автомобиль", Importance.WARNING))
        if (checkBit(canMsgRcv[3], 2)) descriptions.add(Alert("Замените тормозные колодки", Importance.WARNING))
        if (checkBit(canMsgRcv[3], 0)) descriptions.add(Alert("Неисправность подушек безопасности или преднатяжителей ремней", Importance.WARNING))
        if (checkBit(canMsgRcv[4], 5)) descriptions.add(Alert("Неисправность тормозной системы ABS, отремонтируйте автомобиль", Importance.WARNING))
        if (checkBit(canMsgRcv[4], 2)) descriptions.add(Alert("Низкий уровень добавки для сажевого фильтра", Importance.WARNING))
        if (checkBit(canMsgRcv[4], 0)) descriptions.add(Alert("Неисправность подвески, отремонтируйте автомобиль", Importance.WARNING))
        if (checkBit(canMsgRcv[5], 2)) descriptions.add(Alert("Неисправность иммобилайзера", Importance.WARNING))
        if (checkBit(canMsgRcv[6], 1)) descriptions.add(Alert("Долейте уровень жидкости для стеклоомывателя", Importance.INFORMATION))
        //   }

        //   if (checkBit(canMsgRcv[0], 7) && !checkBit(canMsgRcv[0], 6)) {
        if (checkBit(canMsgRcv[1], 4) || checkBit(canMsgRcv[1], 3) || checkBit(canMsgRcv[1], 2) || checkBit(canMsgRcv[1], 1)) descriptions.add(Alert("Прокол: замените или отремонтируйте колесо", Importance.STOP))
        if (checkBit(canMsgRcv[1], 0) || checkBit(canMsgRcv[2], 7) || checkBit(canMsgRcv[2], 6) || checkBit(canMsgRcv[2], 5)) descriptions.add(Alert("Проверьте габаритные огни", Importance.INFORMATION))
        if (checkBit(canMsgRcv[2], 0)) descriptions.add(Alert("Правый стоп-сигнал неисправен", Importance.INFORMATION))
        if (checkBit(canMsgRcv[3], 7)) descriptions.add(Alert("Левый стоп-сигнал неисправен", Importance.INFORMATION))
        if (checkBit(canMsgRcv[3], 6)) descriptions.add(Alert("Правая передняя противотуманная фара неисправна", Importance.INFORMATION))
        if (checkBit(canMsgRcv[3], 5)) descriptions.add(Alert("Левая передняя противотуманная фара неисправна", Importance.INFORMATION))
        if (checkBit(canMsgRcv[3], 2) || checkBit(canMsgRcv[3], 1) || checkBit(canMsgRcv[3], 0) || checkBit(canMsgRcv[4], 7)) descriptions.add(Alert("Проверьте указатели поворота", Importance.INFORMATION))
        if (checkBit(canMsgRcv[4], 6)) descriptions.add(Alert("Правая лампа заднего хода неисправна", Importance.INFORMATION))
        if (checkBit(canMsgRcv[4], 5)) descriptions.add(Alert("Левая лампа заднего хода неисправна", Importance.INFORMATION))
        if (checkBit(canMsgRcv[5], 4)) descriptions.add(Alert("Неисправность системы помощи при парковке", Importance.INFORMATION))
        if (checkBit(canMsgRcv[5], 1)) descriptions.add(Alert("Передняя левая шина: отрегулируйте давление", Importance.WARNING))
        if (checkBit(canMsgRcv[5], 0)) descriptions.add(Alert("Передняя правая шина: отрегулируйте давление", Importance.WARNING))
        if (checkBit(canMsgRcv[6], 7)) descriptions.add(Alert("Задняя правая шина: отрегулируйте давление", Importance.WARNING))
        if (checkBit(canMsgRcv[6], 5)) descriptions.add(Alert("Задняя левая шина: отрегулируйте давление", Importance.WARNING))
        if (checkBit(canMsgRcv[6], 3) || checkBit(canMsgRcv[6], 1)) descriptions.add(Alert("Неисправность очистки выхлопных газов", Importance.WARNING))
        //     }

        // if (checkBit(canMsgRcv[0], 7) && checkBit(canMsgRcv[0], 6)) {
        if (checkBit(canMsgRcv[2], 4)) descriptions.add(Alert("Неисправность стояночного тормоза", Importance.WARNING))
        if (checkBit(canMsgRcv[3], 2)) descriptions.add(Alert("Неисправность АКПП", Importance.WARNING))
        if (checkBit(canMsgRcv[4], 1)) descriptions.add(Alert("Неисправность подвески: ограничьте скорость до 90 км/ч", Importance.WARNING))
        if (checkBit(canMsgRcv[5], 3)) descriptions.add(Alert("Давление в передней левой шине не контролируется", Importance.INFORMATION))
        if (checkBit(canMsgRcv[5], 2)) descriptions.add(Alert("Давление в передней правой шине не контролируется", Importance.INFORMATION))
        if (checkBit(canMsgRcv[5], 1)) descriptions.add(Alert("Давление в задней правой шине не контролируется", Importance.INFORMATION))
        if (checkBit(canMsgRcv[5], 0)) descriptions.add(Alert("Давление в задней левой шине не контролируется", Importance.INFORMATION))
        if (checkBit(canMsgRcv[6], 7)) descriptions.add(Alert("Неисправность подвески: отремонтируйте автомобиль", Importance.WARNING))
        if (checkBit(canMsgRcv[6], 6)) descriptions.add(Alert("Неисправность усилителя рулевого управления: отремонтируйте автомобиль", Importance.WARNING))
        //  }
        return descriptions
    }


    // data[3] brightness  0010 1110 5 бит с конца 0 - день 1 - ночь, 4 бит dark mode, первые 0-3 - яркость - 0-15 "20" > "2F"
    private fun decodeBrightness(canData: CanData, personSettingsStatus: PersonSettingsStatus): PersonSettingsStatus {
        //   val isDay = !checkBit(canData.data[3], 5)
        val isDay = canData.data[0].toInt() < 32

        val cmbBrightness = canData.data[0].toInt() and 0x0F
        return personSettingsStatus.copy(isDay = isDay, cmbBrightness = cmbBrightness)
    }

    //  bitRead(canMsgRcv.data[2], 7)); // Adaptative lighting
    //  bitRead(canMsgRcv.data[2], 5)); // Guide-me home lighting
    //  bitRead(canMsgRcv.data[2], 1)); // Duration Guide-me home lighting (2b)
    //  bitRead(canMsgRcv.data[2], 0)); // Duration Guide-me home lighting (2b)
    //  bitRead(canMsgRcv.data[5], 6)); // AAS - парктроник  +
    //  bitRead(canMsgRcv.data[1], 1)); // Driver Welcome
    //  bitRead(canMsgRcv.data[1], 0)); // Automatic parking brake
    //canMsgSnd.data[3], 7, 1); // DSG Reset ?????


    private fun decodePersonSettingsStatus(canData: CanData, personSettingsStatus: PersonSettingsStatus): PersonSettingsStatus {
        val durationBits = ((canData.data[2].toInt() shr 0) and 0b11) // Маска 0b11 захватывает только 2 бита
        val durationInSeconds = when (durationBits) {
            0b00 -> 0
            0b01 -> 15
            0b10 -> 30
            0b11 -> 45
            else -> 0
        }
        return personSettingsStatus.copy(
            adaptiveLighting = checkBit(canData.data[2], 7),
            guideMeHome = checkBit(canData.data[2], 5),
         //   parktronics = !checkBit(canData.data[5], 6),
            driverWelcome = checkBit(canData.data[1], 1),
            automaticHandbrake = checkBit(canData.data[1], 0),
            durationGuide = durationInSeconds
        )
    }

    fun encodeTextForCAN(inputText: String): List<ByteArray> {
     //   val utf8Bytes = inputText.toByteArray(Charsets.UTF_8) + 0x00.toByte()

        val utf8Bytes: ByteArray = byteArrayOf(
            0x5A, 0x69, 0x76, 0x65, 0x72, 0x74, 0x2C,
            0x4D.toByte(), 0x44.toByte(), 0x65.toByte(), 0x65.toByte(), 0x20.toByte(), 0x2D.toByte(), 0x20.toByte(),
            0xD0.toByte(), 0x94.toByte(), 0xD0.toByte(), 0xB2.toByte(), 0xD1.toByte(), 0x83.toByte(), 0xD1.toByte(),
            0x81.toByte(), 0xD0.toByte(), 0xBC.toByte(), 0xD1.toByte(), 0x8B.toByte(), 0xD1.toByte(), 0x81.toByte(),
            0xD0.toByte(), 0xBB.toByte(), 0xD0.toByte(), 0xB5.toByte(), 0xD0.toByte(), 0xBD.toByte(), 0xD0.toByte(),
            0xBD.toByte(), 0xD0.toByte(), 0xBE.toByte()
        )
        val totalLength = utf8Bytes.size + 6 // длина текста + 6 служебных байт
        val packets = mutableListOf<ByteArray>()
//        // First Frame
//        val firstPacket = ByteArray(8)
//        firstPacket[0] = 0x10.toByte()
//        firstPacket[1] = totalLength.toByte() // Длина сообщения
//        firstPacket[2] = 0x28.toByte() // Служебный байт для блютуса
//        firstPacket[3] = 0x01.toByte()
//        firstPacket[4] = 0x00.toByte()
//        firstPacket[5] = count // counter
//        firstPacket[6] = 0xFF.toByte()
//        firstPacket[7] = 0xFE.toByte()
        // First Frame
        val firstPacket = ByteArray(8)
        firstPacket[0] = 0x10.toByte()
        firstPacket[1] = (totalLength+1).toByte() // Длина сообщения
        firstPacket[2] = 0x28.toByte() // Служебный байт для блютуса
        firstPacket[3] = 0x01.toByte()
        firstPacket[4] = 0x00.toByte()
        firstPacket[5] = 0x8A.toByte()
        firstPacket[6] = 0xFF.toByte()
        firstPacket[7] = 0xFE.toByte()
//        firstPacket[2] = 0x26.toByte() // Служебный байт для usb трек 1/1
//       firstPacket[3] = 0x01.toByte()
//        firstPacket[4] = 0x00.toByte()
//       firstPacket[5] = 0x01.toByte()
//       firstPacket[6] = 0x00.toByte()
//       firstPacket[7] = 0x01.toByte()

    //    utf8Bytes.copyInto(firstPacket, 3, 0, minOf(utf8Bytes.size, 5)) // Первые 5 байтов данных
        packets.add(firstPacket)
        count++
        // Формируем последующие пакеты (Consecutive Frames)
     //   var byteIndex = 5
        var byteIndex = 0
        var frameId = 0x21.toByte() // Первый номер следующего пакета
        while (byteIndex < utf8Bytes.size) {
            val remainingBytes = utf8Bytes.size - byteIndex
            val nextPacketSize = minOf(remainingBytes + 1, 8) // Учитываем размер пакета (с учётом служебных байтов)

            val nextPacket = ByteArray(nextPacketSize)
            nextPacket[0] = frameId // Указатель пакета
            val bytesToCopy = nextPacketSize - 1 // Оставшиеся байты для данных
            utf8Bytes.copyInto(nextPacket, 1, byteIndex, byteIndex + bytesToCopy ) // Копируем данные
            packets.add(nextPacket)

            byteIndex += bytesToCopy
            frameId = (frameId + 1).toByte() // Следующий номер пакета
        }
        return packets
    }

//    00 00 28 40 00 00 00	h	low to normal  28 = 10 1000   40 = 100 0000 +
//    00 00 29 40 00 00 00	h	low to med     29 = 10 1001   40 = 100 0000 +
//    00 00 2B 40 00 00 00	h	low to high    2b = 10 1011   40 = 100 0000 +
//    00 00 2B 00 00 00 00	h	normal to high 2b = 10 1011   00 = 000 0000 +
//    00 00 2B 20 00 00 00	h	med to high    2b = 10 1011   20 = 010 0000 +
//    00 00 29 00 00 00 00	h	normal to med  29 = 10 1001   00-           +

//    00 00 32 00 00 00 00	l	normal to low  32 = 11 0010   00-           +
//    00 00 30 20 00 00 00	l	med to normal  30 = 11 0000   20 = 010 0000 +
//    00 00 32 20 00 00 00	l	med to low     32 = 11 0010   20 = 010 0000 +
//    00 00 34 60 00 00 00	l	high to med    34 = 11 0100   60 = 110 0000 +
//    00 00 30 60 00 00 00	l	high to normal 30 = 11 0000   60 = 110 0000 +
//    00 00 32 60 00 00 00	l	high to low    32 = 11 0010   60 = 110 0000 +
//
//    00 00 38 00 00 00 00		not granted   38 = 11 1000    00-           +

    //    00 00 20 0C 00 00 00		high          20 = 10 0000    0C = 000 1100 +           07 = 0000 0111   6D =  110 11**
//    00 00 20 08 00 00 00		low           20 = 10 0000    08 = 000 1000 +
//    00 00 20 04 00 00 00		mid           20 = 10 0000    04 = 000 0100 +
//    00 00 20 00 00 00 00		normal        20 = 10 0000    00-           +
    private fun checkSuspensionStatus(canData: CanData, suspensionState: SuspensionState): SuspensionState {
        val isSport = checkBit(canData.data[3], 1)

        if (checkBit(canData.data[2], 5)) {
            //    val f28 = checkBit(canData.data[2], 5) && !checkBit(canData.data[2], 4) && checkBit(canData.data[2], 3) && !checkBit(canData.data[2], 2) && !checkBit(canData.data[2], 1) && !checkBit(canData.data[2], 0)
            val f28 = !checkBit(canData.data[2], 4) && checkBit(canData.data[2], 3) && !checkBit(canData.data[2], 2) && !checkBit(canData.data[2], 1) && !checkBit(canData.data[2], 0)
            val f29 = !checkBit(canData.data[2], 4) && checkBit(canData.data[2], 3) && !checkBit(canData.data[2], 2) && !checkBit(canData.data[2], 1) && checkBit(canData.data[2], 0)
            val f2b = !checkBit(canData.data[2], 4) && checkBit(canData.data[2], 3) && !checkBit(canData.data[2], 2) && checkBit(canData.data[2], 1) && checkBit(canData.data[2], 0)
            val f32 = checkBit(canData.data[2], 4) && !checkBit(canData.data[2], 3) && !checkBit(canData.data[2], 2) && checkBit(canData.data[2], 1) && !checkBit(canData.data[2], 0)
            val f30 = checkBit(canData.data[2], 4) && !checkBit(canData.data[2], 3) && !checkBit(canData.data[2], 2) && !checkBit(canData.data[2], 1) && !checkBit(canData.data[2], 0)
            //      val f34 = checkBit(canData.data[2], 4) && !checkBit(canData.data[2], 3) && checkBit(canData.data[2], 2) && !checkBit(canData.data[2], 1) && !checkBit(canData.data[2], 0)
            val f38 = checkBit(canData.data[2], 4) && checkBit(canData.data[2], 3) && !checkBit(canData.data[2], 2) && !checkBit(canData.data[2], 1) && !checkBit(canData.data[2], 0)
            //    val f20 =  !checkBit(canData.data[2], 4) && !checkBit(canData.data[2], 3) && !checkBit(canData.data[2], 2) && !checkBit(canData.data[2], 1) && !checkBit(canData.data[2], 0)
            val f07 = checkBit(canData.data[2], 2) && checkBit(canData.data[2], 1) && checkBit(canData.data[2], 0) // для 4
            val f31 = checkBit(canData.data[2], 0) && checkBit(canData.data[2], 4) && !checkBit(canData.data[2], 3) && !checkBit(canData.data[2], 2) && !checkBit(canData.data[2], 1) // для 4->3
            val s40 = checkBit(canData.data[3], 6) && !checkBit(canData.data[3], 5)
            val s00 = !checkBit(canData.data[3], 6) && !checkBit(canData.data[3], 5)
            val s20 = !checkBit(canData.data[3], 6) && checkBit(canData.data[3], 5)
            val s60 = checkBit(canData.data[3], 6) && checkBit(canData.data[3], 5)

            val s0c = checkBit(canData.data[3], 3) && checkBit(canData.data[3], 2)
            val s08 = checkBit(canData.data[3], 3) && !checkBit(canData.data[3], 2)
            val s04 = !checkBit(canData.data[3], 3) && checkBit(canData.data[3], 2)
            //     val s00n = !checkBit(canData.data[3], 3) && !checkBit(canData.data[3], 2)

            val mode = when {
                f38 -> Mode.NOT_GRANTED
                f07 && s0c -> Mode.HIGH
                //     f20 && s04 -> Mode.MID
                //     f20 && s08 -> Mode.LOW
                //     f20 && s0c -> Mode.HIGH
                s04 -> Mode.MID
                s08 -> Mode.LOW
                f07 && s00 -> Mode.NORMAL
                f28 && s40 -> Mode.LOW_TO_NORMAL
                f29 && s40 -> Mode.LOW_TO_MED
                f2b && s40 -> Mode.LOW_TO_HIGH
                f2b && s00 -> Mode.NORMAL_TO_HIGH
                f2b && s20 -> Mode.MED_TO_HIGH
                f29 && s00 -> Mode.NORMAL_TO_MED
                f32 && s00 -> Mode.NORMAL_TO_LOW
                f30 && s20 -> Mode.MED_TO_NORMAL
                f32 && s20 -> Mode.MED_TO_LOW
                f31 && s60 -> Mode.HIGH_TO_MED
                f30 && s60 -> Mode.HIGH_TO_NORMAL
                f32 && s60 -> Mode.HIGH_TO_LOW
                else -> Mode.NORMAL
            }

            return suspensionState.copy(mode = mode, isSport = isSport)
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
            avgSpeedKmh = litersPer100km, totalDistance = totalDistance, litersPer100 = avgSpeed
        )
    }

    fun calculateTemperature(canData: CanData): Int {
        val byteValue = canData.data[0].toInt()
        val temperature = (byteValue shr 1) - 40 // Расчет температуры
        return temperature
    }

    //на nac только остаток пробега и средний расход + тут же трип бтн byte[0] 4
    fun decodeTripMomentData(canData: CanData): MomentTripData {

        val litersPer100km = (canData.data[1].toInt() shl 8) or canData.data[2].toInt()

        val totalDistance = (canData.data[3].toInt() shl 8) or canData.data[4].toInt() // Rest of run on current fuel level видимо остаток пробега на баке max 2000

        val totalDistanceFinish = (canData.data[5].toInt() shl 8) or canData.data[6].toInt()  // /10 Rest of run to finish  [хз]  max 60000 == 6000.0

        return MomentTripData(
            totalDistanceFinish = totalDistanceFinish, totalDistance = totalDistance, litersPer100km = litersPer100km
        )
    }
}

