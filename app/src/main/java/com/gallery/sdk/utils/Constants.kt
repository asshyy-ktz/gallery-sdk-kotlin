package com.gallery.sdk.utils

object Constants {

    const val KEY_FLASH = "sPrefFlashCamera"
    const val RATIO_4_3_VALUE = 4.0 / 3.0 // aspect ratio 4x3
    const val RATIO_16_9_VALUE = 16.0 / 9.0 // aspect ratio 16x9
    const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
    const val PHOTO_TYPE = "image/jpeg"

    const val BUNDLE_MAX_SELECTION_LIMIT = "maxSelectionLimit"
    const val BUNDLE_MAX_VIDEO_RECORD_LIMIT = "maxVideoRecordLimit"
    const val BUNDLE_SELECTED_IMAGE_RESULT = "selectedImageResult"

    const val DEFAULT_SELECTION_LIMIT = 10
    const val DEFAULT_RECORDING_LIMIT = 10

    //Minimum Video you want to buffer while Playing
    const val MIN_BUFFER_DURATION = 2000

    //Max Video you want to buffer during PlayBack
    const val MAX_BUFFER_DURATION = 5000

    //Min Video you want to buffer before start Playing it
    const val MIN_PLAYBACK_START_BUFFER = 1500

    //Min video You want to buffer when user resumes video
    const val MIN_PLAYBACK_RESUME_BUFFER = 2000


}