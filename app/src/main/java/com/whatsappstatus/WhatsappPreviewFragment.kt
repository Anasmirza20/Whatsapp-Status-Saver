package com.whatsappstatus

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.whatsapp.reelsfiz.R
import com.whatsappstatus.util.CommonMethods
import com.whatsapp.reelsfiz.databinding.FragmentPlayerBinding
import com.whatsappstatus.viewmodel.HomeViewModel

class WhatsappPreviewFragment : DialogFragment(R.layout.fragment_player) {

    private lateinit var binding: FragmentPlayerBinding
    private val viewModel by activityViewModels<HomeViewModel>()
    private var fromImage: Boolean? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPlayerBinding.bind(view)
        fromImage = arguments?.getBoolean(StatusFragment.FROM)
        showMedia()
    }

    private fun showMedia() {
        binding.apply {
            if (fromImage == true) {
                image.visibility = View.VISIBLE
                viewModel.currentVideoUri.value?.let {
                    CommonMethods.loadImage(context, it.toString(), image)
                }
            } else
                (activity as HomeActivity?)?.apply {
                    player.visibility = View.VISIBLE
                    player.player = getExoPlayer()
                    viewModel.currentVideoUri.value?.let {
                        playerManager.buildProgressiveMediaSource(it)?.let { media ->
                            getExoPlayer()?.setMediaSource(media)
                            getExoPlayer()?.prepare()
                            getExoPlayer()?.playWhenReady = true
                        }
                    }
                }
        }
    }

    override fun onStop() {
        super.onStop()
        if ((activity as? HomeActivity)?.getExoPlayer() != null)
            (activity as? HomeActivity)?.getExoPlayer()?.playWhenReady = false
    }

    companion object {
        fun newInstance(from: Boolean) = WhatsappPreviewFragment().apply {
            arguments = bundleOf(StatusFragment.FROM to from)
        }
    }
}