package com.gallery.sdk.views.fragments.gallery

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.gallery.sdk.R
import com.gallery.sdk.databinding.FragmentCustomGalleryBinding
import com.gallery.sdk.models.GalleryData
import com.gallery.sdk.utils.EqualSpaceItemDecoration
import com.gallery.sdk.interfaces.GenericInterface
import com.gallery.sdk.utils.applyBoomEffect
import com.gallery.sdk.utils.toast
import com.gallery.sdk.viewModels.GalleryViewModel
import com.gallery.sdk.views.adapter.GalleryAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class CustomGalleryFragment(
    private val callBack: GenericInterface<ArrayList<Uri?>>,
    private val maxSelectionLimit:String,
    private val withVideo: Boolean = true,
    private val onlyVideo: Boolean = false
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentCustomGalleryBinding
    private lateinit var galleryAdapter: GalleryAdapter
    private val viewModel: GalleryViewModel by viewModels()
    private var galleryDataList = ArrayList<GalleryData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCustomGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        if (withVideo && !onlyVideo) {
            viewModel.loadAllImagesVideos()
        } else if (onlyVideo) {
            viewModel.loadAllVideos()
        } else {
            viewModel.loadAllImages()
        }
        // do you work now
        setupCollecting()
        initListeners()
    }

    private fun initListeners() {
        binding.apply {
            ivCross.applyBoomEffect()
            ivCross.setOnClickListener {
                dismiss()
            }
            tvDone.setOnClickListener {
                setSelectedImageResult()
            }
        }
    }

    private fun initAdapter() {
        galleryDataList.add(GalleryData(null, 0, -1, false))
        binding.rv.layoutManager = GridLayoutManager(requireContext(), 3)
        galleryAdapter = GalleryAdapter(
            requireContext(),
            galleryDataList,
            object : GenericInterface<Int> {
                override fun data(data: Int) {
                    context?.toast(
                        getString(R.string.select_max_photo_error).format(
                            maxSelectionLimit.toInt()
                        )
                    )
                }
            }, maxSelectionLimit.toInt()
        )
        binding.rv.adapter = galleryAdapter
        binding.rv.addItemDecoration(EqualSpaceItemDecoration(5))
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupCollecting() {
        lifecycleScope.launchWhenStarted {
            viewModel.allImagesFromGallery.collectLatest {
                // simple test, take first image and load it to ImageView using Glide
                if (it.isNotEmpty()) {
                    galleryDataList.clear()
                    galleryDataList.addAll(it)
                    binding.rv.adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private fun setSelectedImageResult() {
        val selectedImages = galleryAdapter.getSelectedUris().toCollection(arrayListOf())
        callBack.data(selectedImages)
        dismiss()
    }

}