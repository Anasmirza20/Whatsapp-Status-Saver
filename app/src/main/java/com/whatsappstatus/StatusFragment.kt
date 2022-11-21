package com.whatsappstatus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.whatsapp.reelsfiz.R
import com.whatsappstatus.adapter.ShimmerAdapter
import com.whatsappstatus.adapter.StatusAdapter
import com.whatsappstatus.adapter.StatusCallbacks
import com.whatsapp.reelsfiz.databinding.FragmentStatusBinding
import com.whatsappstatus.util.AdsID
import com.whatsappstatus.util.DownloadUtils
import com.whatsappstatus.util.Extensions.showIndefiniteSnackBarWithAction
import com.whatsappstatus.util.Utils
import com.whatsappstatus.viewmodel.HomeViewModel
import com.whatsappstatus.viewmodel.WhatsappViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.lang.String

@AndroidEntryPoint
class StatusFragment : Fragment(R.layout.fragment_status) {

    private lateinit var binding: FragmentStatusBinding
    private var fromImage: Boolean? = false
    private lateinit var statusAdapter: StatusAdapter
    private var downloadPosition = RecyclerView.NO_POSITION
    private var downloadedUri: Uri? = null
    private val viewModel by navGraphViewModels<WhatsappViewModel>(R.id.whatsapp_nav) {
        defaultViewModelProviderFactory
    }
    private val homeViewModel by activityViewModels<HomeViewModel>()


    private var mRewardedAd: RewardedAd? = null
    private var TAG = "MainActivity"

    companion object {
        const val FROM = "from"
        fun newInstance(fromImage: Boolean): StatusFragment {
            val fragment = StatusFragment()
            fragment.arguments = bundleOf(FROM to fromImage)
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStatusBinding.bind(view)
        fromImage = arguments?.getBoolean(FROM)
        setRecyclerView()
        setObservers()
        loadRewardedAd()
    }

    private fun setRecyclerView() {
        statusAdapter = StatusAdapter(object : StatusCallbacks {
            override fun onClick(position: Int) {
                if (position != RecyclerView.NO_POSITION)
                    homeViewModel.currentVideoUri.value =
                        if (fromImage == true) viewModel.statusImages.value?.get(position)?.uri else viewModel.statusVideos.value?.get(position)?.uri
                val fragment = WhatsappPreviewFragment.newInstance(fromImage == true)
                homeViewModel.whatsappPreviewDialog = fragment
                homeViewModel.isDialogVisible.value = true
                childFragmentManager.let { it1 ->
                    Utils.showFullScreenDialog(
                        it1,
                        fragment
                    )
                }
            }

            override fun onDownloadClick(position: Int) {
                if (position != RecyclerView.NO_POSITION)
                    downloadPosition = position
                showRewardedAd()
            }

        })
        binding.recyclerView.also {
            Utils.initGridRecyclerview(it, context, false, 3)
            it.adapter = ShimmerAdapter((1..9).toList())
        }
    }

    private fun saveItemInLocal(onErrorLoadingAd: Boolean = false) {
        (if (fromImage == true) viewModel.statusImages.value?.get(downloadPosition) else viewModel.statusVideos.value?.get(
            downloadPosition
        ))?.let {
            activity?.let { it1 ->
                viewModel.saveStatus(isImage = fromImage == true, it, it1) {
                    if (it != null) {
                        if (onErrorLoadingAd)
                            showDownloadedSnackBar(it, fromImage == true)
                        else
                            downloadedUri = it
                    }
                }
            }
        }
    }

    private fun setObservers() {
        binding.recyclerView.apply {
            if (fromImage == true)
                viewModel.statusImages.observe(viewLifecycleOwner) {
                    if (adapter !is StatusAdapter)
                        adapter = statusAdapter
                    if (it != null) {
                        statusAdapter.submitList(it)
                    }
                }
            else viewModel.statusVideos.observe(viewLifecycleOwner) {
                if (adapter !is StatusAdapter)
                    adapter = statusAdapter
                if (it != null) {
                    statusAdapter.submitList(it)
                }
            }
        }

    }


    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        context?.let {
            RewardedAd.load(it, AdsID.REWARDED_AD_ID, adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d(TAG, adError.toString())
                        mRewardedAd = null
                    }

                    override fun onAdLoaded(rewardedAd: RewardedAd) {
                        Log.d(TAG, "Ad was loaded.")
                        mRewardedAd = rewardedAd
                        setAdCallbacks()
                    }
                })
        }
    }

    private fun setAdCallbacks() {
        mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                loadRewardedAd()
                downloadedUri?.let { showDownloadedSnackBar(it, fromImage == true) }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.")
                loadRewardedAd()
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")

            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.")
            }

        }
    }

    private fun showRewardedAd() {
        if (mRewardedAd != null) {
            activity?.let {
                mRewardedAd?.show(it) { rewardItem ->
                    loadRewardedAd()
                    onUserEarnedReward(rewardItem)
                }
            }
        } else {
            saveItemInLocal(true)
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
        }
    }

    private fun onUserEarnedReward(rewardItem: RewardItem) {
        saveItemInLocal()
        Log.d(TAG, "User earned the reward.")
    }


    private fun showDownloadedSnackBar(path: Uri, isImage: Boolean) {
        runBlocking {
            delay(500)
            kotlin.runCatching {
                val realUri = if (path.toString().startsWith("content")) context?.let { DownloadUtils.getRealPathFromUri(it, path) } else if (path.toString().startsWith("file:///")) path.toString().replace("file:///", "") else path.toString()
                val file = realUri?.let { File(it) }
                binding.root.showIndefiniteSnackBarWithAction(String.format(getString(R.string.file_saved), file?.path), getString(R.string.tap_to_view)) {
                    try {
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        context?.let {
                            val uri = file?.let { it1 -> FileProvider.getUriForFile(it, it.applicationContext.packageName + ".fileprovider", it1) }
                            intent.setDataAndType(
                                uri.toString().replace("%2520", "%20").toUri(), if (isImage) "image/*" else "video/*"
                            )
                            // content://com.whatsappstatus.fileprovider/external_files/Pictures/Reelsfiz%20images/1668967068917.jpg   12
                            // content://com.whatsappstatus.fileprovider/external_files/Pictures/Reelsfiz%2520images/1668967414581.jpg 8
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            it.startActivity(intent)
                        }

                    } catch (e: Exception) {
                        Utils.logException(e)
                    }
                }
            }.onFailure {
                Utils.logException(it)
            }
        }
    }

}