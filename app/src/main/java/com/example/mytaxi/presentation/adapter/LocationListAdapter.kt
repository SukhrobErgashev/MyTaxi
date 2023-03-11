package com.example.mytaxi.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mytaxi.databinding.ItemLocationListBinding
import com.example.mytaxi.domen.model.UserLocation

class LocationListAdapter :
    ListAdapter<UserLocation, LocationListAdapter.LocationListViewHolder>(DiffCallback) {

    inner class LocationListViewHolder(private val binding: ItemLocationListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            val userLocation = getItem(bindingAdapterPosition)
            with(binding) {
                textLong.text = userLocation.longitude.toString()
                textLat.text = userLocation.latitude.toString()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationListViewHolder {
        return LocationListViewHolder(
            ItemLocationListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LocationListViewHolder, position: Int) = holder.bind()

    object DiffCallback : DiffUtil.ItemCallback<UserLocation>() {
        override fun areItemsTheSame(oldItem: UserLocation, newItem: UserLocation): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: UserLocation, newItem: UserLocation): Boolean {
            return oldItem == newItem
        }

    }
}