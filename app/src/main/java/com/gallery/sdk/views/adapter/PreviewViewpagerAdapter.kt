package com.gallery.sdk.views.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gallery.sdk.R
import com.gallery.sdk.databinding.PreviewViewpageItemBinding
import com.gallery.sdk.extenstions.viewGone
import com.gallery.sdk.extenstions.viewVisible
import com.gallery.sdk.interfaces.GenericInterface
import com.gallery.sdk.models.ExoPlayerItem
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource

class PreviewViewpagerAdapter(
    context: Context,
    private val uriList: ArrayList<Uri>,
    private var videoPreparedListener: OnVideoPreparedListener,
    private val callbackReturnSelectedUri: GenericInterface<Uri>

) : BaseRecyclerViewAdapter<PreviewViewpagerAdapter.PreviewViewHolder>(context) {
    inner class PreviewViewHolder(
        private val binding: PreviewViewpageItemBinding,
        private var videoPreparedListener: OnVideoPreparedListener
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private lateinit var exoPlayer: ExoPlayer
        private lateinit var mediaSource: MediaSource
        fun onBind(uri: Uri) {
            val type = context.contentResolver.getType(uri)
            binding.apply {
                when {
                    type!!.startsWith("image/") -> {
                        binding.pbLoading.viewGone()
                        binding.playerView.viewGone()
                        Glide.with(context).load(uri)
                            .placeholder(R.drawable.ic_select_image_black).into(ivMain)
                    }
                    type.startsWith("video/") -> {
                        binding.playerView.viewVisible()
                        binding.pbLoading.viewVisible()
                        binding.ivMain.viewGone()
                        setVideoPath(uri)
                    }
                }
                tvEdit.setOnClickListener {
                    callbackReturnSelectedUri.data(uri)
                }
            }
        }

        private fun setVideoPath(url: Uri) {
            exoPlayer = ExoPlayer.Builder(context).build()
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
            exoPlayer.seekTo(0)
            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
            val dataSourceFactory = DefaultDataSource.Factory(context)
            mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(url))
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()

            if (absoluteAdapterPosition == 0) {
                exoPlayer.playWhenReady = true
                exoPlayer.play()
            }
            videoPreparedListener.onVideoPrepared(ExoPlayerItem(exoPlayer, absoluteAdapterPosition))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewViewHolder {
        val binding: PreviewViewpageItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.preview_viewpage_item,
            parent, false
        )
        return PreviewViewHolder(binding, videoPreparedListener)
    }


    override fun onBindViewHolder(holder: PreviewViewHolder, position: Int) {
        val uri: Uri = uriList[position]
        holder.onBind(uri)
    }
    override fun getItemCount(): Int {
        return uriList.size
    }
    interface OnVideoPreparedListener {
        fun onVideoPrepared(exoPlayerItem: ExoPlayerItem)
    }


}