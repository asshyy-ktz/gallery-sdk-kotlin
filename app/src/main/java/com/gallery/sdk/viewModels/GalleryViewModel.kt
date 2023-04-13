package com.gallery.sdk.viewModels

import android.app.Application
import android.content.ContentUris
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gallery.sdk.models.GalleryData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GalleryViewModel @Inject constructor(private val applicationContext: Application) :
    AndroidViewModel(applicationContext) {
    private val _allImagesFromGallery: MutableStateFlow<List<GalleryData>> =
        MutableStateFlow(listOf())

    val allImagesFromGallery: StateFlow<List<GalleryData>> = _allImagesFromGallery

    private fun getAllImages(): List<GalleryData> {
        val allImages = mutableListOf<GalleryData>()

        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED
        )

        val imageSortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = getApplication<Application>().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                MediaStore.Video.Media.getContentUri(
//                    MediaStore.VOLUME_EXTERNAL
//                )
//            } else {
//                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//            },
            imageProjection,
            null,
            null,
            imageSortOrder
        )
        cursor.use {

            if (cursor != null) {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                while (cursor.moveToNext()) {
                    allImages.add(
                        GalleryData(
                            ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                cursor.getLong(idColumn)
                            ),
                            cursor.getLong(dateColumn), 0, false
                        )
                    )
                }
            } else {
                Log.d("AddViewModel", "Cursor is null!")
            }
        }
        return allImages
    }


    private fun getAllVideos(onlyVideos: Boolean = false): List<GalleryData> {
        val allImages = mutableListOf<GalleryData>()
        if (!onlyVideos) {
            allImages.addAll(getAllImages())
        }
        val imageProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATE_ADDED
        )

        val imageSortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        val cursor = getApplication<Application>().contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                MediaStore.Video.Media.getContentUri(
//                    MediaStore.VOLUME_EXTERNAL
//                )
//            } else {
//                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//            },
            imageProjection,
            null,
            null,
            imageSortOrder
        )
        cursor.use {

            if (cursor != null) {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                while (cursor.moveToNext()) {
                    allImages.add(
                        GalleryData(
                            ContentUris.withAppendedId(
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                cursor.getLong(idColumn)
                            ),
                            cursor.getLong(dateColumn),
                            1, false
                        )
                    )
                }
            } else {
                Log.d("AddViewModel", "Cursor is null!")
            }
        }
        allImages.sortByDescending { image -> image.date }
        return allImages
    }

    fun loadAllImages() {
        viewModelScope.launch {
            _allImagesFromGallery.value = withContext(Dispatchers.IO) {
                getAllImages()
            }
        }
    }

    fun loadAllImagesVideos() {
        viewModelScope.launch {
            _allImagesFromGallery.value = withContext(Dispatchers.IO) {
                getAllVideos()
            }
        }
    }

    fun loadAllVideos() {
        viewModelScope.launch {
            _allImagesFromGallery.value = withContext(Dispatchers.IO) {
                getAllVideos(true)
            }
        }
    }


}