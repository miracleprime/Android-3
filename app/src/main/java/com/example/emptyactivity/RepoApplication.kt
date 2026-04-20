package com.example.emptyactivity

import android.app.Application
import com.example.emptyactivity.data.notifications.FavoritePairNotificationHelper
import com.example.emptyactivity.di.AppContainer

class RepoApplication : Application() {
    val appContainer: AppContainer by lazy {
        AppContainer(this)
    }

    override fun onCreate() {
        super.onCreate()
        FavoritePairNotificationHelper.createChannel(this)
    }
}
