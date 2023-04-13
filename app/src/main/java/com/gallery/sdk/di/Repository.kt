package com.gallery.sdk.di

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import com.gallery.sdk.models.VideoRecordModel
import com.gallery.sdk.interfaces.GenericInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class Repository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    @ApplicationContext private val mContext: Context
) {
    suspend fun captureImage(
        localImageCapture: ImageCapture,
        outputDirectory: String,
        metadata: ImageCapture.Metadata,
        successCallBack: GenericInterface<ImageCapture.OutputFileResults>,
        failureCallBack: GenericInterface<String>
    ) {
        remoteDataSource.captureImage(
            localImageCapture,
            outputDirectory,
            metadata,
            object : GenericInterface<ImageCapture.OutputFileResults> {
                override fun onSuccess(data: ImageCapture.OutputFileResults) {
                    successCallBack.onSuccess(data)
                }
            },
            object : GenericInterface<String> {
                override fun onFailure(data: String) {
                    failureCallBack.onFailure(data)
                }

            }
        )
    }

    suspend fun recordVideo(
        recording: Recording?,
        videoCapture: VideoCapture<Recorder>?,
        myInterface: GenericInterface<VideoRecordModel>
    ) {
        remoteDataSource.recordVideo(
            recording,
            videoCapture,
            object : GenericInterface<VideoRecordModel> {
                override fun onSuccess(data: VideoRecordModel) {
                    myInterface.onSuccess(data)
                }
            })
    }


}