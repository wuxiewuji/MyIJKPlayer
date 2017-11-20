package com.tools.xxf.ijklib.utils;

import android.content.Context;

import com.orhanobut.logger.Logger;

/**
 * TODO
 *
 * @author XXF
 *         Create Time : 2017/11/17 10:09
 */
public class UIUtils {
    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取导航栏高度
     *
     * @param context
     * @return
     */
    public static int getDaoHangHeight(Context context) {
        int result = 0;
        int resourceId = 0;
        int rid = context.getResources().getIdentifier("config_showNavigationBar", "bool",
                "android");
        if (rid != 0) {
            resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen",
                    "android");
            Logger.d("高度：" + resourceId);
            Logger.d("高度：" + context.getResources().getDimensionPixelSize(resourceId) + "");
            return context.getResources().getDimensionPixelSize(resourceId);
        } else
            return 0;
    }
}
