package com.gallery.sdk.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class EqualSpaceItemDecoration(var mSpaceHeight: Int) : RecyclerView.ItemDecoration() {

    fun EqualSpaceItemDecoration(mSpaceHeight: Int) {
        this.mSpaceHeight = mSpaceHeight
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.bottom = mSpaceHeight
        outRect.top = mSpaceHeight
        outRect.left = mSpaceHeight
        outRect.right = mSpaceHeight
    }
}