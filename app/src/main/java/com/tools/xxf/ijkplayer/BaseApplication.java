package com.tools.xxf.ijkplayer;

import android.app.Application;
import android.content.Context;

import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.fonts.IoniconsModule;
import com.tools.xxf.configlib.config.Libconfig;
import com.tools.xxf.ijklib.icon.FontIJKModule;

/**
 * Created by HuangJie on 2017/6/23.
 */

public class BaseApplication extends Application {

    //应用程序的入口
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化框架类
        Libconfig.init(this)
                .withIcon(new FontAwesomeModule())
                .withIcon(new IoniconsModule())
                .withIcon(new FontIJKModule())
                .configure();
    }

}
