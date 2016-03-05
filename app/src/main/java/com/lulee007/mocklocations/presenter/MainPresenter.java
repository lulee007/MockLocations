package com.lulee007.mocklocations.presenter;

import android.support.annotation.NonNull;

import com.baidu.mapapi.map.BaiduMapOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lulee007.mocklocations.base.MLBasePresenter;
import com.lulee007.mocklocations.model.CPoint;
import com.lulee007.mocklocations.ui.views.IMainView;
import com.orhanobut.logger.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * User: lulee007@live.com
 * Date: 2016-02-24
 * Time: 21:52
 */
public class MainPresenter extends MLBasePresenter{

    IMainView mainView;

    public MainPresenter(@NonNull IMainView mainView) {
        this.mainView = mainView;
    }

    public void showDrawPanel() {
        mainView.showDrawPanel();
    }

    public void showEmulatorPanel() {
        mainView.showEmulatorPanel();
    }

    public void initView() {
        mainView.initView();
    }

    public void exitApp() {
        mainView.exitApp();
    }

    public void openMockLocationSetting() {
        mainView.openMockLocationSetting();
    }

    public void checkMockLocationSetting() {
        mainView.checkMockLocationSetting();
    }

    public void configBaiduMap() {
        BaiduMapOptions options = new BaiduMapOptions();
        options.zoomControlsEnabled(false);
        options.scaleControlEnabled(false);
        options.rotateGesturesEnabled(false);
        mainView.configBaiduMap(options);
    }

    public void processJson(String filePath) {
        Subscription loadJsonSubscription = Observable.just(filePath)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<String, Observable<CPoint>>() {
                    @Override
                    public Observable<CPoint> call(String s) {
                        List<CPoint> points = new ArrayList<CPoint>();
                        try {
                            points = new Gson().fromJson(new FileReader(s), new TypeToken<ArrayList<CPoint>>() {
                            }.getType());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        return Observable.from(points);
                    }
                })
                .map(new Func1<CPoint, String>() {
                    @Override
                    public String call(CPoint cPoint) {
                        return String.format("%f,%f", cPoint.getLng(), cPoint.getLat());
                    }
                })
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<List<String>>() {
                            @Override
                            public void call(List<String> cPoints) {
                                mainView.processJson(cPoints);
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Logger.e(throwable, "load json error");
                            }
                        }
                );
        addSubscription(loadJsonSubscription);
    }

    @Override
    protected void onLoadMoreComplete(List items) {

    }

    @Override
    protected HashMap<String, String> buildRequestParams(String where, int skip) {
        return null;
    }

    @NonNull
    @Override
    protected HashMap<String, String> buildRequestParams(String where) {
        return null;
    }

    @Override
    public void loadNew() {

    }

    @Override
    public void refresh() {

    }

    @Override
    public void loadMore() {

    }

    public void showAbout() {
        mainView.showAbout();
    }

    public void switchToBackground() {
        mainView.switchToBackground();
    }


}
