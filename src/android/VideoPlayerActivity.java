package com.moust.cordova.videoplayer;

import android.app.Activity;
import android.os.Bundle;

public class VideoPlayerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String package_name = getApplication().getPackageName();
        setContentView(getApplication().getResources().getIdentifier("video_player_activity", "layout", package_name));
    }
}