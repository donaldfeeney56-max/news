package com.newsapp.app

import android.app.Application
import android.util.Log
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.DefaultTrackingOptions
import com.amplitude.android.plugins.SessionReplayPlugin
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SportNewsApp : Application() {

    companion object {
        var amplitude: Amplitude? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        initAmplitude()
    }

    private fun initAmplitude() {
        val apiKey = BuildConfig.AMPLITUDE_API_KEY
        if (apiKey.isBlank()) {
            Log.w("SportNewsApp", "Amplitude API key is missing; analytics disabled.")
            return
        }
        try {
            val instance = Amplitude(
                Configuration(
                    apiKey = apiKey,
                    context = applicationContext,
                    defaultTracking = DefaultTrackingOptions.ALL
                )
            )
            instance.add(SessionReplayPlugin())
            amplitude = instance
        } catch (t: Throwable) {
            Log.e("SportNewsApp", "Failed to initialize Amplitude", t)
        }
    }
}
