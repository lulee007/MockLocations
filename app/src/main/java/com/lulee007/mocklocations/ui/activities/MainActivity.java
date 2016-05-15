package com.lulee007.mocklocations.ui.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.lulee007.mocklocations.R;
import com.lulee007.mocklocations.base.MLBaseActivity;
import com.lulee007.mocklocations.presenter.MainPresenter;
import com.lulee007.mocklocations.ui.views.DrawPanelView;
import com.lulee007.mocklocations.ui.views.EmulatorPanelView;
import com.lulee007.mocklocations.ui.views.IMainView;
import com.lulee007.mocklocations.util.DrawTool;
import com.lulee007.mocklocations.util.GpsJsonFileHelper;
import com.lulee007.mocklocations.util.MLConstant;
import com.lulee007.mocklocations.util.MockLocationHelper;
import com.lulee007.mocklocations.util.RxBus;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.nineoldandroids.animation.Animator;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class MainActivity extends MLBaseActivity implements IMainView, FileChooserDialog.FileCallback {

    BaiduMap mBaiduMap;

    @Bind(R.id.tool_panel)
    RelativeLayout toolPanel;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
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
    private MockLocationHelper mockLocationHelper;


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

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainPresenter.unSubscribeAll();
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
                mainPresenter.showAbout();
                return true;
            case R.id.action_exit:
                mainPresenter.exitApp();
                return true;
            case R.id.action_settings:
                //TODO setting page
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (fabmPanelSwitcher.isExpanded()) {
            fabmPanelSwitcher.collapse();
            return;
        }
        if (emulatorPanelView != null && emulatorPanelView.isInEmulateMode()) {
            new MaterialDialog.Builder(this)
                    .title(R.string.exit_tips)
                    .content(R.string.keep_running_in_bg)
                    .positiveText(R.string.keep_running_in_bg_ok)
                    .negativeText(R.string.keep_running_in_bg_cancel)
                    .neutralText(R.string.force_exit)
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mainPresenter.exitApp();
                        }
                    })
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mainPresenter.switchToBackground();
                        }
                    })
                    .show();
            return;
        }

        if (!doubleClickExit) {
            showToast("再按一次退出应用。");
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MLConstant.ACTIVITY_REQUEST_CODE.JSON_FILES) {
            if (resultCode == RESULT_OK) {
                Logger.d("RESULT_OK: MLConstant.ACTIVITY_REQUEST_CODE.JSON_FILES");
                String filePath = data.getStringExtra(LocationFilesActivity.BUNDLE_KEY_JSON_FILE);
                if (filePath != null)
                    mainPresenter.processJson(filePath);

            } else if (resultCode == RESULT_CANCELED) {
                //do nothing
                Logger.d("RESULT_CANCELED: MLConstant.ACTIVITY_REQUEST_CODE.JSON_FILES");
            }
        } else if (requestCode == MLConstant.ACTIVITY_REQUEST_CODE.MOCK_LOCATION) {
            if (mockLocationHelper.isMockLocationSet()) {
                mockLocationHelper.startMockLocationService();
                mainPresenter.showEmulatorPanel();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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
                    .setConfirmText("不保存，并切换")
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
        //设置标题
        setActionBarWithTitle(toolbar, getResources().getString(R.string.app_name));

        //设置地图
        mainPresenter.configBaiduMap();

        //绑定轨迹绘制工具
        drawTool = new DrawTool(mBaiduMap, this);

        //设置初始位置
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(32.129927, 118.913191)));

        //绑定位置模拟显示当前位置，控制模拟线程
        mockLocationHelper = new MockLocationHelper(this, mBaiduMap);

        RxBus.getDefault().toObserverable(EmulatorPanelView.EmulatorPanelEvent.class)
                .subscribe(
                        new Action1<EmulatorPanelView.EmulatorPanelEvent>() {
                            @Override
                            public void call(EmulatorPanelView.EmulatorPanelEvent emulatorPanelEvent) {
                                if (emulatorPanelEvent.getState() == EmulatorPanelView.EmulatorPanelState.OPEN_FILE) {
                                    new FileChooserDialog.Builder(MainActivity.this)
                                            .chooseButton(R.string.choose_file)  // changes label of the choose button
                                            .cancelButton(R.string.cancel)
                                            .initialPath(GpsJsonFileHelper.sAppFolder)  // changes initial path, defaults to external storage directory
                                            .mimeType("text/plain") // Optional MIME type filter

                                            .tag("choose-location-file-id")
                                            .show();

                                    //startActivityForResult(new Intent(MainActivity.this, LocationFilesActivity.class), MLConstant.ACTIVITY_REQUEST_CODE.JSON_FILES);
                                } else {
                                    // ignore , mocklocation helper will take these
                                }
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Logger.e(throwable, "on EmulatorPanelEvent Error in MainAc");
                            }
                        }
                );
    }

    @Override
    public void exitApp() {
        mockLocationHelper.endService();
        MobclickAgent.onKillProcess(this);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void openMockLocationSetting() {
        Intent appDeploymentIntent = new Intent(
                android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        startActivityForResult(appDeploymentIntent,
                MLConstant.ACTIVITY_REQUEST_CODE.MOCK_LOCATION);
    }

    @Override
    public void checkMockLocationSetting() {
        if (mockLocationHelper.isMockLocationSet()) {
            // TODO
            // mockLocationHelper.startMockLocationService();
        } else {
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("提醒")
                    .setContentText("模拟位置功能尚未开启，现在去开启？")
                    .setConfirmText("去开启")
                    .setCancelText("以后再说")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            mainPresenter.openMockLocationSetting();

                        }
                    })
                    .show();
        }
    }

    /**
     * 初始化地图部分
     * 设置不显示地图缩放控件，比例尺控件
     * 设置地图初始化中心点
     *
     * @param options
     */
    @Override
    public void configBaiduMap(BaiduMapOptions options) {

        mMapView = new MapView(this, options);
        RelativeLayout.LayoutParams params_map = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        mapRoot.addView(mMapView, params_map);
        mBaiduMap = mMapView.getMap();
    }

    @Override
    public void processJson(List<String> cPoints) {
        mockLocationHelper.loadGpsData(cPoints);
        emulatorPanelView.onFileOpened();
    }

    @Override
    public void showAbout() {
        new LibsBuilder()
                .withAboutIconShown(true)
                .withAboutVersionShown(true)
                .withAboutDescription(getResources().getString(R.string.about_description))
                .withActivityTitle("关于")
                .withActivityTheme(R.style.AppTheme_AboutLibrary)
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .start(this);
    }

    @Override
    public void switchToBackground() {
        PackageManager pm = getPackageManager();
        ResolveInfo homeInfo = pm.resolveActivity(
                new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_HOME), 0);
        ActivityInfo ai = homeInfo.activityInfo;
        Intent startIntent = new Intent(Intent.ACTION_MAIN);
        startIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        startIntent
                .setComponent(new ComponentName(ai.packageName, ai.name));
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startIntent);
    }

    @OnClick({R.id.fab_show_draw_panel, R.id.fab_show_emulator_panel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_show_draw_panel:
                mainPresenter.showDrawPanel();
                break;
            case R.id.fab_show_emulator_panel:
                if (mockLocationHelper.isMockLocationSet()) {
                    mainPresenter.showEmulatorPanel();
                } else {
                    mainPresenter.checkMockLocationSetting();
                }
                break;
        }
    }

    @Override
    public void onFileSelection(@NonNull FileChooserDialog dialog, @NonNull File file) {
        dialog.dismiss();
        mainPresenter.processJson(file.getAbsolutePath());
    }

}
