package com.walterade.callcrusher.utils;

import java.util.ArrayList;
import java.util.HashMap;

import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Walter on 10/5/17.
 */

public class RxBusUtils {
    static HashMap<Object, Subscription> subscriptionMap = new HashMap<>();
    static HashMap<String, Subscription> subscriptionArrayMap = new HashMap<>();

    public static class DisposableSubscriptions {
        ArrayList<Integer> actions = new ArrayList<>();

        public void add(Object action) {
            actions.add(action.hashCode());
        }
        public void dispose() {
            for (Integer a : actions) {
                unsubscribeAll(a);
            }
            actions.clear();
        }
    }

    public static <T> Action1<T> listenFor(final Class<T> eventClass, Action1<T> onNext) {

        subscriptionMap.put(onNext.hashCode(), RxBus.getInstance().register(eventClass, t -> {
            onNext.call(t);
            subscriptionMap.remove(onNext.hashCode()).unsubscribe();
        }));

        return onNext;
    }

    public static <T, R> Func1<T, R> listenUntil(final Class<T> eventClass, Func1<T, R> onNext) {

        subscriptionMap.put(onNext.hashCode(), RxBus.getInstance().register(eventClass, t -> {
            if (Boolean.TRUE.equals(onNext.call(t)))
                subscriptionMap.remove(onNext.hashCode()).unsubscribe();
        }));

        return onNext;
    }

    public static <T> CompositeSubscription registerAll(final Class<T> eventClass[], Action1<T> onNext) {

        CompositeSubscription subscriptions = new CompositeSubscription();

        for (int i = 0; i < eventClass.length; i++)
            subscriptions.add(
                    RxBus.getInstance().register(eventClass[i], t -> {
                        onNext.call(t);
                    }));

        return subscriptions;
    }


    public static <T> void stopListeningFor(Action1<T> onNext) {
        unsubscribeAll(onNext);
    }

    //listen for any of these events, if any one event comes in then unsubscribe from all of them

    public static <T> Action1<T> listenForAny(final Class<T> eventClass[], Action1<T> onNext) {

        for (int i = 0; i < eventClass.length; i++)
            subscriptionArrayMap.put(onNext.hashCode() + "." + i,
                RxBus.getInstance().register(eventClass[i], t -> {
                    onNext.call(t);
                    unsubscribeAll(onNext);
                }));

        return onNext;
    }

    public static <T> Action1<T> listenForAll(final Class<T> eventClass[], Action1<T> onNext) {

        for (int i = 0; i < eventClass.length; i++) {
            String key = onNext.hashCode() + "." + i;
            subscriptionArrayMap.put(key,
                    RxBus.getInstance().register(eventClass[i], t -> {
                        onNext.call(t);
                        subscriptionArrayMap.remove(key).unsubscribe();
                    }));
        }

        return onNext;
    }

    private static void unsubscribeAll(Object onNext) {
        Subscription subscription;
        int i = 0;

        subscription = subscriptionMap.remove(onNext.hashCode());
        if (subscription != null) subscription.unsubscribe();

        do {
            subscription = subscriptionArrayMap.remove(onNext.hashCode() + "." + i++);
            if (subscription != null) subscription.unsubscribe();
        } while (subscription != null);
    }

}
