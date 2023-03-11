package com.example.mytaxi.presentation.screens

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.mytaxi.R
import com.example.mytaxi.databinding.ScreenLocationTrackerBinding
import com.example.mytaxi.presentation.adapter.LocationListAdapter
import com.example.mytaxi.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationTrackerScreen: Fragment(R.layout.screen_location_tracker) {

    private val binding by viewBinding(ScreenLocationTrackerBinding::bind)
    private val viewModel: MainViewModel by viewModels()
    private val adapter by lazy { LocationListAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeLocationList()

    }

    private fun observeLocationList() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.storedLocationList.collect {
                adapter.submitList(it)
            }
        }
    }

    private fun setupRecyclerView() {
        with(binding.rvLocationList) {
            adapter = this@LocationTrackerScreen.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

}