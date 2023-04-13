package com.gallery.sdk.views.fragments.videoEdit
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.gallery.sdk.R
import com.gallery.sdk.databinding.FragmentVideoEditBinding
import com.gallery.sdk.views.fragments.BaseFragment
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource

class VideoEditFragment : BaseFragment<FragmentVideoEditBinding>(R.layout.fragment_video_edit) {
    override val binding: FragmentVideoEditBinding by lazy {
        FragmentVideoEditBinding.inflate(
            layoutInflater
        )
    }
    private val navArgs: VideoEditFragmentArgs by navArgs()
    private lateinit var path: String
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSource: MediaSource

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        path = navArgs.path
        binding.toolbar.tvTitle.text = getString(R.string.edit_video)
        setVideoPath(path)
    }


    private fun setVideoPath(url: String) {
        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Toast.makeText(context, "Can't play this video", Toast.LENGTH_SHORT).show()
            }

            @Deprecated("Deprecated in Java")
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_BUFFERING) {
                    binding.pbLoading.visibility = View.VISIBLE
                } else if (playbackState == Player.STATE_READY) {
                    binding.pbLoading.visibility = View.GONE
                }
            }
        })
        binding.playerView.player = exoPlayer
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        val dataSourceFactory = DefaultDataSource.Factory(requireContext())
        mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(url))
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        exoPlayer.play()
    }


    override fun onBackPressed() {

    }

    override fun onResume() {
        super.onResume()
        exoPlayer.playWhenReady = true
        exoPlayer.play()
    }

    override fun onPause() {
        super.onPause()
        exoPlayer.pause()
        exoPlayer.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
    }

}