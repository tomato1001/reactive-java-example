package com.phome.sample;


import rx.Observable;
import rx.observables.BlockingObservable;
import rx.schedulers.Timestamped;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zw
 */
public class Samples {

    /**
     * Create observable with {@code from} or {@code just}.
     * <p>{@code from} accept array or collection value and {@code just} accept single value.
     *
     */
    static void basic() {
        List<String> names = new ArrayList<>();
        names.add("first");
        names.add("two");
        names.add("third");
        names.add("four");

        // create from list
        Observable.from(names).subscribe(System.out::println);

        // create from one item
        Observable.just("first").subscribe(System.out::println);
    }


    /**
     *  custom Observable that blocks when subscribed to (does not spawn an extra thread).
     */
    static void blockingObservable() {
        Observable<String> blockObservable = Observable.create(subscriber -> {
            for (int i = 0; i < 50; i++) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext("BlockObservable-" + i);
                }
            }

            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        });
        blockObservable.subscribe(System.out::println);
    }

    static void nonBlockingObservable() {
        Observable<String> nonBlockObservable = Observable.create(subscriber -> new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                // Do nothing if the subscription is
                if (subscriber.isUnsubscribed()) {
                    return;
                }
                subscriber.onNext("nonblocking-" + i);
            }
            // after sending all values we complete the sequence
            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }).start());

        nonBlockObservable.subscribe(System.out::println);
        System.out.println("execute before above code");
    }

    static void isUnsubscribed() {
        Observable<String> observable = Observable.create(subscriber -> {
            for (int i = 0; i < 1; i++) {
                System.out.println(subscriber.isUnsubscribed() + "  ==");
            }
        });

        observable.subscribe(System.out::println);
    }


    static void nonBlockingObserverTransforming() {
        Observable<String> nonBlockObserver = Observable.create(subscriber -> new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                if (subscriber.isUnsubscribed()) {
                    return;
                }

                subscriber.onNext("onNext_" + i);
            }
            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }).start());

        nonBlockObserver.skip(10).take(3).map(str -> str + "_map").subscribe(System.out::println);
    }

    static void compose() {
        Observable<Integer> o1 = Observable.just(1, 2).delay(1, TimeUnit.SECONDS);
        Observable<Integer> o2 = Observable.just(3, 4).delay(2, TimeUnit.SECONDS);
        Observable<Integer> o3 = Observable.just(5, 6).delay(3, TimeUnit.SECONDS);
//
//
        BlockingObservable<List<Timestamped<Integer>>> os = Observable.merge(o1, o2, o3)
                .timestamp()
                .filter(integerTimestamped -> integerTimestamped.getValue() == 1 || integerTimestamped.getValue() == 6)
                .toList().toBlocking();
        os.subscribe(System.out::println);

    }



    public static void main(String[] args) {

//        isUnsubscribed();
//        nonBlockingObservable();

//        nonBlockingObserverTransforming();
        compose();
    }
}
