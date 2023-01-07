package com.whatsappstatus

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
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
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.whatsapp.reelsfiz.R
import com.whatsapp.reelsfiz.databinding.ActivityMainBinding
import com.whatsappstatus.util.Utils
import com.whatsappstatus.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private var appUpdateManager: AppUpdateManager? = null
    private val UPDATE_REQUEST_CODE: Int = 121
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
        checkAppUpdates()
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
        appUpdateManager?.unregisterListener(listener)
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


    private fun checkAppUpdates() {
        appUpdateManager = AppUpdateManagerFactory.create(this)

// Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo

// Checks that the platform will allow the specified type of update.
        appUpdateInfoTask?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                // This example applies an immediate update. To apply a flexible update
                // instead, pass in AppUpdateType.FLEXIBLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager?.registerListener(listener)

                appUpdateManager?.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    AppUpdateType.FLEXIBLE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    UPDATE_REQUEST_CODE
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.e("MY_APP", "Update flow failed! Result code: $resultCode")
                // If the update is cancelled or fails,
                // you can request to start the update again.
                checkAppUpdates()
            }
        }
    }

    private val listener = InstallStateUpdatedListener { state ->
        // (Optional) Provide a download progress bar.
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytesToDownload = state.totalBytesToDownload()
            }
            InstallStatus.CANCELED -> {

            }
            InstallStatus.DOWNLOADED -> {
                // After the update is downloaded, show a notification
                // and request user confirmation to restart the app.
                popupSnackBarForCompleteUpdate()
            }
            InstallStatus.FAILED -> {

            }
            InstallStatus.INSTALLED -> {

            }
            InstallStatus.INSTALLING -> {

            }
            InstallStatus.PENDING -> {

            }
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackBarForCompleteUpdate()
            }
        }
    }

    private fun popupSnackBarForCompleteUpdate() {
        Snackbar.make(
            findViewById(R.id.main_layout),
            "An update has just been downloaded.",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") { appUpdateManager?.completeUpdate() }
            show()
        }
    }
}