package com.gpacini.daysuntil.data

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.squareup.picasso.Picasso

/**
 * Created by gavinpacini on 27/04/2016.
 */
class CustomTarget(var callback: (Bitmap?) -> Unit) : com.squareup.picasso.Target{

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
    }

    override fun onBitmapFailed(errorDrawable: Drawable?) {
    }

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        callback.invoke(bitmap)
    }
}