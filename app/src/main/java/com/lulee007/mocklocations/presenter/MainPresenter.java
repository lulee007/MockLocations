package com.lulee007.mocklocations.presenter;

import android.support.annotation.NonNull;

import com.lulee007.mocklocations.ui.views.IMainView;

/**
 * User: lulee007@live.com
 * Date: 2016-02-24
 * Time: 21:52
 */
public class MainPresenter {

    IMainView mainView;

    public MainPresenter(@NonNull IMainView mainView){
        this.mainView=mainView;
    }

    public void showDrawPanel(){
        mainView.showDrawPanel();
    }

    public void showEmulatorPanel() {
        mainView.showEmulatorPanel();
    }

    public void initView() {
        mainView.initView();
    }

    public void exitApp() {
        mainView.exitApp();
    }
}
