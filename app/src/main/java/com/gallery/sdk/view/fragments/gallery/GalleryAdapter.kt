package com.gallery.sdk.view.fragments.gallery

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gallery.sdk.R
import com.gallery.sdk.databinding.ItemGalleryBinding
import com.gallery.sdk.extenstions.viewGone
import com.gallery.sdk.extenstions.viewVisible

class GalleryAdapter(
    private val list: List<GalleryData>, private val mListener: OnGallerySelectionListener
) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(
        val binding: ItemGalleryBinding, val listener: OnGallerySelectionListener
    ) : RecyclerView.ViewHolder(binding?.root!!) {
        fun onBind(position: Int) {
            if (position == 0) {
                Glide.with(context)
                    .load(ContextCompat.getDrawable(context, R.drawable.gallery_camera))
                    .centerCrop().into(binding.iv)
            } else {
                Glide.with(context).load(list[position].uri)
                    .placeholder(R.drawable.gallery_mini_image).into(binding.iv)
            }
            if (list[position].type == 1 && position != 0) {
                binding.ivPlay.viewVisible()
            } else {
                binding.ivPlay.viewGone()
            }
            binding.root.setOnClickListener {
                listener.onMediaSelect(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding: ItemGalleryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.item_gallery, parent, false
        )
        return ViewHolder(binding, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = list.size

    interface OnGallerySelectionListener {
        fun onMediaSelect(position: Int)
    }
}