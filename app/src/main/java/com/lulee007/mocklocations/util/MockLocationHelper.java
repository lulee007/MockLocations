package com.lulee007.mocklocations.util;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.google.gson.Gson;
import com.lulee007.mocklocations.BuildConfig;
import com.lulee007.mocklocations.ui.views.EmulatorPanelView;
import com.orhanobut.logger.Logger;

import java.util.List;

import rx.Subscription;
import rx.functions.Action1;

/**
 * User: lulee007@live.com
 * Date: 2016-02-27
 * Time: 19:59
 * <p>接收从 {@link EmulatorPanelView} 发送的控制信息，控制{@link MockLocationService}模拟线程状态
 * start,pause,etc.
 * <p>接收从 {@link MockLocationService} 发送来的最新位置信息，显示到 {@link BaiduMap} 当中.
 */
public class MockLocationHelper {

    private Context context;
    private BaiduMap baiduMap;
    private Subscription locationSubscription;
    private List<String> data;


    public MockLocationHelper(Context context, BaiduMap baiduMap) {
        this.context = context;

        this.baiduMap = baiduMap;
        init();
    }

    private void init() {
        locationSubscription = RxBus.getDefault().toObserverable(MockLocationService.LocationChangedEvent.class)
                .subscribe(
                        new Action1<MockLocationService.LocationChangedEvent>() {
                            @Override
                            public void call(MockLocationService.LocationChangedEvent locationChangedEvent) {
                                Location location = locationChangedEvent.getLocation();
                                Logger.d("接收到位置信息：%s", new Gson().toJson(location));
                                baiduMap.setMyLocationEnabled(true);
                                // 将GPS设备采集的原始GPS坐标转换成百度坐标
                                CoordinateConverter converter = new CoordinateConverter();
                                converter.from(CoordinateConverter.CoordType.GPS);
// sourceLatLng待转换坐标
                                converter.coord(new LatLng(location.getLatitude(), location.getLongitude()));
                                LatLng desLatLng = converter.convert();
                                MyLocationData locData = new MyLocationData.Builder()
                                        .accuracy(location.getAccuracy())
                                        // 此处设置开发者获取到的方向信息，顺时针0-360
                                        .direction(location.getBearing()).latitude(desLatLng.latitude)
                                        .longitude(desLatLng.longitude).build();
                                // 设置定位数据
                                baiduMap.setMyLocationData(locData);
//                                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.ic_nav);
                                MyLocationConfiguration myLocationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, null);
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
                                        //MainActivity will take this case
//                                        loadGpsData(emulatorPanelEvent.getData());
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

    public void loadGpsData(List<String> data) {
        this.data = data;
        Logger.d("MockLocation Helper loadfile");
    }

    public void start() {
        Logger.d("MockLocation Helper onStart");
        MockLocationService.startMockLocation(this.data);
    }

    public void pause() {
        Logger.d("MockLocation Helper onPause");
        MockLocationService.pauseMockLocation();

    }

    public void holding() {
        Logger.d("MockLocation Helper onHolding");
        MockLocationService.waitMockLocation();

    }

    public void toContinue() {
        Logger.d("MockLocation Helper onContinue");
        MockLocationService.continueMockLocation();
    }

    public void stop() {
        Logger.d("MockLocation Helper onStop");
        MockLocationService.stopMockLocation();
    }


    /**
     * 启动 GPS模拟服务
     */
    public void startMockLocationService() {
        Intent serviceIntent = new Intent(context,
                MockLocationService.class);
        if (checkMockLocationService())
            context.stopService(serviceIntent);
        context.startService(serviceIntent);
        Logger.d("try to start a service ");
    }

    public boolean isMockLocationSet() {

        boolean isMockLocation = false;
        try {
            //if marshmallow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AppOpsManager opsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                isMockLocation = (opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), BuildConfig.APPLICATION_ID) == AppOpsManager.MODE_ALLOWED);
            } else {
                // in marshmallow this will always return true
                isMockLocation = !android.provider.Settings.Secure.getString(context.getContentResolver(), "mock_location").equals("0");
            }
        } catch (Exception e) {
            return isMockLocation;
        }
        Logger.d("MockLocation is enable?:%s", isMockLocation);

        return isMockLocation;

    }

    /**
     * 检查MockLocationService是否已经启动
     */
    public boolean checkMockLocationService() {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(Integer.MAX_VALUE);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(
                    MockLocationService.class.getName())) {
                Logger.d("find old service");
                return true;
            }
        }
        return false;
    }

    public void endService() {
        Intent serviceIntent = new Intent(context,
                MockLocationService.class);
        if (checkMockLocationService())
            context.stopService(serviceIntent);
    }
}
