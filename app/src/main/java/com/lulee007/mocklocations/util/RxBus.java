package com.lulee007.mocklocations.util;

/**
 * User: lulee007@live.com
 * Date: 2016-02-24
 * Time: 18:31
 * ref: http://www.jianshu.com/p/ca090f6e2fe2
 */

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class RxBus {

    private static volatile RxBus mDefaultInstance;

    private RxBus() {
    }

    public static RxBus getDefault() {
        if (mDefaultInstance == null) {
            synchronized (RxBus.class) {
                if (mDefaultInstance == null) {
                    mDefaultInstance = new RxBus();
                }
            }
        }
        return mDefaultInstance;
    }

    private final Subject<Object, Object> _bus = new SerializedSubject<>(PublishSubject.create());

    public void send(Object o) {
        _bus.onNext(o);
    }

    // 根据传递的 eventType 类型返回特定类型(eventType)的 被观察者
    public <T extends Object> Observable<T> toObserverable(final Class<T> eventType) {
        return _bus
                .filter(new Func1<Object, Boolean>() {
                    @Override
                    public Boolean call(Object o) {
                        return eventType.isInstance(o);
                    }
                })
                .cast(eventType);
    }
}
