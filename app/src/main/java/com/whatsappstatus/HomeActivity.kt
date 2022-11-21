package com.whatsappstatus

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.whatsapp.reelsfiz.R
import com.whatsapp.reelsfiz.databinding.ActivityMainBinding
import com.whatsappstatus.util.Utils
import com.whatsappstatus.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var playerManager: ExoPlayerManager
    private val viewModel by viewModels<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val isWhatsappInstalled = Utils.isAppInstalled(context = this, "com.whatsapp")
        MobileAds.initialize(this) {

        }
        binding.apply {
            if (isWhatsappInstalled) {
                manageNavigation()
                setObserver()
                notFoundView.isVisible = false
                contentView.isVisible = true
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                    requestMultiplePermission.launch(
                        arrayOf(
                            READ_EXTERNAL_STORAGE,
                            WRITE_EXTERNAL_STORAGE
                        )
                    )
            } else {
                loadBannerAd()
                notFoundView.isVisible = true
                contentView.isVisible = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        getExoPlayer()?.release()
    }

    private fun setObserver() {
        viewModel.isDialogVisible.observe(this) {
            binding.bottomNavigation.visibility = if (it) GONE else View.VISIBLE
        }
        viewModel.message.observe(this) {
            if (it != null) {
                Utils.showMessage(binding.root, it)
                viewModel.message.value = null
            }
        }
    }

    private fun manageNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation
            .setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.i("TAG", "manageNavigation: ${navController.backQueue}")
        }
    }


    fun getExoPlayer() = playerManager.getExoPlayer()

    private val requestMultiplePermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        var isGranted = false
        it.forEach { (_, b) ->
            isGranted = b
        }

        if (!isGranted) {
            Toast.makeText(this, "Permission Not Granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBannerAd() {
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }


}