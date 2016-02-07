package com.gpacini.daysuntil.data

import android.util.Log
import com.gpacini.daysuntil.data.model.Event
import com.gpacini.daysuntil.data.model.RealmEvent
import io.realm.Realm
import io.realm.RealmAsyncTask
import io.realm.Sort
import rx.Observable

/**
 * Created by gavinpacini on 10/10/15.
 */
class RealmManager {

    private var realm = Realm.getDefaultInstance()

    private var position = 0

    fun hasEvents(): Observable<Boolean> {

        return realm.where(RealmEvent::class.java)
                .findAllAsync()
                .asObservable()
                .map { realmEvents ->
                    !realmEvents.isEmpty()
                }
    }

    fun loadEvents(): Observable<Event> {

        return realm.where(RealmEvent::class.java)
                .findAllSortedAsync("timestamp", Sort.DESCENDING)
                .asObservable()
                .map { realmEvents ->
                    position = 0
                    realmEvents
                }
                .flatMap { Observable.from(it) }
                .map { realmEvent ->
                    val event = Event(realmEvent)
                    event.position = position
                    position++
                    event
                }
    }

    fun newEvent(title: String?, uuid: String?, timestamp: Long): Observable<RealmAsyncTask> {

        return Observable.just(realm.executeTransaction({ realm ->
            val event = RealmEvent()
            event.title = title
            event.uuid = uuid
            event.timestamp = timestamp
            realm.copyToRealmOrUpdate(event)
        }, null))
    }

    fun removeEvent(uuid: String?): Observable<RealmAsyncTask> {

        return Observable.just(realm.executeTransaction({ realm ->
            val realmEvent = realm.where(RealmEvent::class.java).equalTo("uuid", uuid).findFirst()
            realmEvent.removeFromRealm()
        }, null))
    }

    fun close() {
        realm.close()
    }

}