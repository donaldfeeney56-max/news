package com.newsapp.app

import android.app.Application
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.DefaultTrackingOptions
import com.amplitude.android.plugins.SessionReplayPlugin
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SportNewsApp : Application() {

    companion object {
        lateinit var amplitude: Amplitude
            private set
    }

    override fun onCreate() {
        super.onCreate()
        amplitude = Amplitude(
            Configuration(
                apiKey = BuildConfig.AMPLITUDE_API_KEY,
                context = applicationContext,
                defaultTracking = DefaultTrackingOptions.ALL
            )
        )

        // Add Session Replay plugin
        amplitude.add(SessionReplayPlugin())
    }
}
