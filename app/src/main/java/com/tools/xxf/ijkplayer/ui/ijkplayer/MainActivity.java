package com.tools.xxf.ijkplayer.ui.ijkplayer;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ViewStubCompat;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.tools.xxf.ijklib.common.MediaController;
import com.tools.xxf.ijklib.common.PlayerManager;
import com.tools.xxf.ijklib.media.IjkVideoView;
import com.tools.xxf.ijkplayer.R;

/**
 * TODO:播放器完成版本
 * @author XXF
 * Create Time : 2017/11/1 15:11
 */
public class MainActivity extends AppCompatActivity implements PlayerManager.PlayerStateListener {
    public static final String TAG = "PlayerManager";
    private static final String URL="http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8";
    private IjkVideoView videoView;
    private PlayerManager player;
    private MediaController mediaController;

    //vol brighness info
    private View vbView;
    private ImageView vbImageView;
    private SeekBar vbSeekbar;
    // seek
    private View forwardRewindView;
    private ImageView forwardRewindImageView;
    private TextView forwardRewindText;
    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = (IjkVideoView) findViewById(R.id.video_view);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        mediaController = (MediaController) findViewById(R.id.media_controller);
        player = new PlayerManager(this, videoView);
        player.setMediaController(mediaController);
        player.setScaleType(PlayerManager.SCALETYPE_FILLPARENT);
        player.setPlayerStateListener(this);
        player.play(URL);
        mProgress.setVisibility(View.VISIBLE);
        initListener();
    }

    //监听事件
    private void initListener() {
        if (null != mediaController)
            mediaController.setMediaListener(new MediaController.MediaControllerListener() {
                @Override
                public void playBackClick(View v) {
                    player.onBackPressed();//点击返回调用控制类里面的返回
                }

                @Override
                public void danmuClick(View v) {
                    Display display = getWindowManager().getDefaultDisplay();

                    int screenWidth = display.getWidth();
                    int screenHeight = ScreenUtils.getScreenHeight();
                    int navBarHeight = BarUtils.getNavBarHeight();
                    LogUtils.a("screenWidth="+screenWidth+",screenHeight="+screenHeight+",=navBarHeight"+navBarHeight);
                }

                @Override
                public void fullClick(View v) {
                    player.onExpendScreen();//全屏
                }

                @Override
                public void addClick(View v) {

                }

                @Override
                public void shareClick(View v) {

                }
            });
        //当前控件滑动事件,声音,亮度,快进等
        player.setScrollTextListener(new PlayerManager.ScrollTextListener() {
            //进度
            @Override
            public void showSeekRewindInfo(PlayerManager.FRtype type, String s) {
                showForwardRewindInfo(type, s);
            }

            //声音
            @Override
            public void showVolBrightnessInfo(PlayerManager.VBtype type, int progress, int lenght) {
                showolBrightnessInfo(type, progress, lenght);
            }

            @Override
            public void setScrollFinish() {//滑动设置音量\屏幕亮度\等完成
                if (vbView != null)
                    vbView.setVisibility(View.INVISIBLE);
                if (forwardRewindView != null)
                    forwardRewindView.setVisibility(View.INVISIBLE);
            }

        });

        player.onError(new PlayerManager.OnErrorListener() {
            @Override
            public void onError(int what, int extra) {
                LogUtils.d(what+","+extra);
            }
        });

    }

    private void showolBrightnessInfo(PlayerManager.VBtype type, int progress, int lenght) {

        ViewStubCompat vs = (ViewStubCompat) findViewById(R.id.player_vol_brightness_stub);
        if (vs != null) {
            vbImageView = (ImageView) findViewById(R.id.player_vol_brightness_image);
            vbSeekbar = (SeekBar) findViewById(R.id.player_vod_brightness_seek);
        }

        if (PlayerManager.VBtype.VOL == type && 0 == progress)
            vbImageView.setImageResource(R.drawable.icon_voice_no);
        else if (PlayerManager.VBtype.VOL == type && progress > 0)
            vbImageView.setImageResource(R.drawable.icon_voice);
        else if (PlayerManager.VBtype.BRIGHTNESS == type && 0 == progress)
            vbImageView.setImageResource(R.drawable.icon_light_no);
        else
            vbImageView.setImageResource(R.drawable.icon_light);

        vbSeekbar.setMax(lenght);
        vbSeekbar.setProgress(progress);
        vbView.setVisibility(View.VISIBLE);
    }

    /**
     * 快进快退布局显示
     *
     * @param type :
     *             PlayerManager.FRtype.INCREASE :增加
     *             PlayerManager.FRtype.DECREASE :减少
     * @param info :当前布局显示的内容
     */
    private void showForwardRewindInfo(PlayerManager.FRtype type, String info) {
        if (null == forwardRewindView) {
            ViewStubCompat vs = (ViewStubCompat) findViewById(R.id
                    .player_forward_rewind_stub);
            if (vs != null) {
                vs.inflate();
                forwardRewindView = findViewById(R.id.player_forward_rewind_root);
                forwardRewindImageView = (ImageView) findViewById(R.id
                        .player_forward_rewind_image);
                forwardRewindText = (TextView) findViewById(R.id
                        .player_forward_rewind_text);
            }
        } else {
            if (PlayerManager.FRtype.INCREASE == type)
                forwardRewindImageView.setImageResource(R.drawable.play_kuaijin);
            else
                forwardRewindImageView.setImageResource(R.drawable.play_kuaitui);
            forwardRewindText.setText(info);
            forwardRewindView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return player.onTouchEvent(event);
    }

    /*********播放器监听*******************************/
    @Override
    public void onPrepared() {//播放器准备完成
        LogUtils.d("onPrepared");
        player.start();
        mProgress.setVisibility(View.GONE);
    }

    @Override
    public void onLoading() {
        LogUtils.d("onLoading");
    }

    @Override
    public void onPlay() {
        LogUtils.d("onPlay");
    }

    @Override
    public void onError() {
        LogUtils.d("onError");
    }

    @Override
    public void onComplete() {
        LogUtils.d("onComplete");
        mediaController.show(0);
    }


    @Override
    public void onBack() {
        Log.i(TAG, "竖屏的时候,点击让我回到上一页");
    }


    /*********生命周期同步*******************************/
    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.onPause();
        }
    }

    @Override
    public void onBackPressed() {
        player.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.onDestroy();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtils.d("onConfigurationChanged");
    }
}
