package com.lulee007.mocklocations.util;

import android.location.Location;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.google.gson.Gson;
import com.lulee007.mocklocations.R;
import com.lulee007.mocklocations.ui.views.EmulatorPanelView;
import com.lulee007.mocklocations.util.coordtransform.CPoint;
import com.orhanobut.logger.Logger;

import java.util.List;

import rx.Subscription;
import rx.functions.Action1;

/**
 * User: lulee007@live.com
 * Date: 2016-02-27
 * Time: 19:59
 */
public class MockLocationHelper {

    private BaiduMap baiduMap;
    private Subscription locationSubscription;

    public MockLocationHelper(BaiduMap baiduMap) {

        this.baiduMap = baiduMap;
        init();
    }

    public void init() {
        locationSubscription = RxBus.getDefault().toObserverable(MockLocationService.LocationChangedEvent.class)
                .subscribe(
                        new Action1<MockLocationService.LocationChangedEvent>() {
                            @Override
                            public void call(MockLocationService.LocationChangedEvent locationChangedEvent) {
                                Location location = locationChangedEvent.getLocation();
                                Logger.d("接收到位置信息：%s", new Gson().toJson(location));
                                MyLocationData locData = new MyLocationData.Builder()
                                        .accuracy(location.getAccuracy())
                                                // 此处设置开发者获取到的方向信息，顺时针0-360
                                        .direction(location.getBearing()).latitude(location.getLatitude())
                                        .longitude(location.getLongitude()).build();
                                // 设置定位数据
                                baiduMap.setMyLocationData(locData);
                                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.ic_nav);
                                MyLocationConfiguration myLocationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, bitmapDescriptor);
                                baiduMap.setMyLocationConfigeration(myLocationConfiguration);
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Logger.e(throwable, "LocationChangedEvent event error");
                            }
                        }
                );
        RxBus.getDefault().toObserverable(EmulatorPanelView.EmulatorPanelEvent.class)
                .subscribe(
                        new Action1<EmulatorPanelView.EmulatorPanelEvent>() {
                            @Override
                            public void call(EmulatorPanelView.EmulatorPanelEvent emulatorPanelEvent) {
                                switch (emulatorPanelEvent.getState()) {
                                    case OPEN_FILE:
                                        loadGpsData(emulatorPanelEvent.getData());
                                        break;
                                    case START:
                                        start();
                                        break;
                                    case PAUSE:
                                        pause();
                                        break;
                                    case HOLDING:
                                        holding();
                                        break;
                                    case CONTINUE:
                                        toContinue();
                                        break;
                                    case STOP:
                                        stop();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Logger.e(throwable, "EmulatorPanelEvent event error");
                            }
                        }
                );
    }

    public void loadGpsData(List<CPoint> data) {
        Logger.d("MockLocation Helper loadfile");

    }

    public void start() {
        Logger.d("MockLocation Helper onStart");
    }

    public void pause() {
        Logger.d("MockLocation Helper onPause");
    }

    public void holding() {
        Logger.d("MockLocation Helper onHolding");
    }

    public void toContinue() {
        Logger.d("MockLocation Helper onContinue");
    }

    public void stop() {
        Logger.d("MockLocation Helper onStop");
    }

}
