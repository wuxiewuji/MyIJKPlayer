package com.tools.xxf.ijkplayer.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.util.Log;
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
import com.tools.xxf.ijkplayer.widget.media.IMediaController;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;


/**
 * TODO 自定义播放控制器
 *
 * @author XXF
 *         Create Time : 2017/10/18 17:37
 */
public class MediaController extends RelativeLayout implements IMediaController {
    private final int HIDE_Controller = 0;
    public static final String TAG = "MediaController";
    private ArrayList<View> childs = new ArrayList<>();
    private RelativeLayout topLn;//顶部控件
    private LinearLayout bottomLn;//底部控件
    private boolean mShowing;
    private static final int sDefaultTimeout = 3000;
    private boolean mDragging;

    private android.widget.MediaController.MediaPlayerControl mPlayer;
    private Context mContext;
    private SeekBar seekBar;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private TextView mEndTime;
    private TextView mCurrentTime;
    private ImageView mPauseButton;
    private CharSequence mPlayDescription;
    private CharSequence mPauseDescription;
    private ImageView playBack;
    private ImageView showDanMu;
    private boolean isMove = false;
    private OnClickListener playBackListener;
    private ImageView fullBtn;
    private ImageView addBtn;
    private ImageView shareBtn;
    private MediaControllerListener mediaListener;


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

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

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

    }

    //监听事件
    private void initListener() {
        for (View child : childs) {
            child.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            show(0);
                            isMove = false;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            isMove = true;
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
            if (seekBar instanceof SeekBar) {
                SeekBar seeker = (SeekBar) seekBar;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
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
        AnimatorSet animatorSet = new AnimatorSet();//组合动画
        ObjectAnimator alpha = ObjectAnimator.ofFloat(topLn, "alpha", 1f, 0f);
        ObjectAnimator translationUp = ObjectAnimator.ofFloat(topLn, "Y", topLn.getY(), -topLn
                .getHeight());

        ObjectAnimator translationDown = ObjectAnimator.ofFloat(bottomLn, "Y", bottomLn.getY(),
                bottomLn.getY() + bottomLn.getHeight());

        animatorSet.setDuration(500);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.play(alpha).with(translationUp).with(translationDown);//两个动画同时开始
        animatorSet.start();

    }

    //开启当前控件
    public void showController() {
        AnimatorSet animatorSet = new AnimatorSet();//组合动画
        ObjectAnimator alpha = ObjectAnimator.ofFloat(topLn, "alpha", 0f, 1f);
        ObjectAnimator translationUp = ObjectAnimator.ofFloat(topLn, "Y", topLn.getY(), topLn
                .getTop());

        ObjectAnimator translationDown = ObjectAnimator.ofFloat(bottomLn, "Y", bottomLn.getY(),
                getHeight() - bottomLn.getHeight());

        animatorSet.setDuration(500);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.play(alpha).with(translationUp).with(translationDown);//两个动画同时开始
        animatorSet.start();
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
        Log.d(TAG, "show");
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
            if (!mDragging && mShowing && mPlayer.isPlaying()) {
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

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    public void showOnce(View view) {

    }

    private void doPauseResume() {
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



