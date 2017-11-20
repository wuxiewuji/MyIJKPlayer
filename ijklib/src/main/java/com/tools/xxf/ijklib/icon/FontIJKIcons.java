package com.tools.xxf.ijklib.icon;

import com.joanzapata.iconify.Icon;

/**
 * TODO 模仿IJK官方图标,播放控件的图标库编写
 *
 * @author XXe
 *         Create Time : 2017/11/17 15:27
 */
public enum FontIJKIcons implements Icon {
    ijk_play('\ue6a4'),//播放开关
    ijk_stop('\ue6a5'),//暂停开关
    ijk_danma_Roll_on('\ue69f'),//滚动弹幕开
    ijk_danma_Roll_off('\ue69e'),//滚动弹幕关
    ijk_danma_bottom_on('\ue69d'),//底部弹幕开
    ijk_danma_bottom_oee('\ue69c'),//底部弹幕关
    ijk_danma_top_on('\ue69b'),//顶部弹幕开
    ijk_danma_top_oee('\ue69a'),//顶部弹幕关
    ijk_danma_on('\ue63f'),//弹幕开
    ijk_danma_oee('\ue620'),//弹幕关
    ijk_setting('\ue67e'),//设置
    ijk_collect_solid('\ue66a'),//收藏实心
    ijk_collect_hollow('\ue669'),//收藏空心
    ijk_full_max('\ue622'),//全屏
    ijk_full_narrow('\ue60d'),//退出全屏

    ijk_back('\ue64e'),//返回
    ijk_menu_point('\ue671'),//菜单 点

    ijk_dianzhan('\ue6c6'),//ic_赞
    ijk_cai('\ue6c5');//ic_踩

    char character;

    FontIJKIcons(char character) {
        this.character = character;
    }

    public String key() {
        return this.name().replace('_', '-');
    }

    public char character() {
        return this.character;
    }
}
