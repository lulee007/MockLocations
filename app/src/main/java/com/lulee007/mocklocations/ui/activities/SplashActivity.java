package com.lulee007.mocklocations.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.lulee007.mocklocations.R;
import com.lulee007.mocklocations.base.MLBaseActivity;
import com.lulee007.mocklocations.presenter.SplashPresenter;
import com.lulee007.mocklocations.ui.views.ISplashView;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

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
}