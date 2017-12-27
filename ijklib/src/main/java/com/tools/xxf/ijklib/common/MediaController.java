package com.tools.xxf.ijklib.common;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.tools.xxf.ijklib.R;
import com.tools.xxf.ijklib.media.IMediaController;

import java.util.ArrayList;

import static com.tools.xxf.ijklib.utils.TimeUtils.stringForTime;


/**
 * TODO 自定义播放控制器
 *
 * @author XXF
 *         Create Time : 2017/10/18 17:37
 */
public class MediaController extends RelativeLayout implements IMediaController {
    private final int HIDE_Controller = 0;
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
    private int height;
    private float ybottom;

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
                LogUtils.a("bottomLn onTouch");
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

        int i = v.getId();
        if (i == R.id.pause) {
            mPauseListener.onClick(v);

        } else if (i == R.id.video_back) {
            if (null != mediaListener) {
                mediaListener.playBackClick(v);
            }

        } else if (i == R.id.video_danmaku) {
            if (null != mediaListener) {
                mediaListener.danmuClick(v);
            }

        } else if (i == R.id.video_add) {
            if (null != mediaListener) {
                mediaListener.addClick(v);
            }

        } else if (i == R.id.video_share) {
            if (null != mediaListener) {
                mediaListener.shareClick(v);
            }

        } else if (i == R.id.player_full) {
            if (null != mediaListener) {
                mediaListener.fullClick(v);
            }
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
        setWith();//设置控件的宽度
        dimStatusBar(false);
        topLn.setVisibility(GONE);
        bottomLn.setVisibility(GONE);


    }

    //开启当前控件
    public void showController() {
        LogUtils.d("showController");
        dimStatusBar(true);
        topLn.setVisibility(VISIBLE);
        bottomLn.setVisibility(VISIBLE);

    }


    @Override
    public void hide() {
        if (mShowing) {
            try {
                removeCallbacks(mShowProgress);
                hanler.removeMessages(HIDE_Controller);
                hideController();

            } catch (IllegalArgumentException ex) {
                LogUtils.d(ex.getMessage());
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
        ViewGroup.LayoutParams params = view.getLayoutParams();
        ybottom = view.getY() + params.height;
        LogUtils.d("ybottom=" + ybottom);
        height = params.height;
    }

    @Override
    public void setMediaPlayer(android.widget.MediaController.MediaPlayerControl player) {
        mPlayer = player;
        LogUtils.d("setMediaPlayer");
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
            LogUtils.a("onStopTrackingTouch");
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
        ViewGroup.LayoutParams params = this.getLayoutParams();
        params.width = view.getWidth();
    }

    @Override
    public void setFull(boolean isFull) {
        this.isFull = isFull;
        setWith();
    }

    private void setWith() {
        Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = screenWidth;

    }

    /**
     * Dim the status bar and/or navigation icons when needed on Android 3.x.
     * Hide it on Android 4.0 and later
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void dimStatusBar(boolean dim) {
        int uiFlags = 0;

        if (!isFull) {
            if (dim) {
                uiFlags = View.SYSTEM_UI_FLAG_LOW_PROFILE;
                uiFlags |= 0x00001000;
            } else {
                uiFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.INVISIBLE;
                uiFlags |= 0x00001000;
            }
        } else {
            if (dim) {
                uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                uiFlags |= 0x00001000;
            } else {
                uiFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.INVISIBLE;
                uiFlags |= 0x00001000;
            }
        }


        ((Activity) mContext).getWindow().getDecorView().setSystemUiVisibility(uiFlags);
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
    private final OnClickListener mPauseListener = new OnClickListener() {
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



