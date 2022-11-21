package com.whatsappstatus

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.navGraphViewModels
import com.google.android.gms.ads.AdRequest
import com.google.android.material.tabs.TabLayoutMediator
import com.whatsapp.reelsfiz.R
import com.whatsappstatus.adapter.WhatsappPagerAdapter
import com.whatsapp.reelsfiz.databinding.FragmentWhatsappBinding
import com.whatsappstatus.util.CommonUserPreferencesSheet
import com.whatsappstatus.util.Constants
import com.whatsappstatus.util.Extensions.showIndefiniteSnackBarWithAction
import com.whatsappstatus.util.SharedPref
import com.whatsappstatus.util.Utils
import com.whatsappstatus.viewmodel.HomeViewModel
import com.whatsappstatus.viewmodel.WhatsappViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.lang.String
import javax.inject.Inject


@AndroidEntryPoint
class WhatsappFragment : Fragment(R.layout.fragment_whatsapp) {

    @Inject
    lateinit var sharedPref: SharedPref
    private lateinit var binding: FragmentWhatsappBinding
    private val viewModel by navGraphViewModels<WhatsappViewModel>(R.id.whatsapp_nav) {
        defaultViewModelProviderFactory
    }
    private val homeViewModel by activityViewModels<HomeViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWhatsappBinding.bind(view)
        setupViewPager()
        setObservers()
        loadBannerAd()
        setListeners()
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    private fun setListeners() {
        binding.allow.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                openStatusDirectory()
            }
        }
    }

    private fun setObservers() {
        homeViewModel.isDialogVisible.observe(viewLifecycleOwner) {
            binding.apply {
                tabLayout.visibility = if (it) {
                    viewPager.isUserInputEnabled = false
                    adView.visibility = GONE
                    GONE
                } else {
                    adView.visibility = VISIBLE
                    viewPager.isUserInputEnabled = true
                    VISIBLE
                }
            }
            activity?.onBackPressedDispatcher?.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(it) {
                    override fun handleOnBackPressed() {
                        homeViewModel.whatsappPreviewDialog?.dismiss()
                        super.remove()
                        homeViewModel.isDialogVisible.value = false
                    }

                })
        }
    }


    private fun setupViewPager() {
        binding.apply {
            val adapter = WhatsappPagerAdapter(this@WhatsappFragment)
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

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val uri = sharedPref.getString(Constants.STATUS_URI_KEY)
            val hasPermission = sharedPref.getBoolean(Constants.STATUS_PERMISSION_GRANTED_KEY) == true && uri.isNullOrEmpty().not()

            manageLayoutVisibility(hasPermission)

            if (hasPermission) {
                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    viewModel.fetchFiles(context, Uri.parse(uri))
                }
            }
        } else if (Build.VERSION.SDK_INT in Build.VERSION_CODES.M until Build.VERSION_CODES.R) {
            requestStoragePermission()
        }
    }

    private fun manageLayoutVisibility(hasPermission: Boolean) {
        binding.contentLayout.isVisible = hasPermission
        binding.permissionLayout.isVisible = !hasPermission
    }

    @RequiresApi(Build.VERSION_CODES.R)
    val result = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val statusPath = it.data?.data?.toString()
            if (statusPath?.endsWith(".Statuses") == true) {
                viewModel.viewModelScope.launch {
                    sharedPref.putString(Constants.STATUS_URI_KEY, statusPath)
                    sharedPref.putBoolean(Constants.STATUS_PERMISSION_GRANTED_KEY, true)
                    manageLayoutVisibility(true)
                    viewModel.manageStatus(statusPath.toUri(), context)
                }
            } else {
                // todo handle wrong path permission (Edge case)
                CommonUserPreferencesSheet().apply {
                    retryCallback {
                        openStatusDirectory()
                    }
                }.show(parentFragmentManager, this::class.simpleName)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun openStatusDirectory() {
        result.launch(context?.let { WhatsappStatusProvider.askPermission(it) })
    }

    private fun loadBannerAd() {
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    private fun requestStoragePermission() {
        context?.let {
            when {
                ContextCompat.checkSelfPermission(
                    it, Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    it, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    manageLayoutVisibility(true)
                    viewModel.getStatusesForAndroid10()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected, and what
                    // features are disabled if it's declined. In this UI, include a
                    // "cancel" or "no thanks" button that lets the user continue
                    // using your app without granting the permission.
//                    showInContextUI(...)
                }
                else -> {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    requestPermissions.launch(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    )
                }
            }
        }
    }

    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        it.forEach { map ->
            if (map.value) {
                manageLayoutVisibility(true)
                viewModel.getStatusesForAndroid10()
            }
        }

    }
}