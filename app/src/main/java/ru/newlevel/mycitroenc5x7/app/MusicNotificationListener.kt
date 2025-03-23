package ru.newlevel.mycitroenc5x7.app

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.newlevel.mycitroenc5x7.models.MusicModel
import ru.newlevel.mycitroenc5x7.models.NaviModel
import ru.newlevel.mycitroenc5x7.repository.CanRepo

class MusicNotificationListener : NotificationListenerService(), KoinComponent {
    private val canRepo: CanRepo by inject()

    var bufferTitle = ""
    var bufferArtist = ""

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onNotificationPosted(statusBarNotification: StatusBarNotification) {
        val packageName = statusBarNotification.packageName
        //      Toast.makeText(applicationContext, "Notification Posted: $packageName", Toast.LENGTH_SHORT).show()
        if (packageName == "ru.yandex.music") {
            val notification = statusBarNotification.notification
            val extras = notification.extras
            val title = extras.getString("android.title")
            val artist = extras.getString("android.text")
            val progress = extras.getString("android.progress")
            val progressMax = extras.getString("android.progressMax")
            Log.e(TAG, "Notification Posted: Title: $title, Artist: $artist")
            if (title != bufferTitle || artist != bufferArtist) {
                canRepo.setMusic(MusicModel(title = title, artist = artist, progress = progress, progressMax = progressMax))
                bufferTitle = title.toString()
                bufferArtist = artist.toString()
            }
        }
        if (packageName == "ru.yandex.yandexnavi") {
            val notification = statusBarNotification.notification
            val extras = notification.extras
            val distance = extras.getString("android.title")
            val turn = extras.getString("android.text")
            canRepo.setNavi(NaviModel(turn = turn, distance = distance))
        }
    }


    override fun onNotificationRemoved(statusBarNotification: StatusBarNotification) {
        // Обработка удаления уведомления
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        requestRebind(ComponentName(this, MusicNotificationListener::class.java))
    }
}

class DeviceStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.e(TAG, "DeviceStateReceiver")
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d(TAG, "Device booted. Starting app...")
                startApp(context)
            }
            Intent.ACTION_SCREEN_ON -> {
                Log.d(TAG, "Screen turned on. Starting app...")
                startApp(context)
            }
        }

//        if (intent.action == Intent.ACTION_SCREEN_ON) {
//            Log.e(TAG, "intent.action == Intent.ACTION_SCREEN_ON")
//            requestRebind(ComponentName(context, MusicNotificationListener::class.java))
//        }
    }
    private fun startApp(context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            Log.e(TAG, "Could not find launch intent for package: ${context.packageName}")
        }
    }
}
