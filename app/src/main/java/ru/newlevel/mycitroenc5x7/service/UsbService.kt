package ru.newlevel.mycitroenc5x7.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.newlevel.mycitroenc5x7.MainActivity
import ru.newlevel.mycitroenc5x7.R
import ru.newlevel.mycitroenc5x7.app.ACTION_USB_PERMISSION
import ru.newlevel.mycitroenc5x7.app.CHANEL_GPS
import ru.newlevel.mycitroenc5x7.app.TAG
import ru.newlevel.mycitroenc5x7.models.Mode
import ru.newlevel.mycitroenc5x7.repository.CanRepo

class UsbService : Service(), KoinComponent {

    private lateinit var usbManager: UsbManager
    private var device: UsbDevice? = null
    private var serialPort: UsbSerialDevice? = null
    private var connection: UsbDeviceConnection? = null
    private val canRepo: CanRepo by inject()
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())


    @OptIn(ExperimentalUnsignedTypes::class)
    private val mCallback = UsbSerialInterface.UsbReadCallback { data ->
        serviceScope.launch {
            canRepo.processCanMessage(data)
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            canRepo.putLog("onCreate()")
        }
        usbManager = getSystemService(USB_SERVICE) as UsbManager
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcastReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(broadcastReceiver, filter)
        }
        setupConnection()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            canRepo.putLog("onStartCommand")
        }
        startForegroundService()
        //TODO удалить тестовые вызовы
        serviceScope.launch(Dispatchers.Main) {
            delay(8000)
            showOverlayMessage(R.drawable.alert_not_granted)
        }
        return START_STICKY
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                serviceScope.launch {
                    canRepo.putLog("onReceive -> setupConnection()")
                }
                setupConnection()
            } else if (intent.action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                stopConnection()
            }
        }
    }


    private fun showOverlayMessage(resourceID: Int) {
        Log.e(TAG, "getIsBackground() = ${canRepo.getIsBackground()}")
        if (canRepo.getIsBackground()) {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            val imageView = ImageView(this).apply {
                setImageResource(resourceID)
                //    scaleType = ImageView.ScaleType.CENTER_INSIDE
                scaleX = 0.4f
                scaleY = 0.4f
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.CENTER
            if (canRepo.getIsBackground())
                windowManager.addView(imageView, params)

            CoroutineScope(Dispatchers.Main).launch {
                delay(4000)
                windowManager.removeView(imageView)
            }
        }
    }


    // яркость приборки data[3] в 0x15 проще добавить переменную и сохранить в eeprom
//    0x20 = 0010 0000
//    0x21 = 0010 0001
//    0x22 = 0010 0010
//    0x23 = 0010 0011
//    0x24 = 0010 0100
//    0x25 = 0010 0101
//    0x26 = 0010 0110
//    0x27 = 0010 0111
//    0x28 = 0010 1000
//    0x29 = 0010 1001
//    0x2A = 0010 1010
//    0x2B = 0010 1011
//    0x2C = 0010 1100
//    0x2D = 0010 1101
//    0x2E = 0010 1110
//    0x2F = 0010 1111

    private fun startForegroundService() {
        serviceScope.launch {
            canRepo.putLog("startForegroundService")
        }
        createBackgroundWorkNotificationChannel()
        val notification = createNotification()
        startForeground(1, notification)
        setupSuspensionUpdates()
    }

    private fun setupSuspensionUpdates() {
        CoroutineScope(Dispatchers.Main).launch {
            canRepo.canDataInfoFlow.collect {
                when (it.suspensionState.mode) {
                    Mode.NOT_GRANTED -> {
                        showOverlayMessage(R.drawable.alert_not_granted)
                    }

                    Mode.HIGH -> {
                        showOverlayMessage(R.drawable.alert_max_pos)
                    }

                    Mode.MID -> {
                        showOverlayMessage(R.drawable.alert_mid_pos)
                    }

                    Mode.NORMAL -> {
                        showOverlayMessage(R.drawable.alert_normal_pos)
                    }

                    Mode.LOW -> {
                        showOverlayMessage(R.drawable.alert_low_pos)
                    }

                    Mode.LOW_TO_NORMAL -> {
                        showOverlayMessage(R.drawable.alert_low_to_normal_pos)
                    }

                    Mode.LOW_TO_MED -> {
                        showOverlayMessage(R.drawable.alert_low_to_mid_pos)
                    }

                    Mode.LOW_TO_HIGH -> {
                        showOverlayMessage(R.drawable.alert_low_to_max_pos)
                    }

                    Mode.NORMAL_TO_LOW -> {
                        showOverlayMessage(R.drawable.alert_normal_to_low_pos)
                    }

                    Mode.NORMAL_TO_MED -> {
                        showOverlayMessage(R.drawable.alert_normal_to_mid_pos)
                    }

                    Mode.NORMAL_TO_HIGH -> {
                        showOverlayMessage(R.drawable.alert_normal_to_max_pos)
                    }

                    Mode.MED_TO_LOW -> {
                        showOverlayMessage(R.drawable.alert_mid_to_low_pos)
                    }

                    Mode.MED_TO_NORMAL -> {
                        showOverlayMessage(R.drawable.alert_mid_to_normal_pos)
                    }

                    Mode.MED_TO_HIGH -> {
                        showOverlayMessage(R.drawable.alert_mid_to_max_pos)
                    }

                    Mode.HIGH_TO_LOW -> {
                        showOverlayMessage(R.drawable.alert_max_to_low_pos)
                    }

                    Mode.HIGH_TO_NORMAL -> {
                        showOverlayMessage(R.drawable.alert_max_to_normal_pos)
                    }

                    Mode.HIGH_TO_MED -> {
                        showOverlayMessage(R.drawable.alert_max_to_mid_pos)
                    }
                }
            }
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, CHANEL_GPS)

        return builder.setContentTitle("CAN Service").setContentText("Collecting data...")
            .setSmallIcon(R.drawable.img)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.img))
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun setupConnection() {
        serviceScope.launch {
            canRepo.putLog("setupConnection()")
        }
        val usbDevices = usbManager.deviceList
        if (usbDevices.isNotEmpty()) {
            usbDevices.forEach { (_, usbDevice) ->
                device = usbDevice
                serviceScope.launch {
                    canRepo.putLog("deviceVID = ${device?.vendorId}")
                }
                val deviceVID = device?.vendorId
                if (deviceVID == 6790) { // Arduino Vendor ID
                    if (usbManager.hasPermission(device)) {
                        connection = usbManager.openDevice(device)
                        serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection)
                        serialPort?.let {
                            it.setBaudRate(9600)
                            it.setDataBits(UsbSerialInterface.DATA_BITS_8)
                            it.setStopBits(UsbSerialInterface.STOP_BITS_1)
                            it.setParity(UsbSerialInterface.PARITY_NONE)
                            it.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                            it.open()
                            if (it.open()) {
                                it.read(mCallback)
                                serviceScope.launch {
                                    canRepo.putLog("Serial Connection Opened!")
                                }
                            } else {
                                serviceScope.launch {
                                    canRepo.putLog("Error: Cannot open serial connection.")
                                }
                            }
                        }
                    } else {
                        val pi = PendingIntent.getBroadcast(
                            this,
                            0,
                            Intent(ACTION_USB_PERMISSION),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                        usbManager.requestPermission(device, pi)
                    }
                }
            }
        }
    }

    private fun stopConnection() {
        serialPort?.close()
        serviceScope.launch {
            canRepo.putLog("Serial Connection Closed!")
        }
    }


    private fun createBackgroundWorkNotificationChannel() {
        Log.e(TAG, "createBackgroundWorkNotificationChannel()")
        val channel = NotificationChannel(
            CHANEL_GPS,
            "CAN Service",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Collecting data from car"
            enableVibration(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        Log.e(TAG, "Background Work notification channel created")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
