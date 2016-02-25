package com.lulee007.mocklocations.util;

import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.google.gson.Gson;
import com.lulee007.mocklocations.ui.views.DrawPanelView;
import com.lulee007.mocklocations.util.coordtransform.CPoint;
import com.lulee007.mocklocations.util.coordtransform.CoordinateConversion;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * User: lulee007@live.com
 * Date: 2016-02-25
 * Time: 16:26
 */
public class DrawTool {
    private BaiduMap mBaiduMap;
    private List<LatLng> mPoints;
    private PolylineOptions mPolylineOptions;
    private Polyline mPolyline;
    private boolean isInEditMode;
    private Subscription drawActionSubscription;


    public DrawTool(BaiduMap baiduMap) {

        mBaiduMap = baiduMap;
        init();
    }

    public void begin() {
        Logger.d("进入新的编辑！");
        Logger.json(new Gson().toJson(mPoints));

        isInEditMode = true;
        mPoints.clear();
        mBaiduMap.clear();

    }

    public void complete() {
        isInEditMode = false;
        Logger.d("完成编辑！");
        Logger.json(new Gson().toJson(mPoints));
    }

    public void cancel() {
        Logger.d("取消编辑！");
        Logger.json(new Gson().toJson(mPoints));

        isInEditMode = false;
        mPoints.clear();
        mBaiduMap.clear();
    }

    public void save() {
        isInEditMode = false;
        Logger.d("保存编辑！");
        Logger.json(new Gson().toJson(mPoints));
//        Observable.from(mPoints)
//                .map(new Func1<LatLng, CPoint>() {
//                    @Override
//                    public CPoint call(LatLng latLng) {
//                        CPoint gps= CoordinateConversion.bg2gps(latLng.longitude, latLng.latitude);
//                        return gps;
//                    }
//                })
//                .toList()
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .doOnNext(new Action1<List<CPoint>>() {
//                    @Override
//                    public void call(List<CPoint> CPoints) {
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                })

    }

    public void onPause() {
        isInEditMode = false;
    }

    public void onContinue() {
        isInEditMode = true;
    }

    private void init() {
        mPoints = new ArrayList<>();

        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                if (!isInEditMode)
                    return;
                LatLng xy = mBaiduMap.getProjection().fromScreenLocation(new Point((int) motionEvent.getX(), (int) motionEvent.getY()));
                Log.d("xy", String.format("x:%f   y:%f", xy.longitude, xy.latitude));
                CPoint gps= CoordinateConversion.bg2gps(xy.longitude, xy.latitude);
                Log.d("gps", String.format("x:%f   y:%f", gps.getLng(), gps.getLat()));
                if (mPoints.size() == 1) {
                    // 添加圆 作为起点
                    OverlayOptions ooCircle = new CircleOptions().fillColor(0x000000FF)
                            .center(xy).stroke(new Stroke(10, 0xAA000000))
                            .radius(10);
                    mBaiduMap.addOverlay(ooCircle);
                    mPoints.add(xy);
                    return;
                } else if (mPoints.size() == 0) {
                    mPoints.add(xy);
                    return;
                }
                double dis = DistanceUtil.getDistance(xy, mPoints.get(mPoints.size() - 1));
                if (dis > 0.2) {
                    Log.d("distance", String.format("%f", dis));
                    mPoints.add(xy);

                    mPolylineOptions = new PolylineOptions().width(10)
                            .points(mPoints);
                    Polyline oldPolyline = mPolyline;
                    mPolyline = (Polyline) mBaiduMap.addOverlay(mPolylineOptions);
                    if (oldPolyline != null) {
                        oldPolyline.remove();
                    }
                }
            }
        });

        drawActionSubscription = RxBus.getDefault().toObserverable(DrawPanelView.DrawActionEvent.class)
                .subscribe(
                        new Action1<DrawPanelView.DrawActionEvent>() {
                            @Override
                            public void call(DrawPanelView.DrawActionEvent drawActionEvent) {
                                Logger.d("subscribe: DrawActionEvent:%s", drawActionEvent.getDrawMode().toString());
                                switch (drawActionEvent.getDrawMode()) {
                                    case TO_START:
                                        cancel();
                                        break;
                                    case STARTED:
                                        begin();
                                        break;
                                    case SAVE:
                                        save();
                                        break;
                                    case COMPLETED:
                                        complete();
                                        break;
                                    case TO_PAN:
                                        onContinue();
                                        break;
                                    case PANNING:
                                        onPause();
                                        break;
                                    default:
                                        break;
                                }

                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Logger.e(throwable, "map pan event error");
                            }
                        }
                );
    }
}
