package com.tools.xxf.ijkplayer.ui;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ViewStubCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tools.xxf.ijkplayer.R;
import com.tools.xxf.ijkplayer.common.MediaController;
import com.tools.xxf.ijkplayer.common.PlayerManager;
import com.tools.xxf.ijkplayer.common.PlayerManager.*;
import com.tools.xxf.ijkplayer.utils.MyLogger;
import com.tools.xxf.ijkplayer.widget.media.IjkVideoView;

/**
 * TODO:播放器完成版本
 * @author XXF
 * Create Time : 2017/11/1 15:11
 */
public class MainActivity extends AppCompatActivity implements PlayerManager.PlayerStateListener {
    public static final String TAG = "PlayerManager";
    public MyLogger logger = MyLogger.getXiongFengLog();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = (IjkVideoView) findViewById(R.id.video_view);
        mediaController = (MediaController) findViewById(R.id.media_controller);

        player = new PlayerManager(this, videoView);
        player.setMediaController(mediaController);
        player.setScaleType(PlayerManager.SCALETYPE_FILLPARENT);
        player.setPlayerStateListener(this);
        player.play("http://221.4.223.101:8000/media/49_720p.flv");

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
        player.setScrollTextListener(new ScrollTextListener() {
            //进度
            @Override
            public void showSeekRewindInfo(FRtype type, String s) {
                showForwardRewindInfo(type, s);
            }

            //声音
            @Override
            public void showVolBrightnessInfo(VBtype type, int progress, int lenght) {
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
    }

    private void showolBrightnessInfo(PlayerManager.VBtype type, int progress, int lenght) {

        ViewStubCompat vs = (ViewStubCompat) findViewById(R.id.player_vol_brightness_stub);
        if (vs != null) {
            vbView = vs.inflate();
            vbImageView = (ImageView) findViewById(R.id.player_vol_brightness_image);
            vbSeekbar = (SeekBar) findViewById(R.id.player_vod_brightness_seek);
        }

        if (VBtype.VOL == type && 0 == progress)
            vbImageView.setImageResource(R.drawable.icon_voice_no);
        else if (VBtype.VOL == type && progress > 0)
            vbImageView.setImageResource(R.drawable.icon_voice);
        else if (VBtype.BRIGHTNESS == type && 0 == progress)
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
            if (FRtype.INCREASE == type)
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
        logger.d("onPrepared");
        player.start();
    }

    @Override
    public void onLoading() {
        logger.d("onLoading");
    }

    @Override
    public void onPlay() {
        logger.d("onPlay");
    }

    @Override
    public void onError() {
        logger.d("onPlay");
    }

    @Override
    public void onComplete() {
        logger.d("onComplete");
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
        logger.d("onConfigurationChanged");
    }
}
