package com.gallery.sdk.views.fragments.mediaPreview

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.gallery.sdk.R
import com.gallery.sdk.databinding.FragmentMediaPreviewBinding
import com.gallery.sdk.views.adapter.PreviewViewpagerAdapter
import com.gallery.sdk.views.fragments.BaseFragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.gallery.sdk.extenstions.viewGone
import com.gallery.sdk.interfaces.GenericInterface
import com.gallery.sdk.models.ExoPlayerItem
import com.gallery.sdk.utils.RealPathUtils

class MediaPreviewFragment :
    BaseFragment<FragmentMediaPreviewBinding>(R.layout.fragment_media_preview) {
    private var adapter: PreviewViewpagerAdapter? = null
    private val navArgs: MediaPreviewFragmentArgs by navArgs()
    private lateinit var list: Array<Uri>
    private lateinit var uriList: ArrayList<Uri>
    private val exoPlayerItems = ArrayList<ExoPlayerItem>()
    override val binding: FragmentMediaPreviewBinding by lazy {
        FragmentMediaPreviewBinding.inflate(
            layoutInflater
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list = navArgs.list
        uriList = ArrayList(list.asList())
        binding.toolbar.ivOk.viewGone()
        setViewPager()
    }

    override fun onBackPressed() {

    }

    private fun setViewPager() {
        adapter = PreviewViewpagerAdapter(
            requireContext(),
            uriList,
            object : PreviewViewpagerAdapter.OnVideoPreparedListener {
                override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                    exoPlayerItems.add(exoPlayerItem)
                }
            },
            object : GenericInterface<Uri> {
                override fun data(data: Uri) {
                    var path = RealPathUtils.getRealPath(context, data)
                    val type = requireContext().contentResolver.getType(data)
                    when {
                        type!!.startsWith("image/") -> {
                            val action =
                                MediaPreviewFragmentDirections.actionMediaPreviewFragmentToPhotoEditFragment(
                                    data.path!!
                                )
                            findNavController().navigate(action)
                        }
                        type.startsWith("video/") -> {
                            val action =
                                MediaPreviewFragmentDirections.actionMediaPreviewFragmentToVideoEditFragment(
                                    path!!
                                )
                            findNavController().navigate(action)
                        }
                    }
                }
            }
        )
        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val previousIndex = exoPlayerItems.indexOfFirst { it.exoPlayer.isPlaying }
                if (previousIndex != -1) {
                    val player = exoPlayerItems[previousIndex].exoPlayer
                    player.pause()
                    player.playWhenReady = false
                }
                val newIndex = exoPlayerItems.indexOfFirst { it.position == position }
                if (newIndex != -1) {
                    val player = exoPlayerItems[newIndex].exoPlayer
                    player.playWhenReady = true
                    player.play()
                }
            }
        })
        binding.dotsIndicator.attachToPager(binding.viewPager)
    }

    override fun onPause() {
        super.onPause()
        val index = exoPlayerItems.indexOfFirst { it.position == binding.viewPager.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.pause()
            player.playWhenReady = false
        }
    }

    override fun onResume() {
        super.onResume()
        val index = exoPlayerItems.indexOfFirst { it.position == binding.viewPager.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.playWhenReady = true
            player.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (exoPlayerItems.isNotEmpty()) {
            for (item in exoPlayerItems) {
                val player = item.exoPlayer
                player.stop()
                player.clearMediaItems()
            }
        }
    }
}
