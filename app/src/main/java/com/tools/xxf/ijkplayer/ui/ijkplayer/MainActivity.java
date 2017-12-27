package com.tools.xxf.ijkplayer.ui.ijkplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.res.Configuration;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ViewStubCompat;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.jaeger.library.StatusBarUtil;
import com.tools.xxf.ijklib.common.MediaController;
import com.tools.xxf.ijklib.common.PlayerManager;
import com.tools.xxf.ijklib.media.AndroidMediaController;
import com.tools.xxf.ijklib.media.IjkVideoView;
import com.tools.xxf.ijkplayer.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * TODO:播放器完成版本
 *
 * @author XXF
 *         Create Time : 2017/11/1 15:11
 */
public class MainActivity extends AppCompatActivity implements PlayerManager.PlayerStateListener, ViewTreeObserver.OnGlobalLayoutListener {
    public static final String TAG = "PlayerManager";
    private static final String URL = "http://117.144.248.49/HDhnws.m3u8?authCode=07110409322147352675&amp;" +
            "stbId=006001FF0018120000060019F0D49A1&amp;Contentid=6837496099179515295&amp;" +
            "mos=jbjhhzstsl&amp;livemode=1&amp;channel-id=wasusyt";
    //    private static final String URL="http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8";
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
    private HashMap<Integer, String> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setImmerseLayout();
        getStatusMsg();
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

    /**
     * IJKPlayer 返回码字符串组装
     */
    private void getStatusMsg() {
        int MEDIA_INFO_UNKNOWN = 1;//未知信息
        int MEDIA_INFO_STARTED_AS_NEXT = 2;//播放下一条
        int MEDIA_INFO_VIDEO_RENDERING_START = 3;//视频开始整备中
        int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;//视频日志跟踪
        int MEDIA_INFO_BUFFERING_START = 701;//开始缓冲中
        int MEDIA_INFO_BUFFERING_END = 702;//缓冲结束
        int MEDIA_INFO_NETWORK_BANDWIDTH = 703;//网络带宽，网速方面
        int MEDIA_INFO_BAD_INTERLEAVING = 800;//
        int MEDIA_INFO_NOT_SEEKABLE = 801;//不可设置播放位置，直播方面
        int MEDIA_INFO_METADATA_UPDATE = 802;//
        int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
        int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;//不支持字幕
        int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;//字幕超时

        int MEDIA_INFO_VIDEO_INTERRUPT = -10000;//数据连接中断
        int MEDIA_INFO_VIDEO_ROTATION_CHANGED = 10001;//视频方向改变
        int MEDIA_INFO_AUDIO_RENDERING_START = 10002;//音频开始整备中

        int MEDIA_ERROR_UNKNOWN = 1;//未知错误
        int MEDIA_ERROR_SERVER_DIED = 100;//服务挂掉
        int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;//数据错误没有有效的回收
        int MEDIA_ERROR_IO = -1004;//IO 错误
        int MEDIA_ERROR_MALFORMED = -1007;
        int MEDIA_ERROR_UNSUPPORTED = -1010;//数据不支持
        int MEDIA_ERROR_TIMED_OUT = -110;//数据超时

        map = new HashMap<>();
        map.put(MEDIA_INFO_UNKNOWN, "未知信息");
        map.put(MEDIA_INFO_STARTED_AS_NEXT, "播放下一条");
        map.put(MEDIA_INFO_VIDEO_RENDERING_START, "视频开始整备中");
        map.put(MEDIA_INFO_VIDEO_TRACK_LAGGING, "视频日志跟踪");
        map.put(MEDIA_INFO_BUFFERING_START, "开始缓冲中");
        map.put(MEDIA_INFO_BUFFERING_END, "缓冲结束");
        map.put(MEDIA_INFO_NETWORK_BANDWIDTH, "网络带宽，网速方面");
        map.put(MEDIA_INFO_BAD_INTERLEAVING, "800");
        map.put(MEDIA_INFO_NOT_SEEKABLE, "801");
        map.put(MEDIA_INFO_METADATA_UPDATE, "802");
        map.put(MEDIA_INFO_TIMED_TEXT_ERROR, "900");
        map.put(MEDIA_INFO_UNSUPPORTED_SUBTITLE, "不支持字幕");
        map.put(MEDIA_INFO_SUBTITLE_TIMED_OUT, "字幕超时");
        map.put(MEDIA_INFO_VIDEO_INTERRUPT, "数据连接中断");
        map.put(MEDIA_INFO_VIDEO_ROTATION_CHANGED, "视频方向改变");
        map.put(MEDIA_INFO_AUDIO_RENDERING_START, "音频开始整备中");
        map.put(MEDIA_ERROR_UNKNOWN, "未知错误");
        map.put(MEDIA_ERROR_SERVER_DIED, "服务挂掉");
        map.put(MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK, "数据错误没有有效的回收");
        map.put(MEDIA_ERROR_IO, "IO 错误");
        map.put(MEDIA_ERROR_MALFORMED, "-1007");
        map.put(MEDIA_ERROR_UNSUPPORTED, "数据不支持");
        map.put(MEDIA_ERROR_TIMED_OUT, "-110");
    }

    //设置状态栏透明
    protected void setImmerseLayout() {// view为标题栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
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
                    LogUtils.a("screenWidth=" + screenWidth + ",screenHeight=" + screenHeight + ",=navBarHeight" + navBarHeight);
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


                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("error:"+map.get(what)).show();
            }
        });

    }

    /**
     * 声音或者亮度
     *
     * @param type
     * @param progress
     * @param lenght
     */
    private void showolBrightnessInfo(PlayerManager.VBtype type, int progress, int lenght) {

        ViewStubCompat vs = (ViewStubCompat) findViewById(R.id.player_vol_brightness_stub);
        if (vs != null) {
            vbView = vs.inflate();
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
        finish();
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

    @Override
    public void onGlobalLayout() {
        LogUtils.d("onGlobalLayout");
    }
}
