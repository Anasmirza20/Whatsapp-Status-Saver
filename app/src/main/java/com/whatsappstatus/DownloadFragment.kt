package com.whatsappstatus

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.ads.AdRequest
import com.google.android.material.tabs.TabLayoutMediator
import com.whatsapp.reelsfiz.R
import com.whatsapp.reelsfiz.databinding.FragmentDownloadBinding
import com.whatsappstatus.util.SharedPref
import com.whatsappstatus.viewmodel.HomeViewModel
import com.whatsappstatus.adapter.DownloadedContentPagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class DownloadFragment : Fragment(R.layout.fragment_download) {

    @Inject
    lateinit var sharedPref: SharedPref
    private lateinit var binding: FragmentDownloadBinding
    private val viewModel by activityViewModels<HomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDownloadBinding.bind(view)
        setupViewPager()
        setObservers()
        loadBannerAd()
    }

    private fun setObservers() {
        viewModel.isDialogVisible.observe(viewLifecycleOwner) {
            binding.tabLayout.visibility = if (it) {
                binding.viewPager.isUserInputEnabled = false
                GONE
            } else {
                binding.viewPager.isUserInputEnabled = true
                VISIBLE
            }
            activity?.onBackPressedDispatcher?.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(it) {
                    override fun handleOnBackPressed() {
                        viewModel.whatsappPreviewDialog?.dismiss()
                        super.remove()
                        viewModel.isDialogVisible.value = false
                    }

                })
        }
    }


    private fun setupViewPager() {
        binding.apply {
            val adapter = DownloadedContentPagerAdapter(this@DownloadFragment)
            viewPager.adapter = adapter
            viewPager.offscreenPageLimit = 2
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> {
                        tab.icon =
                            context?.let { ContextCompat.getDrawable(it, R.drawable.ic_video) }
                        "Videos"
                    }
                    else -> {
                        tab.icon =
                            context?.let { ContextCompat.getDrawable(it, R.drawable.ic_image) }
                        "Images"
                    }
                }
            }.attach()
        }
    }
    private fun loadBannerAd() {
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }



}