package com.gpacini.daysuntil.data.images

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

/**
 * Created by gavinpacini on 27/04/2016.
 *
 * A custom Picasso Target which allows for raw access to the Bitmap
 */
class CustomTarget(var callback: (Bitmap?) -> Unit) : Target {

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
    }

    override fun onBitmapFailed(errorDrawable: Drawable?) {
    }

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        callback.invoke(bitmap)
    }
}