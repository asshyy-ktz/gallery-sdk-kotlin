package com.gallery.sdk.viewModels

import android.animation.ObjectAnimator
import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.MediaActionSound
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import androidx.camera.core.*
import androidx.camera.video.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gallery.sdk.di.Repository
import com.gallery.sdk.interfaces.GenericInterface
import com.gallery.sdk.models.VideoRecordModel
import com.gallery.sdk.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repository: Repository,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {
    private var audio: AudioManager =
        getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _capturedImage = MutableLiveData<ImageCapture.OutputFileResults>()
    val captureImage: LiveData<ImageCapture.OutputFileResults> get() = _capturedImage

    private val _videoUri = MutableLiveData<VideoRecordModel>()
    val videoUri: LiveData<VideoRecordModel> get() = _videoUri

    private val _getTime = MutableLiveData<String>()
    val getTime: LiveData<String> get() = _getTime
//    private val _getTime: MutableStateFlow<String> = MutableStateFlow(String())
//    val getTime: StateFlow<String> = _getTime

    private val _getAspectRatio = MutableLiveData<Int>()
    val getAspectRatio: LiveData<Int> get() = _getAspectRatio


    private var timer: Timer? = null

    private var recorderSecondsElapsed = 0.0
    private var _progressAnimator: ObjectAnimator? = null

    suspend fun captureImage(
        localImageCapture: ImageCapture,
        outputDirectory: String,
        metadata: ImageCapture.Metadata
    ) {
        repository.captureImage(
            localImageCapture,
            outputDirectory,
            metadata,
            object : GenericInterface<ImageCapture.OutputFileResults> {
                override fun onSuccess(data: ImageCapture.OutputFileResults) {
                    _capturedImage.value = data
                }
            },
            object : GenericInterface<String> {
                override fun onFailure(data: String) {

                }

            })
    }

    suspend fun recordVideo(recording: Recording?, videoCapture: VideoCapture<Recorder>?) {
        repository.recordVideo(
            recording,
            videoCapture,
            object : GenericInterface<VideoRecordModel> {
                override fun onSuccess(data: VideoRecordModel) {
                    _videoUri.value = data
                }
            })
    }

    fun playShutter() {
        Coroutines.io(this@CameraViewModel) {
            if (audio.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                val sound = MediaActionSound()
                sound.play(MediaActionSound.SHUTTER_CLICK)
            }
        }
    }

    fun startRecordSound() {
        Coroutines.io(this@CameraViewModel) {
            if (audio.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                val sound = MediaActionSound()
                sound.play(MediaActionSound.START_VIDEO_RECORDING)
            }
        }
    }

    fun endRecordSound() {
        Coroutines.io(this@CameraViewModel) {
            if (audio.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                val sound = MediaActionSound()
                sound.play(MediaActionSound.STOP_VIDEO_RECORDING)
            }
        }
    }

    fun formatTime(seconds: Int, minutes: Int, hours: Int): String {
        return String.format(
            "%02d",
            minutes
        ) + " : " + String.format("%02d", seconds)
    }

    /**
     *  Detecting the most suitable aspect ratio for current dimensions
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    fun aspectRatio(width: Int, height: Int) {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - Constants.RATIO_4_3_VALUE) <= abs(previewRatio - Constants.RATIO_16_9_VALUE)) {
            _getAspectRatio.value = AspectRatio.RATIO_4_3
        }
        _getAspectRatio.value = AspectRatio.RATIO_16_9
    }

    fun startTimer(checkRecording: Boolean) {
        stopTimer()
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateTimer(checkRecording)
            }
        }, 0, 1000)
    }

    fun stopTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer!!.purge()
            timer = null
        }
    }

    private fun updateTimer(checkRecording: Boolean) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (checkRecording) {
                recorderSecondsElapsed++
                _getTime.value = 0.getTimerText()

            }
        }, 100)
    }

    fun resetTimer() {
        viewModelScope.launch {
            recorderSecondsElapsed = 0.0
            _getTime.value = 0.getTimerText()
        }
    }

    fun Int.getTimerText(): String {
        val rounded = recorderSecondsElapsed.roundToInt()
        val seconds = rounded % 86400 % 60
        val minutes = rounded % 86400 % 3600 / 60
        val hours = rounded % 86400 / 3600
        return formatTime(seconds, minutes, hours)
    }

    fun setProgressAnimator(progressBar: ProgressBar) {
        _progressAnimator = ObjectAnimator.ofInt(
            progressBar, "progress", 100
        )
        _progressAnimator?.duration = 10000
        _progressAnimator?.repeatCount = Animation.INFINITE
        _progressAnimator?.interpolator = DecelerateInterpolator()
        _progressAnimator?.start()
    }

    fun cancelProgressAnimator() {
        if (_progressAnimator != null) {
            _progressAnimator?.cancel()
            _progressAnimator?.end()
            _progressAnimator?.removeAllListeners()
        }
    }
}