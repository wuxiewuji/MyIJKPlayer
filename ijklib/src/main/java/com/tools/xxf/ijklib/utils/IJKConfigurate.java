package com.tools.xxf.ijklib.utils;

import android.content.Context;

/**
 * TODO
 *
 * @author XXF
 *         Create Time : 2017/11/17 10:43
 */
public class IJKConfigurate {

    private Context context;

    private IJKConfigurate() {

    }

    public IJKConfigurate getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final IJKConfigurate INSTANCE = new IJKConfigurate();
    }

    public final void initIjk(Context context) {
        this.context = context;
    }

    public final Context getContext() {
        return context;
    }

}
