package com.example.mytaxi.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mytaxi.presentation.screens.LocationTrackerScreen
import com.example.mytaxi.presentation.screens.MapBoxScreen
import com.example.mytaxi.utils.TAB_LAYOUT_AMOUNT

class ViewPagerAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount() = TAB_LAYOUT_AMOUNT

    override fun createFragment(position: Int): Fragment {
        val locationTrackerScreen = LocationTrackerScreen()
        val mapBoxScreen = MapBoxScreen()

        return if (position == 0) mapBoxScreen else locationTrackerScreen
    }
}