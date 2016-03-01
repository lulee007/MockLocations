package com.lulee007.mocklocations.ui.views;

import java.util.List;

/**
 * User: lulee007@live.com
 * Date: 2016-02-24
 * Time: 10:50
 */
public interface IMainView {

    void showDrawPanel();
    void showEmulatorPanel();

    void initView();

    void exitApp();

    void openMockLocationSetting();

    void checkMockLocationSetting();

    void configBaiduMap();

    void processJson(List<String> cPoints);
}
