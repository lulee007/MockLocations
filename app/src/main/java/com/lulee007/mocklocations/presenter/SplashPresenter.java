package com.lulee007.mocklocations.presenter;

import com.lulee007.mocklocations.ui.views.ISplashView;
import com.orhanobut.logger.Logger;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * User: lulee007@live.com
 * Date: 2016-03-01
 * Time: 10:20
 */
public class SplashPresenter {
    private ISplashView splashView;

    public SplashPresenter(ISplashView splashView) {

        this.splashView = splashView;
    }

    public void setAsFullScreen() {
        splashView.setAsFullScreen();
    }

    public void showWelcome() {
        splashView.showWelcome();
    }

    public void waitForEnd() {
        Observable.timer(3000, TimeUnit.MILLISECONDS)
                .map(new Func1<Long, Long>() {
                    @Override
                    public Long call(Long aLong) {
                        Logger.d("waiting end:%d ms", 3000);
                        return aLong;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                splashView.launchMainActivity();
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Logger.e(throwable, "wait for start main activity error");
                            }
                        }
                );
    }
}
