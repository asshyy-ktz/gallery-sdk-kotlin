package com.gallery.sdk.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.astritveliu.boom.Boom
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.gallery.sdk.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

fun ImageView.loadThumbnail(
    path: String,
    @DrawableRes placeholder: Int = R.drawable.gallery_mini_image,
    @DrawableRes errorDrawable: Int = R.drawable.gallery_mini_image
) {
    val requestOptions = RequestOptions()
    Glide.with(this)
        .load(path)
        .transition(DrawableTransitionOptions.withCrossFade()) //Here a fading animation
        .apply(requestOptions)
        .placeholder(placeholder)
        .into(this)
}

fun View.applyBoomEffect() {
    Boom(this)
}

fun Context.mainExecutor(): Executor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    mainExecutor
} else {
    MainExecutor()
}

fun Context.toast(message: CharSequence) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()


fun ImageButton.toggleButton(
    flag: Boolean, rotationAngle: Float, @DrawableRes firstIcon: Int, @DrawableRes secondIcon: Int,
    action: (Boolean) -> Unit
) {
    if (flag) {
        if (rotationY == 0f) rotationY = rotationAngle
        animate().rotationY(0f).apply {
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    action(!flag)
                }
            })
        }.duration = 200
        GlobalScope.launch(Dispatchers.Main) {
            delay(100)
            setImageResource(firstIcon)
        }
    } else {
        if (rotationY == rotationAngle) rotationY = 0f
        animate().rotationY(rotationAngle).apply {
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    action(!flag)
                }
            })
        }.duration = 200
        GlobalScope.launch(Dispatchers.Main) {
            delay(100)
            setImageResource(secondIcon)
        }
    }
}