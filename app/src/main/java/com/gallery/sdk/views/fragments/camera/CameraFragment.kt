package com.gallery.sdk.views.fragments.camera

import android.annotation.SuppressLint
import android.net.Uri
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
import androidx.camera.core.ImageCapture.FLASH_MODE_ON
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gallery.sdk.R
import com.gallery.sdk.databinding.FragmentCameraBinding
import com.gallery.sdk.extenstions.viewGone
import com.gallery.sdk.extenstions.viewVisible
import com.gallery.sdk.interfaces.GenericInterface
import com.gallery.sdk.utils.*
import com.gallery.sdk.utils.Constants.KEY_FLASH
import com.gallery.sdk.viewModels.CameraViewModel
import com.gallery.sdk.views.fragments.BaseFragment
import com.gallery.sdk.views.fragments.gallery.CustomGalleryFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ExecutionException
import kotlin.properties.Delegates

@ExperimentalCamera2Interop
@SuppressLint("RestrictedApi")
@AndroidEntryPoint
class CameraFragment : BaseFragment<FragmentCameraBinding>(R.layout.fragment_camera) {
    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    private val handler = Handler(Looper.getMainLooper())
    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null

    // Selector showing which camera is selected (front or back)
    private var hdrCameraSelector: CameraSelector? = null
    private var imageCapture: ImageCapture? = null
    private var recording: Recording? = null
    private var preview: Preview? = null
    private var camera: Camera? = null

    // An instance of a helper function to work with Shared Preferences
    private val prefs by lazy { SharedPrefsManager.newInstance(requireContext()) }

    // Selector showing which flash mode is selected (on, off or auto)
    private var flashMode by Delegates.observable(FLASH_MODE_OFF) { _, _, new ->
        binding.btnFlash.setImageResource(
            when (new) {
                FLASH_MODE_ON -> R.drawable.flash_icon_on
                else -> R.drawable.flash_icon_off
            }
        )
    }

    // A lazy instance of the current fragment's view binding
    override val binding: FragmentCameraBinding by lazy {
        FragmentCameraBinding.inflate(layoutInflater)
    }
    private var mLongPressed = Runnable {
        isLongPressHandlerActivated = true
        startVideo()
    }
    private var displayId = -1
    private var setTime: Int = 0
    private var aspectRatio: Int = 0
    private var isLongPressHandlerActivated = false
    private var isFlashMode = false

    // check recording is off or on
    private var checkRecording = false
    private val viewModel: CameraViewModel by viewModels()

    //    var args = arguments
//    val args by navArgs<CameraFragmentArgs>()
    private val args by navArgs<CameraFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        flashMode = prefs.getInt(KEY_FLASH, FLASH_MODE_OFF)
        setupObservers()
        listeners()
        setVideoTime()
        openBottomSheet()

    }


    @SuppressLint("ClickableViewAccessibility")
    private fun listeners() {
        binding.run {
            btnTakePicture.applyBoomEffect()
            btnTakePicture.setOnTouchListener { _, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        handler.postDelayed(
                            mLongPressed, ViewConfiguration.getLongPressTimeout().toLong()
                        )
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isLongPressHandlerActivated) {
                            stopVideo()
                        } else {
                            takePicture()
                            viewModel.playShutter()
                            handler.removeCallbacks(mLongPressed)
                        }
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        handler.removeCallbacks(mLongPressed)
                        progressBar.viewGone()
                        btnTakePicture.viewVisible()
                    }
                }
                false
            }

            btnFlash.setOnClickListener {
                if (!isFlashMode) {
                    isFlashMode = true
                    closeFlashAndSelect(FLASH_MODE_ON)
                } else {
                    isFlashMode = false
                    closeFlashAndSelect(FLASH_MODE_OFF)
                }
            }
            btnSwitchCamera.setOnClickListener { toggleCamera() }

            ivCross.applyBoomEffect()
            ivCross.setOnClickListener {
                onBackPressed()
            }
        }
    }

    private fun stopVideo() {
        binding.progressBar.viewGone()
        binding.btnTakePicture.viewVisible()
        viewModel.endRecordSound()
        checkRecording = false
        isLongPressHandlerActivated = false
        camera?.cameraControl?.enableTorch(false)
        handler.removeCallbacks(mLongPressed)
        viewModel.cancelProgressAnimator()
        resetTimer()
        viewModel.stopTimer()
        FLASH_MODE_OFF
        if (recording != null) {
            val currentRecording = recording
            if (currentRecording != null) {
                currentRecording.stop()
                recording = null
            }
        }
    }

    private fun openBottomSheet() {
        binding.btnGallery.setOnClickListener {
            val bottomSheet = CustomGalleryFragment(object : GenericInterface<ArrayList<Uri?>> {
                override fun data(data: ArrayList<Uri?>) {
                    val action =
                        CameraFragmentDirections.actionPhotoImageCameraFragmentToMediaPreviewFragment(
                            data.toTypedArray()
                        )

                    findNavController().navigate(action)
                }
            }, args.maxSelectionLimit)
            bottomSheet.show(childFragmentManager, "MyBottomSheetDialog")
        }
    }

    /**
     * Handle progress and record video
     * */
    private fun startVideo() {
        binding.run {
            progressBar.viewVisible()
            timerTxt.viewVisible()
            btnTakePicture.viewGone()
            viewModel.startRecordSound()
            checkRecording = true
            viewModel.startTimer(checkRecording)
//            startTimer()
            if (isFlashMode) {
                toggleFlash()
            }
            binding.progressBar.progress = 0
            viewModel.setProgressAnimator(binding.progressBar)

            lifecycleScope.launch {
                viewModel.recordVideo(recording, videoCapture)
            }
        }
    }

    /**
     * Handle granted permissions
     * */
    override fun onPermissionGranted() {
        // Each time apps is coming to foreground the need permission check is being processed
        binding.viewFinder.let { vf ->
            vf.post {
                // Setting current display ID
                displayId = vf.display.displayId
                Log.d("kdkdkdkd", vf.display.displayId.toString())
                startCamera()
            }
        }
    }

    override fun onBackPressed() {
        requireActivity().finish()
    }

    private fun setGalleryThumbnail(savedUri: Uri?) =
        binding.btnGallery.loadThumbnail(RealPathUtils.getRealPath(context, savedUri))

    /**
     * Unbinds all the lifecycles from CameraX, then creates new with new parameters
     * */
    private fun startCamera() {
        // This is the CameraX PreviewView where the camera will be rendered
        val viewFinder = binding.viewFinder
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
            } catch (e: InterruptedException) {
                Toast.makeText(requireContext(), "Error starting camera", Toast.LENGTH_SHORT).show()
                return@addListener
            } catch (e: ExecutionException) {
                Toast.makeText(requireContext(), "Error starting camera", Toast.LENGTH_SHORT).show()
                return@addListener
            }

            // The display information
            val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
            // The ratio for the output image and preview
            viewModel.aspectRatio(metrics.widthPixels, metrics.heightPixels)

            // The display rotation
            val rotation = viewFinder.display.rotation

            val localCameraProvider =
                cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

            // The Configuration of camera preview
            preview =
                Preview.Builder().setTargetAspectRatio(aspectRatio) // set the camera aspect ratio
                    .setTargetRotation(rotation) // set the camera rotation
                    .build()

            // The Configuration of image capture
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY) // setting to have pictures with highest quality possible (may be slow)
                .setTargetAspectRatio(aspectRatio) // set the capture aspect ratio
                .setTargetRotation(rotation) // set the capture rotation
                .build()

            //videoRecords
//            val cameraInfo = localCameraProvider.availableCameraInfos.filter {
//                Camera2CameraInfo
//                    .from(it)
//                    .getCameraCharacteristic(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_BACK
//            }

//            val supportedQualities = QualitySelector.getSupportedQualities(cameraInfo[0])
            val qualitySelector = QualitySelector.fromOrderedList(
                listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD),
                FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
            )

            val recorder =
                Recorder.Builder().setExecutor(ContextCompat.getMainExecutor(requireContext()))
                    .setQualitySelector(qualitySelector).build()
            videoCapture = VideoCapture.withOutput(recorder)

            // Unbind the use-cases before rebinding them
            localCameraProvider.unbindAll()
            // Bind all use cases to the camera with lifecycle
            bindToLifecycle(localCameraProvider, viewFinder)
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    private fun bindToLifecycle(
        localCameraProvider: ProcessCameraProvider, viewFinder: PreviewView
    ) {
        try {
            // Bind all use cases to the camera with lifecycle
            camera = localCameraProvider.bindToLifecycle(
                viewLifecycleOwner, // current lifecycle owner
                hdrCameraSelector ?: lensFacing, // either front or back facing
                preview, // camera preview use case
                imageCapture, // image capture use case
                videoCapture
            )
            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(viewFinder.surfaceProvider)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("NON_EXHAUSTIVE_WHEN")
    private fun takePicture() = lifecycleScope.launch(Dispatchers.Main) {
        captureImage()
    }

    private fun captureImage() {
        val localImageCapture =
            imageCapture ?: throw IllegalStateException("Camera initialization failed.")

        // Setup image capture metadata
        val metadata = ImageCapture.Metadata().apply {
            // Mirror image when using the front camera
            isReversedHorizontal = lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA
        }

        lifecycleScope.launch {
            viewModel.captureImage(localImageCapture, outputDirectory, metadata)
        }
    }

    private fun setupObservers() {
        viewModel.captureImage.observe(viewLifecycleOwner) {
            it.savedUri?.let { uri ->
                setGalleryThumbnail(uri)
            }
        }
        viewModel.videoUri.observe(viewLifecycleOwner) {
            if (recording == null) {
                recording = it.recording
            }
            if (it.uri != null) {
                setGalleryThumbnail(it.uri)
            }
        }

        viewModel.getTime.observe(viewLifecycleOwner) {
            binding.timerTxt.text = it
            val seconds = it.substring(it.length - 2).toInt()
            if (setTime != 0) {
                if (seconds == setTime) {
                    stopVideo()
                    context?.toast("Limit Reach")
                }
            }
        }

        viewModel.getAspectRatio.observe(viewLifecycleOwner) {
            aspectRatio = it
        }
    }

    /**
     * This function is called from XML view via Data Binding to select a FlashMode
     *  possible values are ON, OFF or AUTO
     *  circularClose() function is an Extension function which is adding circular close
     * */
    private fun closeFlashAndSelect(@ImageCapture.FlashMode flash: Int) =
        binding.btnFlash.toggleButton(
            flag = flashMode == FLASH_MODE_ON,
            rotationAngle = 360f,
            firstIcon = R.drawable.flash_icon_off,
            secondIcon = R.drawable.flash_icon_on
        ) {
            flashMode = flash
            imageCapture?.flashMode = flashMode
            prefs.putInt(KEY_FLASH, flashMode)
        }

    /**
     * Turns on or off the flashlight
     * */
    private fun toggleFlash() {
        FLASH_MODE_ON
        camera?.cameraControl?.enableTorch(true)
    }

    /**
     * Change the facing of camera
     *  toggleButton() function is an Extension function made to animate button rotation
     * */
    @SuppressLint("RestrictedApi")
    fun toggleCamera() = binding.btnSwitchCamera.toggleButton(
        flag = lensFacing == CameraSelector.DEFAULT_BACK_CAMERA,
        rotationAngle = 180f,
        firstIcon = R.drawable.rotate_icon,
        secondIcon = R.drawable.rotate_icon,
    ) {
        lensFacing = if (it) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
        startCamera()
    }

    private fun resetTimer() {
        binding.timerTxt.viewGone()
        viewModel.resetTimer()
    }

    override fun onStop() {
        super.onStop()
        camera?.cameraControl?.enableTorch(false)
    }

    private fun setVideoTime() {
        setTime = args.maxVideoRecordLimit.toInt()
    }
}