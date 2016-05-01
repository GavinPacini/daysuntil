package com.gpacini.daysuntil.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.gpacini.daysuntil.R

/**
 * Created by gavinpacini on 01/05/2016.
 */
open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(R.color.background_material_light)
    }

}