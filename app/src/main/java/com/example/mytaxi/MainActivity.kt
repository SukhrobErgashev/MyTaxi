package com.example.mytaxi

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.mytaxi.databinding.ActivityMainBinding
import com.example.mytaxi.presentation.adapter.ViewPagerAdapter
import com.example.mytaxi.service.LocationForegroundService
import com.example.mytaxi.utils.LOCATION_PERMISSION_REQUEST_CODE
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint


val tabs = arrayOf(
    "mapbox",
    "tracker"
)

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var adapter: ViewPagerAdapter
    private lateinit var locationManager: LocationManager

    private val viewBinding: ActivityMainBinding by viewBinding(R.id.mainRoot)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestLocationPermission()
        adapter = ViewPagerAdapter(this)

        viewBinding.viewPager2.adapter = adapter
        viewBinding.viewPager2.isUserInputEnabled = false

        TabLayoutMediator(
            findViewById(R.id.tabLayout),
            findViewById(R.id.viewPager2)
        ) { tab, position ->
            tab.text = tabs[position]
        }.attach()
    }

    private fun startLocationService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, LocationForegroundService::class.java))
        } else {
            startService(Intent(this, LocationForegroundService::class.java))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Location permission is already granted
            // You can start using the location services
            startLocationService()
        } else {
            // Location permission is not granted
            // Request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location permission granted
                    // You can start using the location services
                    startLocationService()
                } else {
                    // Location permission denied
                    // You can't use the location services
                    // ...
                }
                return
            }
        }
    }
}
