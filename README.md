# IJKPlayer
简单的播放页面+弹幕实现

****注意事项： 横竖屏切换并实现全屏，必须设置Activity生命周期， 以免重走生命周期而导致全屏失败

              android:configChanges="keyboardHidden|orientation|screenSize"
              
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
    
