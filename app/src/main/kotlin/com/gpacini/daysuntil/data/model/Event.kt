package com.gpacini.daysuntil.data.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by gavinpacini on 10/10/15.
 */
class Event : Parcelable {

    var uuid: String? = null
    var title: String? = null
    var timestamp: Long = 0

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.uuid)
        dest.writeString(this.title)
        dest.writeLong(this.timestamp)
    }

    constructor(realmEvent: RealmEvent) {
        this.uuid = realmEvent.uuid
        this.title = realmEvent.title
        this.timestamp = realmEvent.timestamp
    }

    protected constructor(input: Parcel) {
        this.uuid = input.readString()
        this.title = input.readString()
        this.timestamp = input.readLong()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val event = other as Event?

        if (uuid == event?.uuid) return true
        return false

    }

    override fun hashCode(): Int {
        var result = uuid!!.hashCode()
        result = 31 * result + title!!.hashCode()
        result = 31 * result + (timestamp xor (timestamp.ushr(32))).toInt()
        return result
    }

    companion object {

        @JvmField final val CREATOR: Parcelable.Creator<Event> = object : Parcelable.Creator<Event> {
            override fun createFromParcel(source: Parcel): Event {
                return Event(source)
            }

            override fun newArray(size: Int): Array<Event?> {
                return arrayOfNulls(size)
            }
        }
    }
}
