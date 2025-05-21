package org.awi.fitness

import android.app.Application
import android.content.Context

class FitnessApp : Application() {
    companion object {
        lateinit var context: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }
} 