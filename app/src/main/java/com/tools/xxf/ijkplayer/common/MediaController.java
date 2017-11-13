package com.tools.xxf.ijkplayer.common;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tools.xxf.ijkplayer.R;
import com.tools.xxf.ijkplayer.utils.AndroidDevices;
import com.tools.xxf.ijkplayer.utils.AndroidUtil;
import com.tools.xxf.ijkplayer.utils.MyLogger;
import com.tools.xxf.ijkplayer.widget.media.IMediaController;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static com.tools.xxf.ijkplayer.utils.TimeUtils.stringForTime;


/**
 * TODO 自定义播放控制器
 *
 * @author XXF
 *         Create Time : 2017/10/18 17:37
 */
public class MediaController extends RelativeLayout implements IMediaController {
    private final int HIDE_Controller = 0;
    public static final String TAG = "MediaController";
    MyLogger logger = MyLogger.getXiongFengLog();
    private ArrayList<View> childs = new ArrayList<>();
    private RelativeLayout topLn;//顶部控件
    private LinearLayout bottomLn;//底部控件
    private boolean mShowing;
    float startx;
    float starty;
    private static final int sDefaultTimeout = 3000;
    private boolean mDragging;

    private android.widget.MediaController.MediaPlayerControl mPlayer;
    private Context mContext;
    private SeekBar seekBar;
    private TextView mEndTime;
    private TextView mCurrentTime;
    private ImageView mPauseButton;
    private CharSequence mPlayDescription;
    private CharSequence mPauseDescription;
    private ImageView playBack;
    private ImageView showDanMu;
    private boolean isMove = false;
    private ImageView fullBtn;
    private ImageView addBtn;
    private ImageView shareBtn;
    private MediaControllerListener mediaListener;
    private float movex;
    private float movey;
    private double moves;
    private boolean isFull;

    public MediaController(Context context) {
        this(context, null);
    }

    public MediaController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        this.isInEditMode();
        mContext = context;
        Resources res = mContext.getResources();
        setBackgroundColor(getResources().getColor(R.color.transparent));
        LayoutInflater.from(context).inflate(R.layout.controller_layout, this);
        mPlayDescription = res
                .getText(R.string.lockscreen_transport_play_description);
        mPauseDescription = res
                .getText(R.string.lockscreen_transport_pause_description);


        topLn = findViewById(R.id.top_ln);
        playBack = findViewById(R.id.video_back);
        showDanMu = findViewById(R.id.video_danmaku);
        bottomLn = findViewById(R.id.bottom_ln);

        seekBar = findViewById(R.id.mediacontroller_progress);
        mEndTime = findViewById(R.id.time);
        mCurrentTime = findViewById(R.id.time_current);
        mPauseButton = findViewById(R.id.pause);
        fullBtn = findViewById(R.id.player_full);
        addBtn = findViewById(R.id.video_add);
        shareBtn = findViewById(R.id.video_share);


        //事件拦截，重置关闭控件操作
        bottomLn.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                show(0);
                Log.i(TAG, "bottomLn onTouch");
                return true;
            }
        });
        childs.add(playBack);
        childs.add(showDanMu);
        childs.add(mPauseButton);
        childs.add(fullBtn);
        childs.add(addBtn);
        childs.add(shareBtn);
        initListener();
        int height = getHeight();
        Log.i(TAG, "height=" + height);
    }

    //监听事件,通过控件的触摸事件,拦截事件发送到父布局,并处理当前控件的点击事件
    private void initListener() {
        for (View child : childs) {
            child.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            show(0);
                            isMove = false;
                            startx = event.getX();
                            starty = event.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            movex = event.getX() - startx;
                            movey = event.getY() - starty;
                            //noinspection ResultOfMethodCallIgnored
                            moves = Math.sqrt(Math.pow(movex, 2) + Math.pow(movey, 2));
                            if (moves > 24) {//触摸范围值
                                isMove = true;
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            show(sDefaultTimeout);
                            if (!isMove) {
                                setViewListener(v);
                            }
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            hide();
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
        }

        if (seekBar != null) {
            SeekBar seeker = seekBar;
            seeker.setOnSeekBarChangeListener(mSeekListener);
        }
    }

    //控件的点击事件
    private void setViewListener(View v) {

        switch (v.getId()) {
            case R.id.pause://开启暂停
                mPauseListener.onClick(v);
                break;
            case R.id.video_back://返回键
                if (null != mediaListener) {
                    mediaListener.playBackClick(v);
                }
                break;
            case R.id.video_danmaku://弹幕开关
                if (null != mediaListener) {
                    mediaListener.danmuClick(v);
                }
                break;
            case R.id.video_add://
                if (null != mediaListener) {
                    mediaListener.addClick(v);
                }
                break;
            case R.id.video_share://分享
                if (null != mediaListener) {
                    mediaListener.shareClick(v);
                }
                break;
            case R.id.player_full://全屏
                if (null != mediaListener) {
                    mediaListener.fullClick(v);
                }
                break;

            default:
                break;
        }
    }


    private Handler hanler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (null == getContext())
                return true;

            switch (msg.what) {
                case HIDE_Controller:
                    hide();
                    break;
            }
            return false;
        }
    });

    /**
     * 定时关闭控件的任务
     *
     * @param timeout
     */
    private void timerTask(int timeout) {
        hanler.sendEmptyMessageDelayed(HIDE_Controller, timeout);
    }

    //关闭当前控件
    private void hideController() {
        if (isFull)
            dimStatusBar(true);
        AnimatorSet animatorSet = new AnimatorSet();//组合动画
        ObjectAnimator alpha = ObjectAnimator.ofFloat(topLn, "alpha", 1f, 0f);
        ObjectAnimator translationUp = ObjectAnimator.ofFloat(topLn, "Y", topLn.getY(), -topLn
                .getHeight());

        ObjectAnimator translationDown = ObjectAnimator.ofFloat(bottomLn, "Y", bottomLn.getY(),
                bottomLn.getY() + bottomLn.getHeight());

        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.play(alpha).with(translationUp).with(translationDown);//两个动画同时开始
        animatorSet.start();

    }

    //开启当前控件
    public void showController() {

        if (isFull) {
            dimStatusBar(false);
        }

        AnimatorSet animatorSet = new AnimatorSet();//组合动画
        ObjectAnimator alpha = ObjectAnimator.ofFloat(topLn, "alpha", 0f, 1f);
        ObjectAnimator translationUp = ObjectAnimator.ofFloat(topLn, "Y", topLn.getY(), topLn
                .getTop());

        ObjectAnimator translationDown = ObjectAnimator.ofFloat(bottomLn, "Y", bottomLn.getY(),
                getHeight() - bottomLn.getHeight());

        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.play(alpha).with(translationUp).with(translationDown);//两个动画同时开始
        animatorSet.start();
    }

    /**
     * dip 转 px
     *
     * @param dip
     * @return
     */
    public int dip2px(int dip) {
        //
        // 公式： dp = px / (dpi / 160) px = dp * (dpi / 160)
        // dp = px / denisity
        // px = dp * denisity;
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        float density = metrics.density;
        return (int) (dip * density + 0.5f);
    }

    /**
     * Dim the status bar and/or navigation icons when needed on Android 3.x.
     * Hide it on Android 4.0 and later
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void dimStatusBar(boolean dim) {
        logger.i("dimStatusBar=" + dim);

        if (!AndroidUtil.isHoneycombOrLater)
            return;
        int visibility = 0;
        int navbar = 0;

        if (AndroidUtil.isJellyBeanOrLater) {
            visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            if (isFull)
                navbar = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        if (dim) {
            ((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (AndroidUtil.isICSOrLater)
                navbar |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
            else
                visibility |= View.STATUS_BAR_HIDDEN;
            if (!AndroidDevices.hasCombBar()) {
                navbar |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                if (AndroidUtil.isKitKatOrLater)
                    visibility |= View.SYSTEM_UI_FLAG_IMMERSIVE;
                if (AndroidUtil.isJellyBeanOrLater)
                    visibility |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            }
        } else {
            ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams
                    .FLAG_FULLSCREEN);
            if (AndroidUtil.isICSOrLater)
                visibility |= View.SYSTEM_UI_FLAG_VISIBLE;
            else
                visibility |= View.STATUS_BAR_VISIBLE;
        }

        if (AndroidDevices.hasNavBar())
            visibility |= navbar;
        ((Activity) mContext).getWindow().getDecorView().setSystemUiVisibility(visibility);
    }

    private int getDpi() {
        int dpi = 0;
        Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            dpi = dm.widthPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        dm = new DisplayMetrics();
        WindowManager windowManager = ((Activity) mContext).getWindowManager();
        windowManager.getDefaultDisplay().getMetrics(dm);
        if (dpi != 0) {
            return dpi - dm.widthPixels;
        }
        return 0;
    }

    @Override
    public void hide() {
        if (mShowing) {
            try {
                removeCallbacks(mShowProgress);
                hanler.removeMessages(HIDE_Controller);
                hideController();

            } catch (IllegalArgumentException ex) {
                Log.w("MediaController", "already removed");
            }
            mShowing = false;
        }
    }

    @Override
    public boolean isShowing() {
        return mShowing;
    }


    //此处可手动设置当前布局的大小
    @Override
    public void setAnchorView(View view) {

    }

    @Override
    public void setMediaPlayer(android.widget.MediaController.MediaPlayerControl player) {
        mPlayer = player;
        Log.d(TAG, "setMediaPlayer");
        updatePausePlay();
    }

    @Override
    public void show(int timeout) {
        if (!mShowing) {
            showController();
            mShowing = true;
        }

        setProgress();
        updatePausePlay();
        post(mShowProgress);
        hanler.removeMessages(HIDE_Controller);

        if (timeout != 0) {
            timerTask(timeout);
        }
    }

    @Override
    public void show() {
        show(sDefaultTimeout);
    }

    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar
            .OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            removeCallbacks(mShowProgress);
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                return;
            }
            if (null != mPlayer)
                mPlayer.seekTo(progress);
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            Log.d(TAG, "onStopTrackingTouch");
            show(sDefaultTimeout);
            post(mShowProgress);
        }
    };

    private void updatePausePlay() {
        if (null != mPlayer)
            if (mPlayer.isPlaying()) {
                mPauseButton.setImageResource(R.drawable.icon_play);
                mPauseButton.setContentDescription(mPauseDescription);
            } else {
                mPauseButton.setImageResource(R.drawable.icon_zanting);
                mPauseButton.setContentDescription(mPlayDescription);
            }
    }

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (!mDragging && mShowing && null != mPlayer && mPlayer.isPlaying()) {
                postDelayed(mShowProgress, 1000 - (pos % 1000));
            }
        }
    };

    private int setProgress() {
        if (mPlayer == null || mDragging || null == seekBar) {
            return 0;
        }
        int time = mPlayer.getCurrentPosition();
        int length = mPlayer.getDuration();

        if (mEndTime != null)
            mEndTime.setText(stringForTime(length));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(time));
        seekBar.setMax(length);
        seekBar.setProgress(time);
        return 0;
    }


    @Override
    public void showOnce(View view) {

    }

    @Override
    public void setFull(boolean isFull) {
        this.isFull = isFull;
        if (topLn != null) {
            RelativeLayout.LayoutParams params = (LayoutParams) topLn.getLayoutParams();
            if (isScreenOriatationPortrait(mContext)){
                params.topMargin = dip2px(12 + 36);
                dimStatusBar(false);
            }else {
                params.topMargin = dip2px(12+24);
            }
        }
    }
    public static boolean isScreenOriatationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }
    private void doPauseResume() {
        if (mPlayer != null)
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
            } else {
                mPlayer.start();
            }
    }


    //播放控件点击的监听事件
    private final View.OnClickListener mPauseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };

    public void setMediaListener(MediaControllerListener listener) {
        mediaListener = listener;
    }

    public interface MediaControllerListener {
        void playBackClick(View v);

        void danmuClick(View v);

        void fullClick(View v);

        void addClick(View v);

        void shareClick(View v);
    }
}



