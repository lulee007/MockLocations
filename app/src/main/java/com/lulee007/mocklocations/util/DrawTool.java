package com.lulee007.mocklocations.util;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.google.gson.Gson;
import com.lulee007.mocklocations.R;
import com.lulee007.mocklocations.ui.views.DrawPanelView;
import com.lulee007.mocklocations.model.CPoint;
import com.lulee007.mocklocations.util.coordtransform.CoordinateConversion;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.functions.Action1;

/**
 * User: lulee007@live.com
 * Date: 2016-02-25
 * Time: 16:26
 * 接收来自{@link DrawPanelView}的绘制控制信息 {@link com.lulee007.mocklocations.ui.views.DrawPanelView.DrawActionEvent}
 * 控制地图{@link BaiduMap}是否可以移动地图，绘制轨迹
 */
public class DrawTool {
    private BaiduMap mBaiduMap;
    private Context mContext;
    private List<LatLng> mPoints;
    private PolylineOptions mPolylineOptions;
    private Polyline mPolyline;
    /**
     * 是否可以绘制
     */
    private boolean isInEditMode;
    private Subscription drawActionSubscription;


    public DrawTool(BaiduMap baiduMap, Context mContext) {

        mBaiduMap = baiduMap;
        this.mContext = mContext;
        init();
    }

    public void begin() {
        Logger.d("进入新的编辑！");
        mPoints.clear();
        mBaiduMap.clear();

    }

    public void complete() {
        Logger.d("完成编辑！");
        Logger.json(new Gson().toJson(mPoints));
        //TODO 绘制终点到地图上

    }

    public void cancel() {
        Logger.d("取消编辑！");
        mPoints.clear();
        mBaiduMap.clear();
    }

    public void save() {
        Logger.d("保存编辑！");
        new GpsJsonFileHelper(mContext).saveJson(mPoints);
    }

    public void onPause() {
    }

    public void onContinue() {
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
                CPoint gps = CoordinateConversion.bg2gps(xy.longitude, xy.latitude);
                Log.d("gps", String.format("x:%f   y:%f", gps.getLng(), gps.getLat()));
                if (mPoints.size() == 1) {
                    // 添加圆 作为起点
                    //准备 marker 的图片
                    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.point);
                    OverlayOptions ooCircle = new MarkerOptions()
                            .position(xy)
                            .animateType(MarkerOptions.MarkerAnimateType.grow)
                            .icon(bitmap);
                    mBaiduMap.addOverlay(ooCircle);
                    mPoints.add(xy);
                    return;
                } else if (mPoints.size() == 0) {
                    mPoints.add(xy);
                    return;
                }
                double dis = DistanceUtil.getDistance(xy, mPoints.get(mPoints.size() - 1));
                if (dis > 0.05) {
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
                                Logger.d("subscribe: DrawActionEvent：%s enable pan:%s",drawActionEvent.getDrawMode().toString(), Boolean.toString(drawActionEvent.isCanPanMap()));
                                mBaiduMap.getUiSettings().setScrollGesturesEnabled(drawActionEvent.isCanPanMap());
                                isInEditMode = !drawActionEvent.isCanPanMap();
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
