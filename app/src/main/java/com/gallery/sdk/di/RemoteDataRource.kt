package com.gallery.sdk.di

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import com.gallery.sdk.models.VideoRecordModel
import com.gallery.sdk.utils.Constants.FILENAME
import com.gallery.sdk.utils.Constants.PHOTO_TYPE
import com.gallery.sdk.interfaces.GenericInterface
import com.gallery.sdk.utils.mainExecutor
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    @ApplicationContext private val mContext: Context
) {
    private var mRecording: Recording? = null
    private var videoRecordModel: VideoRecordModel? = null
    private var getVideoUri: GenericInterface<VideoRecordModel>? = null
    private var outputOptions: ImageCapture.OutputFileOptions? = null

    suspend fun captureImage(
        localImageCapture: ImageCapture,
        outputDirectory: String,
        metadata: ImageCapture.Metadata,
        success: GenericInterface<ImageCapture.OutputFileResults>,
        failure: GenericInterface<String>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis())
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, outputDirectory)
            }

            val contentResolver = mContext.contentResolver

            // Create the output uri
            val contentUri =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            outputOptions =
                ImageCapture.OutputFileOptions.Builder(contentResolver, contentUri, contentValues)
                    .setMetadata(metadata).build()


        } else {

            val name = SimpleDateFormat(FILENAME, Locale.US).format(System.currentTimeMillis())
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, PHOTO_TYPE)
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${"gallery-sdk"}")
                }
            }
            // Create output options object which contains file + metadata
            outputOptions = ImageCapture.OutputFileOptions.Builder(
                    mContext.contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ).setMetadata(metadata).build()

        }

        localImageCapture.takePicture(outputOptions!!, // the options needed for the final image
            mContext.mainExecutor(), // the executor, on which the task will run
            object :
                ImageCapture.OnImageSavedCallback { // the callback, about the result of capture process
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    success.onSuccess(outputFileResults)
                }

                override fun onError(exception: ImageCaptureException) {
                    val msg = "Photo capture failed: ${exception.message}"
                    // This function is called if there is an errors during capture process
                    failure.onFailure(msg)
                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show()
                    exception.printStackTrace()
                }
            })
    }


    @SuppressLint("MissingPermission")
    suspend fun recordVideo(
        recording: Recording?,
        videoCapture: VideoCapture<Recorder>?,
        myInterface: GenericInterface<VideoRecordModel>
    ) {
        mRecording = recording
        getVideoUri = myInterface
        val name = "Gallery-SDK-recording-${System.currentTimeMillis()}.mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
        }
        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            mContext.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()
        mRecording =
            videoCapture?.output?.prepareRecording(mContext, mediaStoreOutput)?.withAudioEnabled()
                ?.start(ContextCompat.getMainExecutor(mContext), captureListener)
        videoRecordModel = VideoRecordModel(null, mRecording!!)
        getVideoUri?.onSuccess(videoRecordModel!!)
    }

    /**
     * CaptureEvent listener.
     */
    private val captureListener = Consumer<VideoRecordEvent> { event ->
        if (event is VideoRecordEvent.Finalize) {
            // display the captured video
            if (!event.hasError()) {
                videoRecordModel = VideoRecordModel(event.outputResults.outputUri, null)
                getVideoUri?.onSuccess(videoRecordModel!!)
            } else {
                mRecording?.close()
                mRecording = null
            }
        }
    }

}