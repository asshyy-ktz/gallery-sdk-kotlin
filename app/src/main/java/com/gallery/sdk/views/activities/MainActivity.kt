package com.gallery.sdk.views.activities

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.gallery.sdk.R
import com.gallery.sdk.utils.Constants
import com.gallery.sdk.utils.Constants.DEFAULT_RECORDING_LIMIT
import com.gallery.sdk.utils.Constants.DEFAULT_SELECTION_LIMIT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private var maxSelectionLimit = 0
    private var maxVideoRecordLimit = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initParams()
        setStatusBar()

        val bundle = Bundle()
        bundle.putString("maxVideoRecordLimit", maxVideoRecordLimit.toString())
        bundle.putString("maxSelectionLimit", maxSelectionLimit.toString())

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.setGraph(R.navigation.nav_graph,bundle)
//        findNavController(R.id.nav_host_fragment)
//            .setGraph(R.navigation.nav_graph, bundle)

//        if (maxSelectionLimit != null && maxVideoRecordLimit != null) {
//            bundle.putInt("maxSelectionLimit", maxSelectionLimit)
//            bundle.putInt("maxVideoRecordLimit", maxVideoRecordLimit)
//            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//            val navController = navHostFragment.navController
//            navController.navigate(R.id.photoImageCameraFragment, bundle)
////            Navigation.findNavController(this, R.id.nav_host_fragment)
////                .navigate(R.id.photoImageCameraFragment, bundle)
//        }

    }

    private fun initParams() {
        maxSelectionLimit = intent.getIntExtra(
            Constants.BUNDLE_MAX_SELECTION_LIMIT, DEFAULT_SELECTION_LIMIT
        )
        maxVideoRecordLimit = intent.getIntExtra(
            Constants.BUNDLE_MAX_VIDEO_RECORD_LIMIT, DEFAULT_RECORDING_LIMIT
        )
    }

}