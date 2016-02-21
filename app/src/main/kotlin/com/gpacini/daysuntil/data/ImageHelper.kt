package com.gpacini.daysuntil.data

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

/**
 * Created by gavinpacini on 10/10/15.
 */
class ImageHelper {

    companion object Factory {
        private val instanceImageHelper: ImageHelper by lazy {
            ImageHelper()
        }

        fun getInstance(): ImageHelper {
            return instanceImageHelper
        }

    }

    var filePath: String? = null

    fun init(filePath: String) {
        this.filePath = filePath
    }

    fun with(uuid: String?): String {
        return "file://${filePath}/${uuid}.jpg"
    }

    fun withCrop(uuid: String?): String {
        return "file://${filePath}/${uuid}_crop.jpg"
    }

    fun saveImage(bmp: Bitmap?, bmpCrop: Bitmap?, uuid: String?) {

        val folder = File(filePath)
        val imageFile = File(filePath + "/${uuid}.jpg")
        val imageFileCrop = File(filePath + "/${uuid}_crop.jpg")

        if (!folder.exists()) {
            folder.mkdir()
        }

        if (!imageFile.exists()) {
            imageFile.createNewFile()
        }

        if (!imageFileCrop.exists()) {
            imageFileCrop.createNewFile()
        }

        FileOutputStream(imageFile).use {
            bmp?.compress(Bitmap.CompressFormat.JPEG, 85, it)
        }

        FileOutputStream(imageFileCrop).use {
            bmpCrop?.compress(Bitmap.CompressFormat.JPEG, 85, it)
        }
    }

    fun deleteImage(uuid: String?) {

        val imageFile = File(filePath + "/${uuid}.jpg")
        val imageFileCrop = File(filePath + "/${uuid}_crop.jpg")

        if (imageFile.exists()) {
            imageFile.delete()
        }

        if (imageFileCrop.exists()) {
            imageFileCrop.delete()
        }
    }
}