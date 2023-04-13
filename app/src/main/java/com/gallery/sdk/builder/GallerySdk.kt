package com.gallery.sdk.builder

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import com.gallery.sdk.utils.Constants
import com.gallery.sdk.views.activities.MainActivity
import java.lang.Exception

class GallerySdk {
    companion object {

        fun with(activity: Activity): Builder {
            return Builder(activity)
        }

        fun with(fragment: Fragment): Builder {
            return Builder(fragment)
        }
    }

    class Builder private constructor() {
        private var activity: Activity? = null
        private var fragment: Fragment? = null
        private var useActivity: Boolean = true
        private var maxSelectionLimit: Int = 1
        private var maxVideoRecordLimit: Int = 1

        internal constructor(activity: Activity) : this() {
            this.activity = activity
            this.useActivity = true
        }

        internal constructor(fragment: Fragment) : this() {
            this.fragment = fragment
            this.useActivity = false
        }

        fun setVideoRecordLimit(maxLimit: Int): Builder {
            maxSelectionLimit = maxLimit
            return this
        }

        fun setSelectionLimit(maxLimit: Int): Builder {
            maxSelectionLimit = maxLimit
            return this
        }

        fun openCamera() {
            val activity = if (useActivity) activity else fragment?.activity
            activity?.let {
                val intent = Intent(activity, MainActivity::class.java)
                intent.putExtra(Constants.BUNDLE_MAX_SELECTION_LIMIT, maxSelectionLimit)
                intent.putExtra(Constants.BUNDLE_MAX_VIDEO_RECORD_LIMIT, maxVideoRecordLimit)
            } ?: kotlin.run {
                throw Exception("Activity/Fragment is not defined to start SDK!")
            }
        }
    }
}