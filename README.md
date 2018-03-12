
# 工具集框架试编辑

 **app**
   测试mode，测试所编写的库。
 
    
 **configlib**
   框架整合包,所有需要的框架,和基本信息处理lib.
   
 **ijklib**
   自定义的Ijkplayer播放器的封装
      一、字体图片库加载引用
         1）、assets 下的iconfont.ttf,阿里图库选择的相关图片字体库
         2）、icon 包下编译、引入的iconfont字体图标库
         3）、具体使用详见xml
      二、ijkview引入使用
         1)、相关库引入
         2)、ijk官方mode下载 拷贝media包下面相关文件
         3)、编辑自己的Controller、Manager管理类
         4)、微调处理状态栏、导航栏问题
      三、app mode中简易demo编写测试运行
      
    ****注意事项： 横竖屏切换并实现全屏，必须设置Activity生命周期， 以免重走生命周期而导致全屏失败
    
     `  android:configChanges="keyboardHidden|orientation|screenSize"`
                  
        /**
         * 横竖屏判断
         */
        private boolean isPortrait() {
            int orientation = getScreenOrientation();
            boolean portrait = orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
                    orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            return portrait;
        }
        
        /**
         * 横竖屏切换，实现全屏
         */
        private void onExpendScreen() {
            if (isPortrait()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ffView.getLayoutParams();
                if (0 == playerNormalHeight)
                    playerNormalHeight = params.height;
                params.height =LinearLayout.LayoutParams.MATCH_PARENT;
                ffView.requestLayout();
    
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ffView.getLayoutParams();
                params.height = playerNormalHeight;
                ffView.requestLayout();
            }
        }
        
##错误码
//        int MEDIA_INFO_UNKNOWN = 1;//未知信息
//        int MEDIA_INFO_STARTED_AS_NEXT = 2;//播放下一条
//        int MEDIA_INFO_VIDEO_RENDERING_START = 3;//视频开始整备中
//        int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;//视频日志跟踪
//        int MEDIA_INFO_BUFFERING_START = 701;//701 开始缓冲中
//        int MEDIA_INFO_BUFFERING_END = 702;//702 缓冲结束
//        int MEDIA_INFO_NETWORK_BANDWIDTH = 703;//703 网络带宽，网速方面
//        int MEDIA_INFO_BAD_INTERLEAVING = 800;//交错出错意味着交错存储的 media 有错误或者根本没交错
//        int MEDIA_INFO_NOT_SEEKABLE = 801;//801 media 无法定位播放（如直播）
//        int MEDIA_INFO_METADATA_UPDATE = 802;//一个新的元数据集可用
//        int MEDIA_INFO_TIMED_TEXT_ERROR = 900;//未能妥善处理定时文本轨道
//        int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;//不支持字幕
//        int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;//字幕超时
//        int MEDIA_INFO_VIDEO_ROTATION_CHANGED = 10001;//这里返回了视频旋转的角度，根据角度旋转视频到正确的画面
//        int MEDIA_INFO_AUDIO_RENDERING_START = 10002;//音频开始整备中
//        int MEDIA_INFO_AUDIO_DECODED_START = 10003;//音频开始解码
//        int MEDIA_INFO_VIDEO_DECODED_START = 10004;//视频开始解码
//        int MEDIA_INFO_OPEN_INPUT = 10005;//
//        int MEDIA_INFO_FIND_STREAM_INFO = 10006;
//        int MEDIA_INFO_COMPONENT_OPEN = 10007;
//        int MEDIA_INFO_MEDIA_ACCURATE_SEEK_COMPLETE = 10100;
//        int MEDIA_ERROR_UNKNOWN = 1;
//        int MEDIA_ERROR_SERVER_DIED = 100;//服务挂掉
//        int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;//数据错误没有有效的回收
//        int MEDIA_ERROR_IO = -1004;//IO错误
//        int MEDIA_ERROR_MALFORMED = -1007;
//        int MEDIA_ERROR_UNSUPPORTED = -1010;//数据不支持
//        int MEDIA_ERROR_TIMED_OUT = -110;//数据超时


