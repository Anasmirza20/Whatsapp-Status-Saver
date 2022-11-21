package com.whatsappstatus

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import javax.inject.Inject


class ExoPlayerManager @Inject constructor(private val context: Context?) {

    private var simpleExoPlayer: ExoPlayer? = null
    private var dataSourceFactory: DataSource.Factory? = null

    init {
        dataSourceFactory = context?.let { DefaultDataSource.Factory(it) }
    }

    private fun initializeExoPlayer() {
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory()
        val trackSelector = context?.let { DefaultTrackSelector(it, videoTrackSelectionFactory) }
        trackSelector?.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd())
        val defaultLoadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(1000, 5000, 500, 1000)
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
        simpleExoPlayer = context?.let {
            trackSelector?.let { it1 ->
                ExoPlayer.Builder(it).setTrackSelector(it1)
                    .setLoadControl(defaultLoadControl).build()
            }
        }
        simpleExoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
    }

    fun getExoPlayer(): ExoPlayer? {
        return if (simpleExoPlayer == null) {
            initializeExoPlayer()
            simpleExoPlayer
        } else simpleExoPlayer
    }


    fun buildProgressiveMediaSource(url: Uri): MediaSource? {
        return dataSourceFactory?.let {
            ProgressiveMediaSource.Factory(it)
                .createMediaSource(MediaItem.fromUri(url))
        }
    }

}