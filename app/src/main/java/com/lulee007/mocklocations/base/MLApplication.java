package com.lulee007.mocklocations.base;

import android.app.Application;


import com.lulee007.mocklocations.util.DrawerImageLoaderHelper;
import com.lulee007.mocklocations.util.IconFontUtil;
import com.mcxiaoke.packer.helper.PackerNg;
import com.mikepenz.iconics.Iconics;
import com.mikepenz.iconics.typeface.GenericFont;
import com.orhanobut.logger.Logger;
import com.squareup.leakcanary.LeakCanary;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

import im.fir.sdk.FIR;

/**
 * User: lulee007@live.com
 * Date: 2015-12-07
 * Time: 17:03
 */
public class MLApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DrawerImageLoaderHelper.init();

        //pretty logger
        Logger.init("XTAppLog");

        //内存泄露检测
        LeakCanary.install(this);

        FIR.init(this);

        // 如果没有使用PackerNg打包添加渠道，默认返回的是""
        // com.mcxiaoke.packer.helper.PackerNg
        final String market = PackerNg.getMarket(this);
        // 或者使用 PackerNg.getMarket(Context,defaultValue)
        // 之后就可以使用了，比如友盟可以这样设置
        Logger.d("=====market:{%s}=====",market);
        AnalyticsConfig.setChannel(market);
        MobclickAgent.setDebugMode(true);

    }

}
