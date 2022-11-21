package com.whatsappstatus.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.whatsappstatus.StatusFragment

class WhatsappPagerAdapter(fa: Fragment?) : FragmentStateAdapter(fa!!) {
    override fun createFragment(position: Int) = StatusFragment.newInstance(position == 1)

    override fun getItemCount(): Int {
        return 2
    }
}
