package com.gpacini.daysuntil.data

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by gavinpacini on 10/10/15.
 */
class ImageHelper {

    companion object Factory {
        private val instanceImageHelper: ImageHelper by lazy {
            ImageHelper()
        }

        public fun getInstance(): ImageHelper {
            return instanceImageHelper
        }

    }

    public var filePath: String? = null

    public fun init(filePath: String) {
        this.filePath = filePath
    }

    public fun with(uuid: String?): String {
        return "file://${filePath}/${uuid}.jpg"
    }

    public fun saveImage(bmp: Bitmap?, uuid: String?) {

        val folder = File(filePath);
        val imageFile = File(filePath + "/${uuid}.jpg")

        if (!folder.exists()) {
            folder.mkdir();
        }

        if (!imageFile.exists()) {
            imageFile.createNewFile();
        }

        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(imageFile);
            bmp?.compress(Bitmap.CompressFormat.JPEG, 85, out);
        } catch (e: Exception) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
    }

    public fun deleteImage(uuid: String?) {

        val imageFile = File(filePath + "/${uuid}.jpg")

        if (imageFile.exists()) {
            imageFile.delete()
        }
    }
}