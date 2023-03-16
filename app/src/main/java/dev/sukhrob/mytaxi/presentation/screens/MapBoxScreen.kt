package dev.sukhrob.mytaxi.presentation.screens

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.kirich1409.viewbindingdelegate.viewBinding
import dev.sukhrob.mytaxi.R
import dev.sukhrob.mytaxi.databinding.ScreenMapBoxBinding
import dev.sukhrob.mytaxi.presentation.viewmodel.MainViewModel
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.utils.BitmapUtils
import dagger.hilt.android.AndroidEntryPoint
import dev.sukhrob.mytaxi.domen.model.UserLocation
import dev.sukhrob.mytaxi.utils.CAR_MARKER
import dev.sukhrob.mytaxi.utils.toLatLng
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapBoxScreen : Fragment(R.layout.screen_map_box), OnMapReadyCallback {

    private val binding by viewBinding(ScreenMapBoxBinding::bind)
    private val viewModel: MainViewModel by viewModels()

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var bitmapIcon: Bitmap

    private lateinit var symbolManager: SymbolManager
    private lateinit var markerIcon: Symbol

    private var counter = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        bitmapIcon = BitmapUtils.getBitmapFromDrawable(
            ResourcesCompat.getDrawable(this.resources, R.drawable.ic_car, null)
        )!!

        setClickListeners()
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        observeLatestLocation()
        setMapTheme()
    }

    private fun setClickListeners() {
        with(binding) {
            cardZoomIn.setOnClickListener {
                zoomIn()
            }
            cardZoomOut.setOnClickListener {
                zoomOut()
            }
            cardCurrentLocation.setOnClickListener {
                showCurrentLocation()
            }
        }
    }

    private fun zoomIn() {
        val position = mapboxMap.cameraPosition
        mapboxMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder(
                    position
                ).zoom(position.zoom + 0.8).build()
            ), 1000
        )
    }

    private fun zoomOut() {
        val position = mapboxMap.cameraPosition
        mapboxMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder(
                    position
                ).zoom(position.zoom - 0.8).build()
            ), 1024
        )
    }

    private fun showCurrentLocation() {
        val locationComponent = mapboxMap.locationComponent
        if (locationComponent.isLocationComponentActivated) {
            val lastLocation: LatLng = viewModel.latestLocation.value.toLatLng()
            lastLocation.let {
                val cameraPosition =
                    CameraPosition.Builder().target(LatLng(lastLocation)).zoom(16.0).build()
                mapboxMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(cameraPosition),
                    500
                )
            }
        }
    }

    private fun observeLatestLocation() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.latestLocation.collect {
                updateCamera(it)
                if (counter == 3) {
                    showCurrentLocation()
                }
                counter++
            }
        }
    }

    private fun updateCamera(userLocation: UserLocation) {
        val camera = CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder()
                .target(LatLng(userLocation.latitude, userLocation.longitude))
                .bearing(userLocation.bearing)
                .build()
        )

        mapboxMap.let {
            if (::markerIcon.isInitialized) {
                mapboxMap.animateCamera(camera)
                val anim = ObjectAnimator.ofObject(
                    latLngEvaluator,
                    markerIcon.latLng,
                    LatLng(userLocation.latitude, userLocation.longitude)
                ).setDuration(500L)
                anim.addUpdateListener { valueAnimator ->
                    markerIcon.latLng = valueAnimator.animatedValue as LatLng
                    symbolManager.update(markerIcon)
                }
                anim.start()
            }
        }
    }

    private val latLngEvaluator: TypeEvaluator<LatLng> = object : TypeEvaluator<LatLng> {
        private val latLng = LatLng()
        override fun evaluate(fraction: Float, startValue: LatLng, endValue: LatLng): LatLng {
            latLng.latitude =
                startValue.latitude + (endValue.latitude - startValue.latitude) * fraction
            latLng.longitude =
                startValue.longitude + (endValue.longitude - startValue.longitude) * fraction
            return latLng
        }
    }

    private fun isUsingNightModeResources(): Boolean {
        return when (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

    private fun setMapTheme() {
        mapboxMap.setStyle(
            if (isUsingNightModeResources()) Style.DARK else Style.MAPBOX_STREETS
        ) { style ->
            mapboxMap.uiSettings.apply {
                isCompassEnabled = false
                isLogoEnabled = false
                isAttributionEnabled = false
            }
            setLocationComponent(style)
            style.addImage(CAR_MARKER, bitmapIcon)
            symbolManager = SymbolManager(mapView, mapboxMap, style).apply {
                iconAllowOverlap = true
                iconIgnorePlacement = true
            }
            markerIcon =
                symbolManager.create(
                    SymbolOptions().withLatLng(viewModel.latestLocation.value.toLatLng())
                        .withIconImage(CAR_MARKER).withDraggable(false)
                )
        }
    }

    private fun setLocationComponent(loadedMapStyle: Style) {
        val customLocationComponentOptions =
            LocationComponentOptions.builder(requireContext()).build()
        val locationComponentActivationOptions =
            LocationComponentActivationOptions.builder(requireContext(), loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build()
        mapboxMap.locationComponent.apply {
            activateLocationComponent(locationComponentActivationOptions)
            cameraMode = CameraMode.TRACKING
            renderMode = RenderMode.COMPASS
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}