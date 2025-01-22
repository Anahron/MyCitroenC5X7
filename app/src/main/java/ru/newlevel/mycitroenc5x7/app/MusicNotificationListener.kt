package ru.newlevel.mycitroenc5x7.app

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.newlevel.mycitroenc5x7.repository.CanRepo

class MusicNotificationListener : NotificationListenerService(), KoinComponent {
    private val canRepo: CanRepo by inject()

    override fun onNotificationPosted(statusBarNotification: StatusBarNotification) {
        val packageName = statusBarNotification.packageName
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
            canRepo.setMusic(title, artist, progress, progressMax)
        }
    }

    override fun onNotificationRemoved(statusBarNotification: StatusBarNotification) {
        // Обработка удаления уведомления
    }
}

