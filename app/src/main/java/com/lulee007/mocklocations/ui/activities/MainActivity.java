package com.lulee007.mocklocations.ui.activities;

import android.os.Bundle;
import android.widget.RelativeLayout;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;
import com.lulee007.mocklocations.R;
import com.lulee007.mocklocations.base.MLBaseActivity;
import com.lulee007.mocklocations.ui.views.DrawPanelView;
import com.lulee007.mocklocations.util.RxBus;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MainActivity extends MLBaseActivity {

    MapView mMapView = null;
    BaiduMap mBaiduMap;
    List<LatLng> points = new ArrayList<LatLng>();
    OverlayOptions ooPolyline;
    Polyline mPolyline;
    @Bind(R.id.tool_panel)
    RelativeLayout toolPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        DrawPanelView drawPanelView = new DrawPanelView(toolPanel);
        toolPanel.addView(drawPanelView.getDrawPanelView());

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);

        mBaiduMap = mMapView.getMap();
        //设置初始位置
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(32.129927, 118.913191)));
        // 构造折线点坐标

//        ooPolyline = new PolylineOptions().width(6).points(points);

//        //添加在地图中
//        mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
//
//        mBaiduMap.getUiSettings().setScrollGesturesEnabled(false);
//        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
//            @Override
//            public void onTouch(MotionEvent motionEvent) {
//                LatLng xy = mBaiduMap.getProjection().fromScreenLocation(new Point((int) motionEvent.getX(), (int) motionEvent.getY()));
//                Log.d("xy", String.format("x:%f   y:%f", xy.longitude, xy.latitude));
//                double dis = DistanceUtil.getDistance(xy, points.get(points.size() - 1));
//                if (dis > 0.2) {
//                    Log.d("distance", String.format("%f", dis));
//                    points.add(xy);
//                    ooPolyline = new PolylineOptions().width(10)
//                            .points(points);
//                    mPolyline.remove();
//                    mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
//                }
//            }
//        });

        Subscription subscription = RxBus.getDefault().toObserverable(DrawPanelView.MapPanEvent.class)
                .subscribe(
                        new Action1<DrawPanelView.MapPanEvent>() {
                            @Override
                            public void call(DrawPanelView.MapPanEvent mapPanEvent) {
                                Logger.d("subscribe: toggle map pan, enable pan:%s",Boolean.toString(mapPanEvent.isPanEnabled()));
                                mBaiduMap.getUiSettings().setScrollGesturesEnabled(mapPanEvent.isPanEnabled());
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Logger.e(throwable, "map pan event error");
                            }
                        }
                );
        addSubscription(subscription);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

}
