package dev.sukhrob.mytaxi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sukhrob.mytaxi.domen.model.UserLocation
import dev.sukhrob.mytaxi.domen.repository.MyTaxiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: MyTaxiRepository) : ViewModel() {

    private var _storedLocationList = MutableStateFlow<List<UserLocation>>(emptyList())
    //val storedLocationList: StateFlow<List<UserLocation>> get() = _storedLocationList

    private var _latestLocation = MutableStateFlow(UserLocation.default())
    val latestLocation: StateFlow<UserLocation> get() = _latestLocation

    init {
        getStoredLocationList()
    }

    private fun getStoredLocationList() = viewModelScope.launch {
        repository.getStoredLocationList().collect {
            if (it.isNotEmpty()) {
                _storedLocationList.value = it
                _latestLocation.value = it.first()
            }
        }
    }

}