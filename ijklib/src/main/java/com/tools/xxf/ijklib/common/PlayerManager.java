package com.tools.xxf.ijklib.common;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tools.xxf.ijklib.R;
import com.tools.xxf.ijklib.media.IMediaController;
import com.tools.xxf.ijklib.media.IRenderView;
import com.tools.xxf.ijklib.media.IjkVideoView;
import com.tools.xxf.ijklib.media.Permissions;
import com.tools.xxf.ijklib.utils.MyLogger;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static com.tools.xxf.ijklib.utils.TimeUtils.stringForTime;


/**
 * TODO 播放器控制类
 * 管理,屏幕滑动点击等相关事件,屏幕横竖屏切换等相关事件
 *
 * @author XXF
 *         Create Time : 2017/10/30 13:51
 */
public class PlayerManager {
    private MyLogger logger = MyLogger.getXiongFengLog();
    /**
     * 可能会剪裁,保持原视频的大小，显示在中心,当原视频的大小超过view的大小超过部分裁剪处理
     */
    public static final String SCALETYPE_FITPARENT = "fitParent";
    /**
     * 可能会剪裁,等比例放大视频，直到填满View为止,超过View的部分作裁剪处理
     */
    public static final String SCALETYPE_FILLPARENT = "fillParent";
    /**
     * 将视频的内容完整居中显示，如果视频大于view,则按比例缩视频直到完全显示在view中
     */
    public static final String SCALETYPE_WRAPCONTENT = "wrapContent";
    /**
     * 不剪裁,非等比例拉伸画面填满整个View
     */
    public static final String SCALETYPE_FITXY = "fitXY";
    /**
     * 不剪裁,非等比例拉伸画面到16:9,并完全显示在View中
     */
    public static final String SCALETYPE_16_9 = "16:9";
    /**
     * 不剪裁,非等比例拉伸画面到4:3,并完全显示在View中
     */
    public static final String SCALETYPE_4_3 = "4:3";
    /**
     * 状态常量
     */
    private final int STATUS_ERROR = -1;
    private final int STATUS_IDLE = 0;
    private final int STATUS_LOADING = 1;
    private final int STATUS_PLAYING = 2;
    private final int STATUS_PAUSE = 3;
    private final int STATUS_COMPLETED = 4;
    private final int STATUS_PREPARED = 5;

    private final Activity activity;

    private final IjkVideoView videoView;
    private final AudioManager audioManager;
    private final int mMaxVolume;
    public final GestureDetector gestureDetector;
    private int status = STATUS_IDLE;
    private PlayerStateListener playerStateListener;
    private boolean isLive = false;//是否为直播
    private boolean fullScreenOnly;
    private boolean playerSupport;
    private long pauseTime;
    private int currentPosition;
    private IMediaController controller;
    private DisplayMetrics screen = new DisplayMetrics();

    //Touch Events
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_VOLUME = 1;
    private static final int TOUCH_BRIGHTNESS = 2;
    private static final int TOUCH_MOVE = 3;
    private static final int TOUCH_SEEK = 4;
    private int touchAction = TOUCH_NONE;
    private float percent;//滑动距离占屏幕的百分比,用于亮度、声音、进度等调节

    private float brightness = -1;
    private int volume = -1;
    private long newPosition = -1;
    private boolean isFirstBrightnessGesture;
    // small full screen
    private int playerNormalHeight = 0;
    //长按开启或锁定页面
    private boolean isLockUI;//是否锁定UI页面

    //滑动声音\亮度\进度监听返回
    private ScrollTextListener scrollTextListener;


    public PlayerManager(final Activity activity, IjkVideoView videoView) {
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
            playerSupport = true;
        } catch (Throwable e) {
            Log.e("GiraffePlayer", "loadLibraries error", e);
        }

        this.activity = activity;

        this.videoView = videoView;
        initListener();
        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        activity.getWindowManager().getDefaultDisplay().getMetrics(screen);
        gestureDetector = new GestureDetector(activity, new PlayerGestureListener());
        if (fullScreenOnly) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        if (!playerSupport) {
            logger.e("播放器不支持此设备");
        }
    }

    /**
     * videoView播放相关监听事件
     */
    private void initListener() {
        videoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                statusChange(STATUS_COMPLETED);
                onCompleteListener.onComplete();
            }
        });
        videoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                statusChange(STATUS_ERROR);
                onErrorListener.onError(what, extra);
                return true;
            }
        });
        videoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                switch (what) {
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        statusChange(STATUS_LOADING);
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        statusChange(STATUS_PLAYING);
                        break;
                    case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                        //显示下载速度
//                      Toast.show("download rate:" + extra);
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        statusChange(STATUS_PLAYING);
                        break;
                }
                onInfoListener.onInfo(what, extra);
                return false;
            }
        });
        //初始化完成
        videoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                statusChange(STATUS_PREPARED);
                if (controller != null) {
                    controller.show();
                }
            }
        });
    }


    /***生命周期开始****************************************/
    public void play(String url) {
        if (playerSupport) {
            videoView.setVideoPath(url);
        }
    }

    /**
     * 暂停
     */
    public void onPause() {
        pauseTime = System.currentTimeMillis();
        if (status == STATUS_PLAYING) {
            videoView.pause();
            if (!isLive) {
                currentPosition = videoView.getCurrentPosition();
            }
        }
    }

    /**
     * 继续播放
     */
    public void onResume() {
        pauseTime = 0;
        if (status == STATUS_PLAYING) {
            if (isLive) {
                videoView.seekTo(0);
            } else {
                if (currentPosition > 0) {
                    videoView.seekTo(currentPosition);
                }
            }
            videoView.start();
        }
    }

    public void start() {
        videoView.start();
    }

    public void pause() {
        videoView.pause();
    }

    public void stop() {
        videoView.stopPlayback();
    }

    public void onDestroy() {
        videoView.stopPlayback();
    }

    /***内部相关****************************************/
    public void onBackPressed() {
        if (isPortrait()) {//此处返回操作
            if (playerStateListener != null) {
                playerStateListener.onBack();
            }
        } else {
            onExpendScreen();
        }
    }

    /**
     * 播放器支持此设备么
     *
     * @return
     */
    public boolean isPlayerSupport() {
        return playerSupport;
    }

    /**
     * 是否正在播放
     *
     * @return
     */
    public boolean isPlaying() {
        return videoView != null ? videoView.isPlaying() : false;
    }


    public int getCurrentPosition() {
        return videoView.getCurrentPosition();
    }

    /**
     * get video duration
     *
     * @return
     */
    public int getDuration() {
        return videoView.getDuration();
    }

    /**
     * 横竖屏判断
     *
     * @return
     */
    private int getScreenOrientation() {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
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

    /***相关功能设置****************************************/
    //直播设置
    public PlayerManager live(boolean isLive) {
        this.isLive = isLive;
        return this;
    }


    //控件设置
    public void setMediaController(IMediaController controller) {
        this.controller = controller;
        videoView.setMediaController(controller);
    }

    /**
     * <pre>
     *     fitParent:可能会剪裁,保持原视频的大小，显示在中心,当原视频的大小超过view的大小超过部分裁剪处理
     *     fillParent:可能会剪裁,等比例放大视频，直到填满View为止,超过View的部分作裁剪处理
     *     wrapContent:将视频的内容完整居中显示，如果视频大于view,则按比例缩视频直到完全显示在view中
     *     fitXY:不剪裁,非等比例拉伸画面填满整个View
     *     16:9:不剪裁,非等比例拉伸画面到16:9,并完全显示在View中
     *     4:3:不剪裁,非等比例拉伸画面到4:3,并完全显示在View中
     * </pre>
     *
     * @param scaleType
     */
    public void setScaleType(String scaleType) {
        if (SCALETYPE_FITPARENT.equals(scaleType)) {
            videoView.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
        } else if (SCALETYPE_FILLPARENT.equals(scaleType)) {
            videoView.setAspectRatio(IRenderView.AR_ASPECT_FILL_PARENT);
        } else if (SCALETYPE_WRAPCONTENT.equals(scaleType)) {
            videoView.setAspectRatio(IRenderView.AR_ASPECT_WRAP_CONTENT);
        } else if (SCALETYPE_FITXY.equals(scaleType)) {
            videoView.setAspectRatio(IRenderView.AR_MATCH_PARENT);
        } else if (SCALETYPE_16_9.equals(scaleType)) {
            videoView.setAspectRatio(IRenderView.AR_16_9_FIT_PARENT);
        } else if (SCALETYPE_4_3.equals(scaleType)) {
            videoView.setAspectRatio(IRenderView.AR_4_3_FIT_PARENT);
        }
    }


    //进度调节
    private void onProgressSlide(float percent, boolean seek) {
        long position = videoView.getCurrentPosition();
        long duration = videoView.getDuration();
        long deltaMax = Math.min(100 * 1000, duration - position);
        long delta = (long) (deltaMax * percent);

        newPosition = delta + position;
        if (newPosition > duration) {
            newPosition = duration;
        } else if (newPosition <= 0) {
            newPosition = 0;
            delta = -position;
        }
        int showDelta = (int) delta / 1000;
        if (showDelta != 0) {
            String text = showDelta > 0 ? ("+" + showDelta) : "" + showDelta;
            logger.d("onProgressSlide:" + text);
        }

        FRtype type;
        if (delta > 0)
            type = FRtype.INCREASE;
        else
            type = FRtype.DECREASE;
        if (seek && duration > 0) {
            videoView.seekTo((int) newPosition);
        }

        if (duration > 0 && scrollTextListener != null)
            scrollTextListener.showSeekRewindInfo(type, stringForTime((int) (newPosition)) + "/" +
                    stringForTime((int) duration));

        if (0 == duration && seek)
            Toast.makeText(activity, R.string.unseekable_stream, Toast.LENGTH_SHORT).show();
    }


    /**
     * 滑动改变亮度
     *
     * @param percent 屏幕百分比值
     */
    private void doBrightnessTouch(float percent) {
        if (touchAction != TOUCH_NONE && touchAction != TOUCH_BRIGHTNESS)
            return;
        if (isFirstBrightnessGesture) initBrightnessTouch();
        touchAction = TOUCH_BRIGHTNESS;

        changeBrightness(percent);
    }

    private void initBrightnessTouch() {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        float brightnesstemp = lp.screenBrightness != -1f ? lp.screenBrightness : 0.6f;
        // Initialize the layoutParams screen brightness
        try {
            if (Settings.System.getInt(activity.getContentResolver(), Settings.System
                    .SCREEN_BRIGHTNESS_MODE) ==
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                if (!Permissions.canWriteSettings(activity)) {
                    Settings.System.putInt(activity.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                } else {
                    Permissions.checkWriteSettingsPermission(activity, Permissions
                            .PERMISSION_SYSTEM_BRIGHTNESS);
                }


            } else if (brightnesstemp == 0.6f) {
                brightnesstemp = Settings.System.getInt(activity
                                .getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        lp.screenBrightness = brightnesstemp;
        activity.getWindow().setAttributes(lp);
        isFirstBrightnessGesture = false;
    }

    private void changeBrightness(float delta) {
        // Estimate and adjust Brightness

        float bright = Math.min(Math.max(Math.max(brightness, 0.01f) + delta, 0.01f), 1f);
        setWindowBrightness(bright);
        logger.d("delta=" + delta + ",brightness=" + brightness + ",bright=" + bright);

        bright = Math.round(bright * 100);
        if (scrollTextListener != null)
            scrollTextListener.showVolBrightnessInfo(VBtype.BRIGHTNESS, (int) bright, 100);
    }

    private void setWindowBrightness(float brightness) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = brightness;
        // Set Brightness
        activity.getWindow().setAttributes(lp);
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        logger.i("doVolumeTouch");
        int vod = (int) (percent * mMaxVolume) + volume;

        logger.i("vod" + vod + ",percent=" + percent);

        if (vod > mMaxVolume) {
            vod = mMaxVolume;
        } else if (vod < 0) {
            vod = 0;
        }
        // 变更声音
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vod, 0);
        // 变更进度条
        int i = (int) (vod * 1.0 / mMaxVolume * 100);
        String s = i + "%";
        if (i == 0) {
            s = "off";
        }
        int newVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (vod != newVol)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vod, AudioManager.FLAG_SHOW_UI);
        if (scrollTextListener != null)
            scrollTextListener.showVolBrightnessInfo(VBtype.VOL, vod, mMaxVolume);
        logger.d("onVolumeSlide:" + s);
    }

    /*********************各种监听相关****************************************/
    public PlayerManager onError(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
        return this;
    }

    public PlayerManager onComplete(PlayerManager.OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
        return this;
    }

    public PlayerManager onInfo(PlayerManager.OnInfoListener onInfoListener) {
        this.onInfoListener = onInfoListener;
        return this;
    }

    private PlayerManager.OnErrorListener onErrorListener = new PlayerManager.OnErrorListener() {
        @Override
        public void onError(int what, int extra) {
        }
    };

    private PlayerManager.OnCompleteListener onCompleteListener = new PlayerManager
            .OnCompleteListener() {
        @Override
        public void onComplete() {
        }
    };

    private PlayerManager.OnInfoListener onInfoListener = new PlayerManager.OnInfoListener() {
        @Override
        public void onInfo(int what, int extra) {

        }
    };

    //播放状态监听事件
    public void setPlayerStateListener(PlayerStateListener playerStateListener) {
        this.playerStateListener = playerStateListener;
    }

    private void statusChange(int newStatus) {
        status = newStatus;
        if (!isLive && newStatus == STATUS_COMPLETED) {
            logger.d("statusChange STATUS_COMPLETED...");
            if (playerStateListener != null) {
                playerStateListener.onComplete();
            }
        } else if (newStatus == STATUS_ERROR) {
            logger.d("statusChange STATUS_ERROR...");
            if (playerStateListener != null) {
                playerStateListener.onError();
            }
        } else if (newStatus == STATUS_LOADING) {
//            $.id(R.id.app_video_loading).visible();
            if (playerStateListener != null) {
                playerStateListener.onLoading();
            }
            logger.d("statusChange STATUS_LOADING...");
        } else if (newStatus == STATUS_PLAYING) {
            logger.d("statusChange STATUS_PLAYING...");
            if (playerStateListener != null) {
                playerStateListener.onPlay();
                if (controller != null) {
                    controller.show();
                }
            }
        } else if (newStatus == STATUS_PREPARED) {
            logger.d("statusChange STATUS_PLAYING...");
            if (playerStateListener != null) {
                playerStateListener.onPrepared();
                if (controller != null) {
                    controller.show();
                }
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            logger.d("ACTION_UP");
            if (touchAction == TOUCH_SEEK) {
                onProgressSlide(percent, true);
            }
            if (scrollTextListener != null)
                scrollTextListener.setScrollFinish();

            touchAction = TOUCH_NONE;
        }
        return gestureDetector.onTouchEvent(event);
    }

    /**
     * 视频播放期间的相关事件
     */
    public interface PlayerStateListener {
        void onComplete();//播放完成

        void onError();//错误

        void onLoading();//正在加载

        void onPlay();//

        void onBack();//返回

        void onPrepared();//准备完成
    }

    //播放状态监听事件
    public void setScrollTextListener(ScrollTextListener listener) {
        this.scrollTextListener = listener;
    }

    public interface ScrollTextListener {
        void showSeekRewindInfo(FRtype type, String s);

        void showVolBrightnessInfo(VBtype brightness, int progress, int lenght);

        void setScrollFinish();
    }


    public interface OnErrorListener {
        void onError(int what, int extra);
    }

    public interface OnCompleteListener {
        void onComplete();
    }

    public interface OnInfoListener {
        void onInfo(int what, int extra);
    }

    /**
     * 屏幕触摸事件
     * 1.单击开启关闭控件
     * 2.双击条件判断实现播放比例切换
     * 3.横向滑动实现进度更新
     * 4.左右两边分别实现声音和亮度调节
     * 5.长按锁屏和解锁(锁屏只能进行播放比率切换,解锁不能进行比率切换)
     */
    private class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean firstTouch;
        private boolean volumeControl;
        private boolean toSeek;
        private boolean isTouchView;//是否触摸到videoView

        /**
         * 双击,一班播放器设置播放比例都是在控制view上面去弄
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            logger.d("onDoubleTap");
            if (isTouchView && isLockUI)//屏幕锁定才可以进行双击更换屏幕操作
                videoView.toggleAspectRatio();

            return true;
        }

        /**
         * 手指摁下
         */
        @Override
        public boolean onDown(MotionEvent e) {
            logger.d("onDown");
            touchAction = TOUCH_NONE;
            if (videoView.getX() < e.getRawX() && videoView.getWidth() + videoView.getX() >= e
                    .getRawX() && videoView.getY() < e.getRawY() && videoView.getHeight() +
                    videoView.getY() >= e.getRawY()) {
                isTouchView = true;
                isFirstBrightnessGesture = true;
                firstTouch = true;
                volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                brightness = lp.screenBrightness;
                logger.i("onDown" + ",mMaxVolume=" + mMaxVolume + "volume=" + volume + "," +
                        "brightness=" + brightness);
            } else {
                isTouchView = false;
            }
            return super.onDown(e);
        }

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (isPortrait() || !isTouchView || isLockUI) {//如果是竖屏禁止滑动的相关事件
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
            float mOldX = e1.getX(), mOldY = e1.getY();
            float deltaY = mOldY - e2.getY();
            float deltaX = mOldX - e2.getX();
            if (firstTouch) {
                toSeek = Math.abs(distanceX) >= Math.abs(distanceY);
                volumeControl = mOldX > screen.widthPixels * 0.5f;
                firstTouch = false;
            }
            if (toSeek) {
                if (!isLive) {
                    touchAction = TOUCH_SEEK;
                    percent = -deltaX / screen.widthPixels;
                    onProgressSlide(percent, false);
                }
            } else {
                percent = deltaY / screen.heightPixels;
                if (volumeControl) {
                    touchAction = TOUCH_VOLUME;
                    onVolumeSlide(percent);
                } else {
                    touchAction = TOUCH_BRIGHTNESS;
                    doBrightnessTouch(percent);
                }
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        /**
         * 用户长按触摸屏，由多个MotionEvent ACTION_DOWN触发，一班用于解锁当前屏幕用的
         */
        @Override
        public void onLongPress(MotionEvent e) {
            logger.d("onLongPress");
            if (controller != null)
                if (!controller.isShowing())
                    isLockUI = !isLockUI;
        }

        /**
         * 这个方法不同于onSingleTapUp，他是在GestureDetector确信用户在第一次触摸屏幕后，没有紧跟着第二次触摸屏幕，也就是不是“双击”的时候触发
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.i("PlayerGestureListener", "onSingleTapConfirmed");
            if (controller != null) {
                if (controller.isShowing())
                    controller.hide();
                else
                    controller.show();
            }

            return false;
        }

    }

    /**
     * 触摸事件
     **********************************************/
    public enum FRtype {
        INCREASE, DECREASE
    }

    public enum VBtype {
        VOL, BRIGHTNESS
    }

    /**
     * 全屏切换
     ***************************************/
    private boolean isPortrait() {
        int orientation = getScreenOrientation();
        boolean portrait = orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
                orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        return portrait;
    }

    /**
     * 横竖屏切换，实现全屏
     */
    public void onExpendScreen() {
        this.fullScreenOnly = isPortrait();
        setFullScreen(fullScreenOnly);
        View anchorView = videoView.getParent() instanceof View ?
                (View) videoView.getParent() : videoView;
        if (fullScreenOnly) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            ViewGroup.LayoutParams params = anchorView
                    .getLayoutParams();
            if (0 == playerNormalHeight)
                playerNormalHeight = params.height;
            params.height = LinearLayout.LayoutParams.MATCH_PARENT;
            anchorView.requestLayout();
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            ViewGroup.LayoutParams params = anchorView
                    .getLayoutParams();
            if (0 == playerNormalHeight)
                playerNormalHeight = params.height;
            params.height = playerNormalHeight;
            anchorView.requestLayout();
        }
        if (null != controller){
            controller.setAnchorView(anchorView);
            controller.setFull(fullScreenOnly);
        }
    }

    private void setFullScreen(boolean fullScreen) {
        if (activity != null) {
            WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
            if (fullScreen) {
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                activity.getWindow().setAttributes(attrs);
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            } else {
                attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                activity.getWindow().setAttributes(attrs);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }
            activity.getWindowManager().getDefaultDisplay().getMetrics(screen);
        }
    }

}
