package com.tools.xxf.ijkplayer;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.tools.xxf.ijkplayer.common.PlayerManager;
import com.tools.xxf.ijkplayer.widget.MediaController;
import com.tools.xxf.ijkplayer.widget.MyPlayerManager;
import com.tools.xxf.ijkplayer.widget.media.AndroidMediaController;
import com.tools.xxf.ijkplayer.widget.media.IjkVideoView;

public class MainActivity extends AppCompatActivity implements MyPlayerManager.PlayerStateListener {

    private IjkVideoView videoView;
    private MyPlayerManager player;
    private FrameLayout ffView;
    private LinearLayout view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ffView = (FrameLayout) findViewById(R.id.ffplayer);
        videoView = (IjkVideoView) findViewById(R.id.video_view);
        MediaController mediaController = (MediaController) findViewById(R.id.media_controller);
        videoView.setMediaController(mediaController);
        view = (LinearLayout) findViewById(R.id.view);
        player = new MyPlayerManager(this);
        player.setScaleType(PlayerManager.SCALETYPE_FILLPARENT);
        player.setPlayerStateListener(this);
        player.setMediaController(mediaController);
        player.play("http://221.4.223.101:8000/media/49_720p.flv");
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                Log.i("show", "show");
                return player.gestureDetector.onTouchEvent(event);
            }
        });
    }


    private int getScreenOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && height >
                width ||
                (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && width >
                        height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }
        return orientation;
    }

    /**
     * 横竖屏判断
     */
    private boolean isPortrait() {
        int orientation = getScreenOrientation();
        boolean portrait = orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
                orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        return portrait;
    }

    // small full screen
    private int playerNormalHeight = 0;

    /**
     * 横竖屏切换，实现全屏
     */
    private void onExpendScreen() {
        if (isPortrait()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ffView.getLayoutParams();
            if (0 == playerNormalHeight)
                playerNormalHeight = params.height;
            params.height = LinearLayout.LayoutParams.MATCH_PARENT;
            ffView.requestLayout();

        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ffView.getLayoutParams();
            params.height = playerNormalHeight;
            ffView.requestLayout();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onError() {

    }

    @Override
    public void onLoading() {

    }

    @Override
    public void onPlay() {

    }
}
