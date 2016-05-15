package com.lulee007.mocklocations.ui.views;

public interface ISplashView {
    void setAsFullScreen();
    void showWelcome();

    void launchMainActivity();

    void requestPermissions(String[] permissions, String tips);

    Boolean checkPermission(String s);
}
