package com.lulee007.mocklocations.ui.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.lulee007.mocklocations.R;
import com.lulee007.mocklocations.base.MLBaseActivity;
import com.lulee007.mocklocations.presenter.SplashPresenter;
import com.lulee007.mocklocations.ui.views.ISplashView;
import com.lulee007.mocklocations.util.MLConstant;
import com.lulee007.mocklocations.util.PermissionsChecker;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class SplashActivity extends MLBaseActivity implements ISplashView {

    @Bind(R.id.rl_road_bg)
    RelativeLayout rlRoadBg;
    @Bind(R.id.iv_road)
    ImageView ivRoad;
    @Bind(R.id.tv_app_name)
    TextView tvAppName;
    private SplashPresenter splashPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        splashPresenter = new SplashPresenter(this);
        splashPresenter.setAsFullScreen();
        splashPresenter.showWelcome();
        splashPresenter.waitForEnd();
    }

    @Override
    public void setAsFullScreen() {
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        this.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

    @Override
    public void showWelcome() {
        ivRoad.setVisibility(View.GONE);
        rlRoadBg.setVisibility(View.GONE);
        tvAppName.setVisibility(View.GONE);
        Observable.timer(700, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        ivRoad.setVisibility(View.VISIBLE);
                        rlRoadBg.setVisibility(View.VISIBLE);
                        tvAppName.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.Landing)
                                .duration(500)
                                .playOn(rlRoadBg);
                        YoYo.with(Techniques.RotateInDownRight)
                                .duration(700)
                                .playOn(ivRoad);
                        YoYo.with(Techniques.FlipInX)
                                .duration(1100)
                                .playOn(tvAppName);

                    }
                });


    }

    @Override
    public void launchMainActivity() {
        startActivity(MainActivity.class);
        finish();
    }


    @Override
    public void requestPermissions(final String[] permissions, final String tips) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(SplashActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("注意")
                .setConfirmText("去开启")
                .setContentText("权限")
                .setCancelText("拒绝并退出")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        PermissionsActivity.startActivityForResult(SplashActivity.this, MLConstant.ACTIVITY_REQUEST_CODE.PERMISSIONS, permissions);

                    }
                })
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        finish();
                    }
                });
        sweetAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                SweetAlertDialog alertDialog = (SweetAlertDialog) dialog;
                TextView text = (TextView) alertDialog.findViewById(R.id.content_text);
//                android.view.ViewGroup.LayoutParams layoutParams = text.getLayoutParams();
//                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                text.setGravity(Gravity.LEFT);
//                text.setLayoutParams(layoutParams);
                text.setText("本应用需要以下权限才能正常使用：" + tips);
            }
        });
        sweetAlertDialog.show();

    }

    @Override
    public Boolean checkPermission(String s) {
         PermissionsChecker permissionsChecker = new PermissionsChecker(this);
        return permissionsChecker.lacksPermissions(s);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拒绝时, 关闭页面, 缺少主要权限, 无法运行
        if (requestCode == MLConstant.ACTIVITY_REQUEST_CODE.PERMISSIONS && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish();
        } else {
            launchMainActivity();
        }
    }
}