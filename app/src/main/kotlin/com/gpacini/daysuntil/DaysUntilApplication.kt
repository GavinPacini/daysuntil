package com.gpacini.daysuntil

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.gpacini.daysuntil.data.ImageHelper
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by gavinpacini on 10/10/15.
 */
class DaysUntilApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        //Set path to store event images
        val appPath = this.filesDir.absolutePath
        ImageHelper.getInstance().init("${appPath}/images")

        //Setup Realm
        val config = RealmConfiguration.Builder(this).build()
        Realm.setDefaultConfiguration(config)

        //Setup Crashlytics
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }
    }
}