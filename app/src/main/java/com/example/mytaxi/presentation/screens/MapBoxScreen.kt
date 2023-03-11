package com.example.mytaxi.presentation.screens

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.mytaxi.R
import com.example.mytaxi.databinding.ScreenMapBoxBinding
import com.example.mytaxi.presentation.viewmodel.MainViewModel
import com.example.mytaxi.service.LocationForegroundService
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapBoxScreen : Fragment(R.layout.screen_map_box) {

    private val binding by viewBinding(ScreenMapBoxBinding::bind)
    private val viewModel: MainViewModel by viewModels()

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private var cameraState: CameraState? = null

    private val listener = OnCameraChangeListener {
        cameraState = mapboxMap.cameraState
    }

    private lateinit var annotationApi: AnnotationPlugin
    private lateinit var pointAnnotationManager: PointAnnotationManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.mapView
        mapView.compass.enabled = false
        mapView.scalebar.enabled = false

        mapboxMap = mapView.getMapboxMap()
        mapboxMap.addOnCameraChangeListener(listener)

        annotationApi = mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()

        observeLatestLocation()
        setMapTheme()

    }

    private fun observeLatestLocation() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.latestLocation.collect {
                if (it.longitude != -1.0) {
                    addAnnotationToMap(it.longitude, it.latitude)
                }
            }
        }
    }

    private fun addAnnotationToMap(lng: Double, lat: Double) {

        bitmapFromDrawableRes(requireContext(), R.drawable.ic_car)?.let {
            pointAnnotationManager.deleteAll()

            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(lng, lat))
                .withIconImage(it)
            pointAnnotationManager.create(pointAnnotationOptions)
        }

        val cameraPosition = if (cameraState?.center?.longitude() == 0.0) {
            CameraOptions.Builder().zoom(17.0)
                .center(Point.fromLngLat(lng, lat))
                .build()
        } else {
            CameraOptions.Builder().zoom(cameraState!!.zoom)
                .center(cameraState!!.center)
                .build()
        }

        // set camera position
        //mapView.getMapboxMap().setCamera(cameraPosition)
        mapboxMap.setCamera(cameraPosition)
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    private fun setMapTheme() {
        if (isUsingNightModeResources()) {
            binding.mapView.getMapboxMap().loadStyleUri(Style.DARK)
            binding.ivOrders.setBackgroundResource(R.drawable.shape_category_night)
            binding.ivFrieze.setBackgroundResource(R.drawable.shape_category_night)
            binding.ivTariff.setBackgroundResource(R.drawable.shape_category_night)
        } else {
            binding.mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
            binding.ivOrders.setBackgroundResource(R.drawable.shape_category)
            binding.ivFrieze.setBackgroundResource(R.drawable.shape_category)
            binding.ivTariff.setBackgroundResource(R.drawable.shape_category)
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

    override fun onDestroy() {
        super.onDestroy()
        mapboxMap.removeOnCameraChangeListener(listener)
    }

}