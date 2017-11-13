package com.tools.xxf.ijkplayer;

import android.app.Application;
import android.content.Context;

/**
 * Created by HuangJie on 2017/6/23.
 */

public class BaseApplication extends Application {

    private static Context mContext;//上下文

    //应用程序的入口
    @Override
    public void onCreate() {
        super.onCreate();

        //上下文
        mContext = this;
    }


    public static Context getContext() {
        return mContext;
    }

}
