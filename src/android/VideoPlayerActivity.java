package com.moust.cordova.videoplayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.MediaController;
import android.widget.VideoView;

import org.apache.cordova.PluginResult;

public class VideoPlayerActivity extends Activity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaController.MediaPlayerControl {
    private static final String LOG_TAG = VideoPlayerActivity.class.getSimpleName();

    private LocalBroadcastManager localBroadcastManager;
    private VideoView videoView;
    private MediaController controller;

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            Intent response_intent = new Intent(VideoPlayer.class.getSimpleName());
            response_intent.putExtra("action", action);

            Log.d(LOG_TAG, "Message received: " + action);

            if(action.equals("getCurrentPosition")) {
                response_intent.putExtra("currentPosition", getCurrentPosition());
                localBroadcastManager.sendBroadcast(response_intent);
            }
            else if(action.equals("getDuration")) {
                response_intent.putExtra("duration", getDuration());
                localBroadcastManager.sendBroadcast(response_intent);
            } else if(action.equals("close")) {
                videoView.setMediaController(null);

                if(videoView.isPlaying()) {
                    videoView.stopPlayback();
                }

                response_intent.putExtra("action", "play");
                response_intent.putExtra("event", "closed");
                localBroadcastManager.sendBroadcast(response_intent);

                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String package_name = getApplication().getPackageName();
        setContentView(getResources().getIdentifier("video_player_activity", "layout", package_name));
        int id = getResources().getIdentifier("videoview", "id", package_name);

        videoView = (VideoView) findViewById(id);

        controller = new MediaController(this);
        controller.setMediaPlayer(this);
        controller.setAnchorView(videoView);
        controller.setMediaPlayer(videoView);
        videoView.setMediaController(controller);
        videoView.setOnPreparedListener(this);
        videoView.setOnCompletionListener(this);
        videoView.setOnErrorListener(this);

        Intent intent = getIntent();
        videoView.setVideoPath(intent.getStringExtra("url"));

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(messageReceiver, new IntentFilter(this.getClass().getSimpleName()));
    }

    public void start() {
        videoView.start();

        Intent response_intent = new Intent(VideoPlayer.class.getSimpleName());
        response_intent.putExtra("action", "play");
        response_intent.putExtra("event", "started");
        localBroadcastManager.sendBroadcast(response_intent);
        Log.d(LOG_TAG, "MediaPlayer started");
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    public void pause() {
        videoView.pause();

        Intent response_intent = new Intent(VideoPlayer.class.getSimpleName());
        response_intent.putExtra("action", "play");
        response_intent.putExtra("event", "paused");
        localBroadcastManager.sendBroadcast(response_intent);
        Log.d(LOG_TAG, "MediaPlayer paused");
    }

    public int getDuration() {
        return videoView.getDuration();
    }

    public int getCurrentPosition() {
        return videoView.getCurrentPosition();
    }

    public void seekTo(int i) {
        videoView.seekTo(i);
    }

    public boolean isPlaying() {
        return videoView.isPlaying();
    }

    public int getBufferPercentage() {
        return 0;
    }

    public boolean canPause() {
        return videoView.canPause();
    }

    public boolean canSeekBackward() {
        return videoView.canSeekBackward();
    }

    public boolean canSeekForward() {
        return videoView.canSeekForward();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(LOG_TAG, "MediaPlayer.onError(" + what + ", " + extra + ")");
        if(mp.isPlaying()) {
            mp.stop();
        }

        mp.release();

        Intent response_intent = new Intent(VideoPlayer.class.getSimpleName());
        response_intent.putExtra("action", "play");
        response_intent.putExtra("event", "error");
        response_intent.putExtra("error", "MediaPlayer.onError(" + what + ", " + extra + ")");
        localBroadcastManager.sendBroadcast(response_intent);

        finish();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Intent intent = getIntent();

        float volume = (float)intent.getDoubleExtra("volume", -1);

        if(volume != -1) {
            Log.d(LOG_TAG, "setVolume: " + volume);
            mp.setVolume(volume, volume);
        }

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            int scalingMode = intent.getIntExtra("scalingMode", -1);

            switch (scalingMode) {
                case MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING:
                    Log.d(LOG_TAG, "setVideoScalingMode VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING");
                    mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    break;
                default:
                    Log.d(LOG_TAG, "setVideoScalingMode VIDEO_SCALING_MODE_SCALE_TO_FIT");
                    mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            }
        }

        Intent response_intent = new Intent(VideoPlayer.class.getSimpleName());
        response_intent.putExtra("action", "play");
        response_intent.putExtra("event", "prepared");
        localBroadcastManager.sendBroadcast(response_intent);

        start();
        Log.d(LOG_TAG, "MediaPlayer prepared");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(LOG_TAG, "MediaPlayer completed");
        mp.release();

        Intent response_intent = new Intent(VideoPlayer.class.getSimpleName());
        response_intent.putExtra("action", "play");
        response_intent.putExtra("event", "completed");
        localBroadcastManager.sendBroadcast(response_intent);
    }
}

