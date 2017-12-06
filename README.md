
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
        


