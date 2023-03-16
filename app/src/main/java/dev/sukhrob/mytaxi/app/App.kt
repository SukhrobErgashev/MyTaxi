package dev.sukhrob.mytaxi.app

import android.app.Application
import com.mapbox.mapboxsdk.Mapbox
import dagger.hilt.android.HiltAndroidApp
import dev.sukhrob.mytaxi.R

@HiltAndroidApp
class App: Application() {

    override fun onCreate() {
        super.onCreate()
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
    }
}