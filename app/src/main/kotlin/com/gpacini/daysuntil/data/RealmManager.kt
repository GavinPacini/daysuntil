package com.gpacini.daysuntil.data

import com.gpacini.daysuntil.data.model.Event
import io.realm.Realm
import io.realm.RealmAsyncTask
import io.realm.RealmObject
import io.realm.Sort
import rx.Observable

/**
 * Created by gavinpacini on 10/10/15.
 *
 * Use this to manage the Realm database. Be sure to run close after using this.
 */
class RealmManager {

    private var realm = Realm.getDefaultInstance()

    /**
     * Returns a RxObservable which states if there are any events in the Realm database.
     *
     * @return A hot RxObservable which will emit changes of the result i.e. true when we have
     * events and then false when we don't have events
     */
    fun hasEvents(): Observable<Boolean> {

        return realm.where(Event::class.java)
                .findAllAsync()
                .asObservable()
                .map { !it.isEmpty() }
                .distinctUntilChanged()
    }

    /**
     * Returns a RxObservable with all the events in the Realm database sorted by timestamp descending
     * Returns an empty list if we have no events
     *
     * @return A hot RxObservable which emits all events currently in the Realm Database
     */
    fun loadEvents(): Observable<List<Event>> {

        return realm.where(Event::class.java)
                .findAllSortedAsync("timestamp", Sort.DESCENDING)
                .asObservable()
                .map { it.toList() }
    }

    /**
     * Use this to create a new event in the Realm database. Returns an Observable which emits after
     * inserting the new event. Runs on a separate thread.
     *
     * @return An RxObservable which will call onNext after committing the event to Realm
     */
    fun newEvent(uuid: String?, title: String?, timestamp: Long): Observable<RealmAsyncTask> {

        return Observable.just (
                realm.executeTransactionAsync { realm ->
                    val event = Event(uuid, title, timestamp)
                    realm.copyToRealmOrUpdate(event)
                }
        )
    }

    /**
     * Use this to remove an  event in the Realm database. Returns an Observable which emits after
     * removing the event based on UUID. Runs on a separate thread.
     *
     * @return An RxObservable which will call onNext after removing the event from Realm
     */
    fun removeEvent(uuid: String?): Observable<RealmAsyncTask> {

        return Observable.just(
                realm.executeTransactionAsync { realm ->
                    val realmEvent = realm.where(Event::class.java).equalTo("uuid", uuid).findFirst()
                    RealmObject.deleteFromRealm(realmEvent)
                }
        )
    }

    /**
     * Closes the instance of Realm
     * MUST be called in onDestroy (or similar) to prevent Realm staying open
     */
    fun close() {
        realm.close()
    }

}