package com.gpacini.daysuntil.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

/**
 * Created by gavinpacini on 10/10/15.
 */
@RealmClass
public open class RealmEvent : RealmObject() {

    @PrimaryKey
    public open var uuid: String? = null

    public open var title: String? = null

    public open var timestamp: Long = 0
}
