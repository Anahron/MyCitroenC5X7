package ru.newlevel.mycitroenc5x7.app

import android.content.Context
import android.content.Intent

class ExternalAppLauncher {
    fun openApp(packageName: String, context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            context.startActivity(intent)
        }
    }
}