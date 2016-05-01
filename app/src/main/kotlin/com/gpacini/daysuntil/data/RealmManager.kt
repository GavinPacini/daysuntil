package com.gpacini.daysuntil.data

import com.gpacini.daysuntil.data.model.Event
import io.realm.Realm
import io.realm.RealmAsyncTask
import io.realm.RealmObject
import io.realm.Sort
import rx.Observable

/**
 * Created by gavinpacini on 10/10/15.
 */
class RealmManager {

    private var realm = Realm.getDefaultInstance()

    fun hasEvents(): Observable<Boolean> {

        return realm.where(Event::class.java)
                .findAllAsync()
                .asObservable()
                .map { !it.isEmpty() }
                .distinctUntilChanged()
    }

    fun loadEvents(): Observable<List<Event>> {

        return realm.where(Event::class.java)
                .findAllSortedAsync("timestamp", Sort.DESCENDING)
                .asObservable()
                .map { it.toList() }
    }

    fun newEvent(uuid: String?, title: String?, timestamp: Long): Observable<RealmAsyncTask> {

        return Observable.just (
            realm.executeTransactionAsync{ realm ->
                val event = Event(uuid, title, timestamp)
                realm.copyToRealmOrUpdate(event)
            }
        )
    }

    fun removeEvent(uuid: String?): Observable<RealmAsyncTask> {

        return Observable.just(
            realm.executeTransactionAsync{ realm ->
                val realmEvent = realm.where(Event::class.java).equalTo("uuid", uuid).findFirst()
                RealmObject.deleteFromRealm(realmEvent)
            }
        )
    }

    fun close() {
        realm.close()
    }

}