package com.phome.samples.rx;

import org.junit.Test;
import rx.Observable;
import rx.Subscription;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zw
 */
public class RxTest {


    /**
     * Creates Observable from Iterable/List/Array by {@code from} method.
     * <p>The {@code subscribe} method of Observable will call it's Iterable source's
     * {@code iterator()} method to obtain an new instance of Iterator.
     */
    @Test
    public void from() {
        Observable.from(Arrays.asList(1, 3, 4, 9)).subscribe(
                v -> System.out.print(v + " | "),
                v -> System.out.println("error"),
                () -> System.out.println("End")
        );

        Observable.from(Arrays.asList(5, 3, 7, 8))
                .reduce((integer, integer2) -> integer + integer2)
                .subscribe(System.out::println);

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get("/home/wind"))) {
            Observable<Path> pathObservable = Observable.from(directoryStream);
            pathObservable.subscribe(System.out::println);
            // Below line will cause exception
            // because Iterator can be called only once
//            pathObservable.subscribe(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void just() {
        Observable.just("o1").subscribe(System.out::println);
        // Just print o2
        Observable.just("o1").just("o2").subscribe(System.out::println);

    }

    @Test
    public void interval() throws InterruptedException {
        subscribePrint(
                Observable.interval(500, TimeUnit.MILLISECONDS),
                "Interval Observable"
        );

        subscribePrint(
                Observable.interval(0, 1, TimeUnit.SECONDS),
                "Timed Interval Observable"
        );

        subscribePrint(
                Observable.error(new IllegalArgumentException("Illegal argument")),
                "Error Observable"
        );

//        This one emits no items, but it
//        emits a OnCompleted notification immediately.
        subscribePrint(
                Observable.empty(),
                "Empty Observable"
        );

//        This does nothing. It sends
//        no notifications to its Observer instances, and even the OnCompleted
//        notification is not sent.
        subscribePrint(Observable.never(), "Never Observable");
        subscribePrint(Observable.range(1, 3), "Range Observable");

        Thread.sleep(10000);

    }

    <T> Subscription subscribePrint(Observable<T> observable, String name) {
        return observable.subscribe(
                value -> System.out.println(name + ": " + value),
                e -> {
                    System.err.println("Error from " + name);
                    e.printStackTrace();
                },
                () -> System.out.println("  " + name + " end")
        );

    }

    @Test
    public void fromIterable() {
        fromIterable(Arrays.asList(1, 2, 3)).subscribe(System.out::println);
    }

    <T> Observable<T> fromIterable(Iterable<T> it) {
        return Observable.create(subscriber -> {
            Iterator<T> iterator = it.iterator();
            try {
                while (iterator.hasNext()) {
                    if (subscriber.isUnsubscribed()) {
                        return;
                    }
                    T value = iterator.next();
                    subscriber.onNext(value);
                }
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onCompleted();
                }

            } catch (Exception ex) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(ex);
                }
            }
        });
    }

    @Test
    public void unSubscribe() throws InterruptedException, IOException {
        Path path = Paths.get("/home/wind/Downloads", "warning.xml");
        List<String> lines = Files.readAllLines(path);
        Observable<String> observable = fromIterable(lines)
                .subscribeOn(Schedulers.computation());
        Subscription subscription = subscribePrint(observable, "File");
        System.out.println("Before unsubscribe");
        System.out.println("------------------");

        // sleep awhile
        Thread.sleep(10);

        subscription.unsubscribe();

        System.out.println("------------------");
        System.out.println("After unsubscribe");
    }

    @Test
    public void coldAndHotObservable() throws InterruptedException {
//        Cold Observables produce notifications on demand, and for every Subscriber, they
//        produce independent notifications.

//        There are Observable instances which, when they start emitting notifications, it
//        doesn't matter if there are subscriptions to them or not. They continue emitting them
//        until completion. All the subscribers receive the same notifications, and by default,
//        when a Subscriber subscribes, it doesn't receive the notifications emitted before that.
//        These are hot Observable instances.

//        The ConnectableObservable class
//        These Observable instances are inactive until their connect() method is called.
//        After that, they become hot Observables. The ConnectableObservable instance can
//        be created from any Observable instance by calling its publish() method. In other
//        words, the publish() method can turn any cold Observable into a hot one.

        Observable<Long> observable = Observable.interval(1, TimeUnit.SECONDS);
        ConnectableObservable<Long> connectableObservable = observable.publish();
        Subscription s1 = subscribePrint(connectableObservable, "First");
        Subscription s2 = subscribePrint(connectableObservable, "Two");
        connectableObservable.connect();

        Thread.sleep(3000);
        // Three just receive present, don't receive before
        Subscription s3 = subscribePrint(connectableObservable, "Three");
        Thread.sleep(2000);

        s1.unsubscribe();
        s2.unsubscribe();
        s3.unsubscribe();

        Thread.sleep(5000);

    }

    @Test
    public void hotObservableReceiveAll() throws InterruptedException {
//        Observable<Long> refCount = Observable.interval(1, TimeUnit.SECONDS).publish().refCount();

        // share() is shortcut of .publish().refCount()
        Observable<Long> refCount = Observable.interval(1, TimeUnit.SECONDS).share();
        Subscription s1 = subscribePrint(refCount, "First");
        Subscription s2 = subscribePrint(refCount, "Two");

        Thread.sleep(5000);
        s1.unsubscribe();
        s2.unsubscribe();

        // This will receive all of data before emmit
        Subscription s3 = subscribePrint(refCount, "Three");
        Thread.sleep(5000);
        s3.unsubscribe();

    }

    @Test
    public void subjectHotObservable() throws InterruptedException {
        Observable<Long> observable = Observable.interval(1, TimeUnit.SECONDS);
        Subject<Long, Long> subject = PublishSubject.create();
        observable.subscribe(subject);

        Subscription s1 = subscribePrint(subject, "First");
        Subscription s2 = subscribePrint(subject, "Two");

        Thread.sleep(2000);
        subject.onNext(999L);

        Subscription s3 = subscribePrint(subject, "Three");
        Thread.sleep(2000);

        s1.unsubscribe();
        s2.unsubscribe();
        s3.unsubscribe();

    }

    @Test
    public void replaySubjectReceiveAll() throws InterruptedException {
        Observable<Long> interval = Observable.interval(1, TimeUnit.SECONDS);
        ReplaySubject<Long> replaySubject = ReplaySubject.create();
        interval.subscribe(replaySubject);

        Subscription s1 = subscribePrint(replaySubject, "First");

        Thread.sleep(5000);

        Subscription s2 = subscribePrint(replaySubject, "Two");
        Thread.sleep(2000);

        s1.unsubscribe();
        s2.unsubscribe();
    }

    @Test
    public void reactSumWithBehaviorSubject() {

        ReactSumWithBehaviorSubject rsbs = new ReactSumWithBehaviorSubject();
        subscribePrint(rsbs.getSum(), "Sum");

        rsbs.setA(1);
        rsbs.setB(203);
        rsbs.setA(123);

    }

    private static class ReactSumWithBehaviorSubject {
        private BehaviorSubject<Integer> a = BehaviorSubject.create();
        private BehaviorSubject<Integer> b = BehaviorSubject.create();
        private BehaviorSubject<Integer> sum = BehaviorSubject.create();

        public ReactSumWithBehaviorSubject() {
            Observable.combineLatest(a, b, (x, y) -> x + y).subscribe(sum);
        }

        public int getA() {
            return a.getValue();
        }

        public void setA(int value) {
            a.onNext(value);
        }

        public int getB() {
            return b.getValue();
        }

        public void setB(int value) {
            b.onNext(value);
        }

        public Observable<Integer> getSum() {
            return sum.asObservable();
        }

    }

}
