package com.gallery.sdk.views.fragments.photoEdit

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.gallery.sdk.R
import com.gallery.sdk.databinding.FragmentPhotoEditBinding
import com.gallery.sdk.views.fragments.BaseFragment

class PhotoEditFragment : BaseFragment<FragmentPhotoEditBinding>(R.layout.fragment_photo_edit) {
    override val binding: FragmentPhotoEditBinding by lazy {
        FragmentPhotoEditBinding.inflate(
            layoutInflater
        )
    }
    private val navArgs: PhotoEditFragmentArgs by navArgs()
    private lateinit var path: String


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        path = navArgs.path
        binding.toolbar.tvTitle.text = getString(R.string.edit_photo)
    }


    override fun onBackPressed() {

    }
}