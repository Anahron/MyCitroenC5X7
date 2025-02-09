package ru.newlevel.mycitroenc5x7.app

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.newlevel.mycitroenc5x7.models.MusicModel
import ru.newlevel.mycitroenc5x7.models.NaviModel
import ru.newlevel.mycitroenc5x7.repository.CanRepo

class MusicNotificationListener : NotificationListenerService(), KoinComponent {
    private val canRepo: CanRepo by inject()

    var bufferTitle = ""
    var bufferArtist = ""

    override fun onNotificationPosted(statusBarNotification: StatusBarNotification) {
        val packageName = statusBarNotification.packageName
        Toast.makeText(applicationContext, "Notification Posted: $packageName", Toast.LENGTH_SHORT).show()
        Log.e(TAG, "packageNam: $packageName")
        if (packageName == "ru.yandex.music") {
            val notification = statusBarNotification.notification
            val extras = notification.extras
            val title = extras.getString("android.title")
            val artist = extras.getString("android.text")
            val progress = extras.getString("android.progress")
            val progressMax = extras.getString("android.progressMax")
            Log.e(TAG, "Notification Posted: Title: $title, Artist: $artist")
            Log.e(TAG, "packageNam: $packageName")
            Log.e(TAG, "progress: $progress, progressMax: $progressMax")
            if (title != bufferTitle || artist != bufferArtist) {
                canRepo.setMusic(MusicModel(title = title, artist = artist, progress = progress, progressMax = progressMax))
                bufferTitle = title.toString()
                bufferArtist = artist.toString()
            }
        }
        if (packageName == "ru.yandex.yandexnavi"){
            val notification = statusBarNotification.notification
            val extras = notification.extras
            for (key in extras.keySet()) {
                val value = extras.get(key)
                Log.e(TAG, "Extra key: $key, value: $value")
            }
            val distance = extras.getString("android.title")
            val turn = extras.getString("android.text")
            canRepo.setNavi(NaviModel(turn = turn, distance = distance))
            //  E  Extra key: android.title, value: 20 м
            // android.text, value: налево
            // Extra key: android.text, value: направо
            //Extra key: android.title, value: Навигатор запущен - выкл закрыть подсказки
        }
    }
    override fun onNotificationRemoved(statusBarNotification: StatusBarNotification) {
        // Обработка удаления уведомления
    }
}

