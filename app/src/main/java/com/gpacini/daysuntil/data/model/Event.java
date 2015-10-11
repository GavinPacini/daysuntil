package com.gpacini.daysuntil.data.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gavinpacini on 10/10/15.
 */
public class Event implements Parcelable {

    private String uuid;
    private String title;
    private long timestamp;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uuid);
        dest.writeString(this.title);
        dest.writeLong(this.timestamp);
    }

    public Event(RealmEvent realmEvent) {
        this.uuid = realmEvent.getUuid();
        this.title = realmEvent.getTitle();
        this.timestamp = realmEvent.getTimestamp();
    }

    protected Event(Parcel in) {
        this.uuid = in.readString();
        this.title = in.readString();
        this.timestamp = in.readLong();
    }

    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        public Event createFromParcel(Parcel source) {
            return new Event(source);
        }

        public Event[] newArray(int size) {
            return new Event[size];
        }
    };


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (timestamp != event.timestamp) return false;
        if (!uuid.equals(event.uuid)) return false;
        return title.equals(event.title);

    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }
}
