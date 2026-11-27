package com.alex.assistant

import android.app.Application
import com.alex.assistant.di.AppContainer

class AlexApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
