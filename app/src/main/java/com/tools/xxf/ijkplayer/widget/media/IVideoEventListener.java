package com.tools.xxf.ijkplayer.widget.media;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;

/**
 * Created by Administrator on 2017/9/19.
 */

public interface IVideoEventListener {

    void onPrepared(IMediaPlayer mp);

    void onCompletion(IMediaPlayer mp);

    boolean onInfo(IMediaPlayer mp, int arg1, int arg2);

    boolean onError(IMediaPlayer mp, int framework_err, int impl_err);

    void onBufferingUpdate(IMediaPlayer mp, int percent);

    void onSeekComplete(IMediaPlayer mp);

    void onTimedText(IMediaPlayer mp, IjkTimedText text);

}
