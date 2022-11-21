package com.whatsappstatus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.RecyclerView
import com.whatsapp.reelsfiz.R
import com.whatsappstatus.adapter.StatusAdapter
import com.whatsappstatus.adapter.StatusCallbacks
import com.whatsapp.reelsfiz.databinding.FragmentStatusBinding
import com.whatsappstatus.util.Utils
import com.whatsappstatus.viewmodel.DownloadedViewModel
import com.whatsappstatus.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadedStatusFragment : Fragment(R.layout.fragment_status) {

    private lateinit var binding: FragmentStatusBinding
    private var fromImage: Boolean? = false
    private lateinit var statusAdapter: StatusAdapter
    private val viewModel by navGraphViewModels<DownloadedViewModel>(R.id.download_nav) {
        defaultViewModelProviderFactory
    }
    private val homeViewModel by activityViewModels<HomeViewModel>()


    companion object {
        private const val FROM = "from"
        fun newInstance(fromImage: Boolean): DownloadedStatusFragment {
            val fragment = DownloadedStatusFragment()
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
        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.M until Build.VERSION_CODES.R) {
            requestStoragePermission()
        }
    }

    private fun setRecyclerView() {
        statusAdapter = StatusAdapter(object : StatusCallbacks {
            override fun onClick(position: Int) {
                if (position != RecyclerView.NO_POSITION) {
                    homeViewModel.currentVideoUri.value =
                        if (fromImage == true) viewModel.images.value?.get(position)?.uri else viewModel.videos.value?.get(position)?.uri
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
            }

            override fun onDownloadClick(position: Int) {
                if (position != RecyclerView.NO_POSITION) {
                }
            }

        }, true)
        binding.recyclerView.also {
            Utils.initGridRecyclerview(it, context, false, 3)
            it.adapter = statusAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            viewModel.getDownloadedVideos()
            viewModel. getDownloadedImages()
        }
    }

    private fun setObservers() {
        if (fromImage == true)
            viewModel.images.observe(viewLifecycleOwner) {
                if (it != null) {
                    statusAdapter.submitList(it)
                }
            }
        else viewModel.videos.observe(viewLifecycleOwner) {
            if (it != null) {
                statusAdapter.submitList(it)
            }

        }
    }


    private fun requestStoragePermission() {
        context?.let {
            when {
                ContextCompat.checkSelfPermission(
                    it, Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    it, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    viewModel.getDataIfPermissionGranted()
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
                viewModel.getDataIfPermissionGranted()
            }
        }

    }
}