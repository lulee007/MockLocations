package com.lulee007.mocklocations.presenter;

import android.Manifest;
import android.os.Build;

import com.lulee007.mocklocations.ui.views.ISplashView;
import com.lulee007.mocklocations.util.PermissionsChecker;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private String tips="";

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
                                endSplashPage();
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

    public void endSplashPage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAppPermission();
        } else {
            Logger.d("android version small then 23, skip check");
            splashView.launchMainActivity();
        }
    }

    private void checkAppPermission() {
        final Map<String, String> permissionTips = new HashMap<>();
        permissionTips.put(Manifest.permission.ACCESS_FINE_LOCATION, "获取位置信息以在地图上显示；");
        permissionTips.put(Manifest.permission.READ_PHONE_STATE, "获取设备识别码以正常使用百度地图；");
        permissionTips.put(Manifest.permission.READ_EXTERNAL_STORAGE, "获得读取存储权限以加载轨迹文件到地图中；");
        permissionTips.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "获得写入存储权限以保存轨迹文件；");


        Observable.from(permissionTips.keySet())
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return  splashView.checkPermission(s);

                    }
                })
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Logger.d("需要获取的权限：%s", s);
                        tips += "\r\n - " + permissionTips.get(s);
                    }
                })
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<List<String>>() {
                            @Override
                            public void call(final List<String> strings) {
                                if (strings.isEmpty()) {
                                    splashView.launchMainActivity();
                                    return;
                                }
                                splashView.requestPermissions(strings.toArray(new String[0]), tips);
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Logger.e(throwable, "error in check permissions");

                            }
                        }
                );
    }
}
