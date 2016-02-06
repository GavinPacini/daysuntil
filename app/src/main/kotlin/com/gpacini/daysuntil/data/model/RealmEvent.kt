package com.gpacini.daysuntil.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by gavinpacini on 10/10/15.
 */
open class RealmEvent : RealmObject() {

    @PrimaryKey
    open var uuid: String? = null

    open var title: String? = null

    open var timestamp: Long = 0
}