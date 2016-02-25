package com.lulee007.mocklocations.ui.activities;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.lulee007.mocklocations.R;
import com.lulee007.mocklocations.base.MLBaseActivity;
import com.lulee007.mocklocations.presenter.MainPresenter;
import com.lulee007.mocklocations.ui.views.DrawPanelView;
import com.lulee007.mocklocations.ui.views.EmulatorPanelView;
import com.lulee007.mocklocations.ui.views.IMainView;
import com.lulee007.mocklocations.util.DrawTool;
import com.lulee007.mocklocations.util.RxBus;
import com.nineoldandroids.animation.Animator;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class MainActivity extends MLBaseActivity implements IMainView {

    BaiduMap mBaiduMap;
    List<LatLng> points = new ArrayList<LatLng>();
    OverlayOptions ooPolyline;
    Polyline mPolyline;

    @Bind(R.id.tool_panel)
    RelativeLayout toolPanel;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.app_bar)
    AppBarLayout appBar;

    @Bind(R.id.fab_show_draw_panel)
    FloatingActionButton fabShowDrawPanel;
    @Bind(R.id.fab_show_emulator_panel)
    FloatingActionButton fabShowEmulatorPanel;
    @Bind(R.id.fabm_panel_switcher)
    FloatingActionsMenu fabmPanelSwitcher;
    @Bind(R.id.map_root)
    RelativeLayout mapRoot;

    private DrawPanelView drawPanelView;
    private EmulatorPanelView emulatorPanelView;

    private MainPresenter mainPresenter;
    private MapView mMapView;
    private boolean doubleClickExit;
    private DrawTool drawTool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mainPresenter = new MainPresenter(this);
        mainPresenter.initView();
//        mainPresenter.showDrawPanel();


//        mMapView.getMap().setOnMapLoadedCallback();


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

        Subscription mapPanSubscription = RxBus.getDefault().toObserverable(DrawPanelView.MapPanEvent.class)
                .subscribe(
                        new Action1<DrawPanelView.MapPanEvent>() {
                            @Override
                            public void call(DrawPanelView.MapPanEvent mapPanEvent) {
                                Logger.d("subscribe: toggle map pan, enable pan:%s", Boolean.toString(mapPanEvent.isPanEnabled()));
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
        addSubscription(mapPanSubscription);


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                //TODO about page
                return true;
            case R.id.action_exit:
                mainPresenter.exitApp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (fabmPanelSwitcher.isExpanded()) {
            fabmPanelSwitcher.collapse();
        }
        if (!doubleClickExit) {
            showToast("再按一次退出应用，后台运行请按 Home 键。");
            doubleClickExit = true;
            Observable.timer(2, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(new Func1<Long, Object>() {
                        @Override
                        public Object call(Long aLong) {
                            doubleClickExit = false;
                            return null;
                        }
                    }).subscribe();
        } else {
            mainPresenter.exitApp();
        }
    }

    @Override
    public void showDrawPanel() {
        if (emulatorPanelView != null && emulatorPanelView.isInEmulateMode()) {
            new SweetAlertDialog(this).setTitleText("注意")
                    .setContentText("正在模拟轨迹模式当中，要切换模式么？")
                    .setConfirmText("停止模拟并切换")
                    .setCancelText("取消切换")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            emulatorPanelView.cancelEmulate();
                            sweetAlertDialog.dismiss();
                            showPanel(DrawPanelView.class);
                        }
                    }).show();
        } else {
            showPanel(DrawPanelView.class);
        }
    }

    @Override
    public void showEmulatorPanel() {
        if (drawPanelView != null && drawPanelView.isInEditMode()) {
            new SweetAlertDialog(this).setTitleText("注意")
                    .setContentText("正在绘制轨迹模式当中，要切换模式么？")
                    .setConfirmText("不保存了并切换")
                    .setCancelText("取消切换")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            drawPanelView.cancelEdit();
                            sweetAlertDialog.dismiss();
                            showPanel(EmulatorPanelView.class);
                        }
                    }).show();

        } else {
            showPanel(EmulatorPanelView.class);
        }
    }


    private void showPanel(Class toShowClass) {

        fabmPanelSwitcher.collapse();

        final View toShowPanelView;
        final View toHidePanelView;

        Object toHide;

        if (toShowClass.equals(DrawPanelView.class)) {
            drawPanelView = drawPanelView != null ? drawPanelView : new DrawPanelView(toolPanel);
            toShowPanelView = drawPanelView.getDrawPanelView();

            toHide = emulatorPanelView;
            toHidePanelView = emulatorPanelView != null ? emulatorPanelView.getEmulatorPanelView() : null;

        } else {
            emulatorPanelView = emulatorPanelView != null ? emulatorPanelView : new EmulatorPanelView(toolPanel);
            toShowPanelView = emulatorPanelView.getEmulatorPanelView();

            toHide = drawPanelView;
            toHidePanelView = drawPanelView != null ? drawPanelView.getDrawPanelView() : null;

        }

        if (toolPanel.indexOfChild(toShowPanelView) != -1)
            return;

        boolean isNeedDelay = false;
        if (toHide != null) {
            isNeedDelay = true;
            YoYo.with(Techniques.FadeOutLeft)
                    .duration(200)
                    .withListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            toolPanel.removeView(toHidePanelView);
                            Logger.d("fade out and remove a view");
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    })
                    .playOn(toolPanel);
        }

        Observable.timer(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        toolPanel.addView(toShowPanelView);
                    }
                });

        YoYo.with(Techniques.SlideInRight)
                .duration(400)
                .delay(isNeedDelay ? 250 : 0)
                .playOn(toolPanel);
    }

    @Override
    public void initView() {

        /**
         * 初始化地图部分
         * 设置不显示地图缩放控件，比例尺控件
         * 设置地图初始化中心点
         */
        BaiduMapOptions options = new BaiduMapOptions();
        options.zoomControlsEnabled(false);
        options.scaleControlEnabled(false);
        mMapView = new MapView(this, options);
        RelativeLayout.LayoutParams params_map = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        mapRoot.addView(mMapView, params_map);
        mBaiduMap = mMapView.getMap();
        //设置初始位置
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(32.129927, 118.913191)));

        /**
         * 设置标题
         */
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("轨迹再现使用工具");

        drawTool = new DrawTool(mBaiduMap);
    }

    @Override
    public void exitApp() {
        //TODO stop gps service
        MobclickAgent.onKillProcess(this);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @OnClick({R.id.fab_show_draw_panel, R.id.fab_show_emulator_panel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.action_settings:
                //TODO setting page
                break;
            case R.id.fab_show_draw_panel:
                mainPresenter.showDrawPanel();
                break;
            case R.id.fab_show_emulator_panel:
                mainPresenter.showEmulatorPanel();
                break;
        }
    }
}
