package com.gpacini.daysuntil.data.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by gavinpacini on 10/10/15.
 */
class Event : Parcelable {

    public var uuid: String? = null
    public var title: String? = null
    public var timestamp: Long = 0

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


    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val event = o as Event?

        if (timestamp != event?.timestamp) return false
        if (uuid != event?.uuid) return false
        return title == event?.title

    }

    override fun hashCode(): Int {
        var result = uuid!!.hashCode()
        result = 31 * result + title!!.hashCode()
        result = 31 * result + (timestamp xor (timestamp.ushr(32))).toInt()
        return result
    }

    companion object {

        val CREATOR: Parcelable.Creator<Event> = object : Parcelable.Creator<Event> {
            override fun createFromParcel(source: Parcel): Event {
                return Event(source)
            }

            override fun newArray(size: Int): Array<Event?> {
                return arrayOfNulls(size)
            }
        }
    }
}
