package com.whatsappstatus.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.whatsappstatus.DownloadedStatusFragment

class DownloadedContentPagerAdapter(fa: Fragment?) : FragmentStateAdapter(fa!!) {
    override fun createFragment(position: Int) = DownloadedStatusFragment.newInstance(position == 1)

    override fun getItemCount(): Int {
        return 2
    }
}
