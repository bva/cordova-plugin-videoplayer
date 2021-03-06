package com.moust.cordova.videoplayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VideoPlayerActivity extends Activity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaController.MediaPlayerControl, MediaPlayer.OnInfoListener {
    private static final String LOG_TAG = VideoPlayerActivity.class.getSimpleName();

    private LocalBroadcastManager localBroadcastManager;
    private VideoView videoView;
    private MediaController controller;
    private MediaPlayer mp;

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
            else if(action.equals("selectAudioTrack")) {
                mp.selectTrack(intent.getExtras().getInt("i"));
                response_intent.putExtra("action", "play");
                response_intent.putExtra("event", "audioTrackSelected");
                localBroadcastManager.sendBroadcast(response_intent);
            }
            else if(action.equals("start")) {
                start();
            }
            else if(action.equals("getDuration")) {
                response_intent.putExtra("duration", getDuration());
                localBroadcastManager.sendBroadcast(response_intent);
            } else if(action.equals("close")) {
                if (videoView != null) {
                    videoView.setVisibility(VideoView.GONE);

                    try {
                        if (mp != null) {
                            mp.release();
                        }

                        mp = null;
                        videoView = null;
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "Exception in close", e);
                    }
                }

                response_intent.putExtra("action", "play");
                response_intent.putExtra("event", "closed");
                localBroadcastManager.sendBroadcast(response_intent);
                finish();
            }
        }
    };

    @Override
    protected void onDestroy() {
        localBroadcastManager.unregisterReceiver(messageReceiver);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "VideoPlayer.onCreate");

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
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Log.d(LOG_TAG, "MediaPlayer.onInfo(" + what + ", " + extra + ")");
        return true;
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
        this.mp = mp;

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

        MediaPlayer.TrackInfo[] trackInfoArray = mp.getTrackInfo();
        JSONObject event = new JSONObject();

        try {
            event.put("event", "prepared");
            JSONArray audio_tracks = new JSONArray();

            Log.d(LOG_TAG, "Tracks count: " + trackInfoArray.length);

            for (int j = 0; j < trackInfoArray.length; j++) {
                if (trackInfoArray[j].getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                    JSONObject audio_track = new JSONObject();
                    audio_track.put("index", j);
                    audio_track.put("lang", trackInfoArray[j].getLanguage());
                    audio_tracks.put(audio_track);
                }
            }

            event.put("audio_tracks", audio_tracks);
        } catch(JSONException e) {
            Log.e(LOG_TAG, "MediaPlayer JSON exception", e);
        }

        Intent response_intent = new Intent(VideoPlayer.class.getSimpleName());
        response_intent.putExtra("action", "play");
        response_intent.putExtra("event", "prepared");
        response_intent.putExtra("payload", event.toString());
        localBroadcastManager.sendBroadcast(response_intent);

        mp.setOnInfoListener(this);

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
