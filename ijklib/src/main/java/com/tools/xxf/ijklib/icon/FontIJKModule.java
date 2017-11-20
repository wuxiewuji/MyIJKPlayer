package com.tools.xxf.ijklib.icon;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconFontDescriptor;

/**
 * TODO 自定义的字体图标库,负者加载如阿里图标库内选择的图标的ttr文件(assets文件夹下面)
 *
 * @author XXF
 *         Create Time : 2017/11/17 15:24
 */
public class FontIJKModule implements IconFontDescriptor {
    @Override
    public String ttfFileName() {
        return "iconify/iconfont.ttf";
    }

    @Override
    public Icon[] characters() {
        return FontIJKIcons.values();
    }
}
