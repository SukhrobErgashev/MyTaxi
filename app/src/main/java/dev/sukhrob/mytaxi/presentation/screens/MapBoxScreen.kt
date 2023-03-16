package dev.sukhrob.mytaxi.presentation.screens

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.kirich1409.viewbindingdelegate.viewBinding
import dev.sukhrob.mytaxi.R
import dev.sukhrob.mytaxi.databinding.ScreenMapBoxBinding
import dev.sukhrob.mytaxi.presentation.viewmodel.MainViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.scalebar.scalebar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapBoxScreen : Fragment(R.layout.screen_map_box) {

    private val binding by viewBinding(ScreenMapBoxBinding::bind)
    private val viewModel: MainViewModel by viewModels()

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var bitmapIcon: Bitmap

    private var cameraState: CameraState? = null
    private var isFirstEntering: Boolean = true

    private lateinit var pointAnnotationOptions: PointAnnotationOptions

    private val listener = OnCameraChangeListener {
        cameraState = mapboxMap.cameraState
        viewModel.setZoomLevel(cameraState!!.zoom)
        viewModel.setCameraBearing(cameraState!!.bearing)
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
        bitmapIcon = bitmapFromDrawableRes(requireContext(), R.drawable.ic_car)!!

        annotationApi = mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()
        pointAnnotationOptions = PointAnnotationOptions()

        observeLatestLocation()
        observeCameraBearing()

        setMapTheme()
        setClickListeners()

    }

    private fun setClickListeners() {
        with(binding) {
            cardZoomIn.setOnClickListener { viewModel.increaseZoomLevel(); resetCamera() }
            cardZoomOut.setOnClickListener { viewModel.decreaseZoomLevel(); resetCamera() }
            cardCurrentLocation.setOnClickListener {
                val cameraPosition = CameraOptions.Builder()
                    .center(
                        Point.fromLngLat(
                            viewModel.latestLocation.value.longitude,
                            viewModel.latestLocation.value.latitude
                        )
                    )
                    .zoom(viewModel.zoomLevel.value)
                    .build()
                mapboxMap.setCamera(cameraPosition)
            }
        }
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

    private fun observeCameraBearing() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.cameraBearing.collect {
                with(viewModel.latestLocation.value) {
                    if (latitude != -1.0) addAnnotationToMap(longitude, latitude)
                }
            }
        }
    }

    private fun addAnnotationToMap(lng: Double, lat: Double) {
        if (viewModel.iconRotation.value != -0.0) {
            pointAnnotationManager.deleteAll()
            pointAnnotationOptions
                .withPoint(Point.fromLngLat(lng, lat))
                .withIconImage(bitmapIcon)
                .withIconRotate(viewModel.iconRotation.value - viewModel.cameraBearing.value)
                .withIconSize(viewModel.iconSize.value)

            pointAnnotationManager.create(pointAnnotationOptions)
        }
        if (isFirstEntering) initializeCamera(lng, lat) else resetCamera()
    }

    private fun initializeCamera(lng: Double, lat: Double) {
        val cameraPosition = CameraOptions.Builder()
            .center(Point.fromLngLat(lng, lat))
            .zoom(17.0)
            .build()
        mapboxMap.setCamera(cameraPosition)
        cameraState = mapboxMap.cameraState
        isFirstEntering = false
    }

    private fun resetCamera() {
        val cameraPosition = CameraOptions.Builder()
            .center(cameraState?.center)
            .zoom(viewModel.zoomLevel.value)
            .build()
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
            with(binding) {
                mapView.getMapboxMap().loadStyleUri(Style.DARK)
                imageOrders.setBackgroundResource(R.drawable.shape_category_night)
                imageFrieze.setBackgroundResource(R.drawable.shape_category_night)
                imageTariff.setBackgroundResource(R.drawable.shape_category_night)
            }
        } else {
            with(binding) {
                mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
                imageOrders.setBackgroundResource(R.drawable.shape_category)
                imageFrieze.setBackgroundResource(R.drawable.shape_category)
                imageTariff.setBackgroundResource(R.drawable.shape_category)
            }
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