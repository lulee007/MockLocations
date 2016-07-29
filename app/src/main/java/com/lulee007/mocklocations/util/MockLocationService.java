package com.lulee007.mocklocations.util;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.lulee007.mocklocations.model.CPoint;

import java.util.ArrayList;
import java.util.List;

public class MockLocationService extends Service implements
        LocationListener {

    private static String LOG_TAG = "MockLocationService";
    private static List<String> gpsData;
    private static LocationManager locationManager;

    private static MockLocationThread mockLocationThread;
    private static String mocLocationProvider;
    private static boolean finished = false;
    private static boolean pause = false;
    private static boolean wait = false;
    private static boolean destroyed = false;
    private static int sequence = 1000;
    private static final int MinSequence = 1000;

    @Override
    public void onStart(Intent intent, int startId) {
        Log.v(LOG_TAG, "onStart!");
        destroyed = false;
    }

    @Override
    public void onCreate() {
        Log.v(LOG_TAG, "onCreate!");
        super.onCreate();
        initParams();
    }

    /**
     * 初始化 mocklocation 所需变量
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void initParams() {
        gpsData = new ArrayList<>();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mocLocationProvider = LocationManager.GPS_PROVIDER;
        locationManager.addTestProvider(mocLocationProvider, false, false,
                false, false, true, true, true, 0, 50);
        locationManager.setTestProviderEnabled(mocLocationProvider, true);


        locationManager.requestLocationUpdates(mocLocationProvider, 0, 0, this);
    }

    /**
     * 开始模拟GPS位置服务 如果在运行的话,先停止之前的线程; 使用新的坐标数据，并开始新的线程
     *
     * @param data
     */
    public static void startMockLocation(List data) {
        initParams(0);
        if (data == null) {
            Log.e(LOG_TAG, "gpsdata is null,thread will not be started!");
            return;
        }
        if (mockLocationThread == null) {
            mockLocationThread = new MockLocationThread();
            Log.v(LOG_TAG, "create a new  thread !");
        }
        // 第一次开始线程
        if (mockLocationThread != null) {
            if (gpsData != null)
                gpsData.clear();
            else
                gpsData = new ArrayList<>();
            gpsData.addAll(data);
            if (finished && mockLocationThread.isAlive()) {
                finished = false;
            } else {
                mockLocationThread.start();
                finished = false;
            }
            Log.v(LOG_TAG, "try to start a thread!");
        } else {
            Log.v(LOG_TAG, "start thread field!");
        }
        if (gpsData != null) {
            for (String str : gpsData) {
                Log.v("gpsdata", str);
            }
        }
    }

    /**
     * 阻塞当前线程，使之一直不发送Location
     */
    public static void pauseMockLocation() {
        initParams(2);

    }

    /**
     * 阻塞当前线程，继续发送Location
     */
    public static void continueMockLocation() {
        initParams(3);
    }

    /**
     * 阻塞当前线程，使之使用发送相同坐标的Location
     */
    public static void waitMockLocation() {
        initParams(1);
    }

    /**
     * 终止掉当前发送模拟GPS位置的服务
     */
    public static void stopMockLocation() {
        initParams(0);
    }

    /**
     * 根据ID操作线程 0 finished; 1 pause; 2 wait;3 continue
     *
     * @param operId 操作线程状态
     */
    public static void initParams(int operId) {
        finished = false;
        pause = false;
        wait = false;
        switch (operId) {
            case 0:
                finished = true;
                Log.v("MockLocationThread", "finished = true");
                break;
            case 1:
                pause = true;
                Log.v("MockLocationThread", "pause = true");
                break;
            case 2:
                wait = true;
                Log.v("MockLocationThread", "wait = true");
                break;
            case 3:
                Log.v("MockLocationThread", "continue ");
                break;

        }

    }

    public static void changeGPSSpeed(double d) {
        if (d <= 0) {
            Log.v(LOG_TAG, "speed < 0");
            return;
        }
        MockLocationService.sequence = (int) (MinSequence / d);
        Log.v(LOG_TAG, MockLocationService.sequence + " ---sequence");
    }

    /**
     * 根据需要将模拟坐标传给GPS，
     *
     * @author Lulee007
     */
    public static class MockLocationThread extends Thread {

        public MockLocationThread() {

        }

        @Override
        public void run() {
            CPoint p1Point, p2Point = new CPoint();
            new CPoint();
            if (gpsData == null || gpsData.size() <= 0)
                return;
            int curCoordIndex = 0;
            Location location = null;
            while (!destroyed) {// 由于无法多次对thread进行start，所以只能用标志位来表示是否停止线程
                if (!finished) {
                    try {
                        Thread.sleep(sequence);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!wait) {
                        if (!pause) {// 生成新的模拟坐标
                            try {
                                String str = null;
                                if (curCoordIndex > gpsData.size() - 1) {
                                    gpsData = changeDataOrder(gpsData);
                                    curCoordIndex = 0;
                                }

                                str = gpsData.get(curCoordIndex);
                                if (str == null)
                                    continue;
                                curCoordIndex++;
                                String[] parts = str.split(",");
                                if (parts.length != 2)
                                    continue;
                                Double latitude = Double.valueOf(parts[1]);
                                Double longitude = Double.valueOf(parts[0]);
                                if (curCoordIndex == 0 || p2Point == null) {
                                    p2Point = new CPoint(longitude, latitude);
                                }
                                p1Point = new CPoint(longitude, latitude);
                                float bearing = (float) getAngleBy2Point(p2Point, p1Point);
                                location = new Location(mocLocationProvider);
                                location.setLatitude(latitude);
                                location.setLongitude(longitude);
                                location.setTime(System.currentTimeMillis());
                                location.setAccuracy((int) (Math.random() * 50));
                                location.setExtras(null);
                                location.setBearing(bearing);
                                location.setSpeed(3.0f);
                                if(Build.VERSION.SDK_INT > 16){
                                    location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                                }
                                Log.v("MockLocationThread", curCoordIndex + ":"
                                        + location.toString());
                                p2Point = new CPoint(p1Point.getLng(), p1Point.getLat());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        if (location != null)// 发送模拟坐标
                        {
                            try {
                                locationManager.setTestProviderLocation(
                                        mocLocationProvider, location);
                            } catch (SecurityException se) {
                                finished = true;
                                Log.e(LOG_TAG, " the ACCESS_MOCK_LOCATION permission is not present or the Settings.Secure.ALLOW_MOCK_LOCATION} system setting is not enabled");
                            }catch (Exception ex){
                                Log.e(LOG_TAG,"set test provider location error",ex);
                            }
                        }

                    }

                } else {
                    curCoordIndex = 0;
                    try {
                        Thread.sleep(sequence);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

        private List<String> changeDataOrder(List<String> gpsData) {
            Log.v(LOG_TAG, "datachanged");
            List<String> data = new ArrayList<String>(gpsData);
            for (int i = 0; i < gpsData.size(); i++) {
                data.set(i, gpsData.get(gpsData.size() - i - 1));
            }
            return data;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "onDestroy");
        destroyed = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        RxBus.getDefault().send(new LocationChangedEvent(location));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    public class LocationChangedEvent {
        public Location getLocation() {
            return location;
        }

        private Location location;

        public LocationChangedEvent(Location location) {

            this.location = location;
        }
    }

    public static double getAngleBy2Point(CPoint p1, CPoint p2) {
        // 前进中的2点
        // 用于图转动的角度
        double x = p2.getLng() - p1.getLng();
        double y = p2.getLat() - p1.getLat();
        // int type = 0;//1:第1象限 2:第2象限 3:第3象限 4:第4象限
        double result = 0.0;

        if (x > 0) {
            if (y > 0) {
                // 第一象限
                // result = Math.atan(x / y) / Math.PI * 180;
                result = Math.atan(x / y) / Math.PI * 180;
            } else if (y < 0) {
                // 第四象限
                // result = 180 - Math.atan(-x / y) / Math.PI * 180;
                result = 180 - Math.atan(x / -y) / Math.PI * 180;
            } else {
                result = 90;
            }
        } else if (x < 0) {
            if (y < 0) {
                // 第三象限
                // result = Math.atan(x / y) / Math.PI * 180 - 180;
                result = Math.atan(-x / -y) / Math.PI * 180 - 180;
            } else if (y > 0) {
                // 第二象限
                // result = -Math.tan(-x / y) / Math.PI * 180;
                result = -Math.atan(-x / y) / Math.PI * 180;
            } else {
                result = -90;
            }
        } else {
            if (y > 0) {
                result = 0;
            } else if (y < 0) {
                result = 180;
            } else
                result = 0;
        }
        return result;
    }

}