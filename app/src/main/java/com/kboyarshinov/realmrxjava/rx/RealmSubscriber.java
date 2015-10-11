package com.kboyarshinov.realmrxjava.rx;

import rx.Subscriber;

/**
 * Created by gavinpacini on 10/10/15.
 */
public abstract class RealmSubscriber<T> extends Subscriber<T> {

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onNext(T t) {

    }
}
