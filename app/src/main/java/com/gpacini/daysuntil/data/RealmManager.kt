package com.gpacini.daysuntil.data

import android.content.Context
import com.gpacini.daysuntil.data.model.Event
import com.gpacini.daysuntil.data.model.RealmEvent
import com.kboyarshinov.realmrxjava.rx.RealmObservable
import io.realm.Realm
import io.realm.RealmResults
import rx.Observable
import rx.functions.Func1
import java.util.*

/**
 * Created by gavinpacini on 10/10/15.
 */
object RealmManager {

    public fun loadEvents(context: Context): Observable<List<Event>> {
        return RealmObservable.results(context,{ realm ->
            realm.where(RealmEvent::class.java).findAllSorted("timestamp", false)
        })
        .map{ realmEvents ->
            val events = ArrayList<Event>(realmEvents.size())
            for (realmEvent in realmEvents) {
                events.add(Event(realmEvent))
            }
            events
        }
    }

    public fun newEvent(context: Context, title: String, uuid: String, timestamp: Long): Observable<RealmEvent> {
        return RealmObservable.obj(context, { realm ->
            val event = RealmEvent()
            event.title = title
            event.uuid = uuid
            event.timestamp = timestamp
            realm.copyToRealmOrUpdate(event)
        })
    }

    public fun removeEvent(context: Context, uuid: String): Observable<Event> {
        return RealmObservable.remove(context, { realm ->
            val realmEvent = realm.where(RealmEvent::class.java).equalTo("uuid", uuid).findFirst()
            val event = Event(realmEvent)
            realmEvent.removeFromRealm()
            event
        })
    }
}