package dev.sukhrob.mytaxi.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import dev.sukhrob.mytaxi.R
import dev.sukhrob.mytaxi.domen.model.UserLocation
import dev.sukhrob.mytaxi.domen.repository.MyTaxiRepository
import dev.sukhrob.mytaxi.utils.NOTIFICATION_CHANNEL_ID
import dev.sukhrob.mytaxi.utils.NOTIFICATION_CHANNEL_NAME
import dev.sukhrob.mytaxi.utils.NOTIFICATION_ID
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class LocationForegroundService : Service(), LocationListener {

    @Inject
    lateinit var repository: MyTaxiRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(1000)
            .setMaxUpdateDelayMillis(1000)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return START_STICKY
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, this, null)

        // Create the notification for the foreground service
        val notification = createNotification()

        // Start the foreground service
        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onLocationChanged(location: Location) {
        scope.launch {
            repository.insertUserLocation(
                UserLocation(
                    Calendar.getInstance().timeInMillis,
                    location.longitude,
                    location.latitude
                )
            )
        }
    }

    private fun createNotification(): Notification {
        // Create the notification channel (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Create the notification builder
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Location Service")
            .setContentText("Getting your location...")
            .setSmallIcon(R.drawable.notification_logo)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)

        // Return the built notification
        return builder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        fusedLocationClient.removeLocationUpdates(this)
    }
}