package com.gallery.sdk.views.adapter

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gallery.sdk.R
import com.gallery.sdk.databinding.ItemGalleryBinding
import com.gallery.sdk.extenstions.viewGone
import com.gallery.sdk.extenstions.viewVisible
import com.gallery.sdk.models.GalleryData
import com.gallery.sdk.interfaces.GenericInterface

class GalleryAdapter(
    context: Context,
    private val galleryDataList: ArrayList<GalleryData>,
    private val mListener: GenericInterface<Int>,
    private val maxSelectionLimit: Int,
) : BaseRecyclerViewAdapter<GalleryAdapter.ViewHolder>(context) {

    private val selectedImages = arrayListOf<GalleryData>()

    inner class ViewHolder(
        private val binding: ItemGalleryBinding,
        private val listener: GenericInterface<Int>
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int, model: GalleryData) {
            val selectedIndex = findImageIndex(model, selectedImages)

            binding.apply {
                setupItemForeground(iv, model.isSelected)
                Glide.with(context).load(galleryDataList[position].uri)
                    .placeholder(R.drawable.ic_select_image_black).into(iv)
                if (galleryDataList[position].type == 1) {
                    ivPlay.viewVisible()
                } else {
                    ivPlay.viewGone()
                }

                if (model.isSelected) {
                    relImageView.background = ContextCompat.getDrawable(
                        context,
                        R.drawable.selection_border
                    )
                    textSelectedNumber.viewVisible()
                } else {
                    relImageView.background = ContextCompat.getDrawable(
                        context,
                        R.drawable.selection_border_white
                    )
                    textSelectedNumber.viewGone()
                }

                if (textSelectedNumber.visibility == View.VISIBLE) {
                    textSelectedNumber.text = (selectedIndex + 1).toString()
                    Log.d("djdjdddsnds", (selectedIndex + 1).toString())
                }

                binding.root.setOnClickListener {
                    selectOrRemoveImage(model, position)

                    if (model.isSelected) {
                        model.isSelected = false
                        relImageView.background = ContextCompat.getDrawable(
                            context,
                            R.drawable.selection_border_white
                        )
                        textSelectedNumber.viewGone()
                    } else {
                        if (getSelectedImageCount() < maxSelectionLimit) {
                            model.isSelected = true
                            relImageView.background = ContextCompat.getDrawable(
                                context,
                                R.drawable.selection_border
                            )
                            textSelectedNumber.viewVisible()
                        } else {
                            listener.data(position)
                        }

                    }
                }
            }
        }
    }

    private fun setupItemForeground(view: View, isSelected: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.foreground = if (isSelected) ColorDrawable(
                ContextCompat.getColor(
                    context,
                    R.color.imagepicker_black_alpha_30
                )
            ) else null
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemGalleryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.item_gallery,
            parent, false
        )
        return ViewHolder(binding, mListener)
    }

    override fun onBindViewHolder(
        holder: ViewHolder, position: Int
    ) {
        val model = galleryDataList[position]
        holder.onBind(position, model)
    }

    override fun getItemCount(): Int = galleryDataList.size


    private fun selectOrRemoveImage(image: GalleryData, position: Int) {
        val selectedIndex = findImageIndex(image, selectedImages)
        if (selectedIndex != -1) {
            selectedImages.removeAt(selectedIndex)
            notifyItemChanged(position, ImageUnselected())
            val indexes = findImageIndexes(selectedImages, galleryDataList)
            for (index in indexes) {
                notifyItemChanged(index, ImageSelectedOrUpdated())
            }
        } else {
            if (getSelectedImageCount() < maxSelectionLimit){
                selectedImages.add(image)
            }

            notifyItemChanged(position, ImageSelectedOrUpdated())

        }
    }

    class ImageSelectedOrUpdated

    class ImageUnselected

    fun findImageIndex(image: GalleryData, images: ArrayList<GalleryData>): Int {
        for (i in images.indices) {
            if (images[i].uri == image.uri) {
                return i
            }
        }
        return -1
    }

    private fun findImageIndexes(
        subImages: ArrayList<GalleryData>,
        images: ArrayList<GalleryData>
    ): ArrayList<Int> {
        val indexes = arrayListOf<Int>()
        for (image in subImages) {
            for (i in images.indices) {
                if (images[i].uri == image.uri) {
                    indexes.add(i)
                    break
                }
            }
        }
        return indexes
    }

    fun getSelectedImageCount(): Int {
        return galleryDataList.filter { it.isSelected }.size
    }

    fun getSelectedUris(): MutableList<Uri?> {
        return selectedImages.filter { it.isSelected }.map { it.uri }.toMutableList()
    }

}