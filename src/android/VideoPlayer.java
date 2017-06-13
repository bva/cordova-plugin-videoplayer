package com.moust.cordova.videoplayer;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.widget.MediaController;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.VideoView;
import android.content.Context;
import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

public class VideoPlayer extends CordovaPlugin implements OnCompletionListener, OnPreparedListener, OnErrorListener, OnDismissListener {

    protected static final String LOG_TAG = "VideoPlayer";

    private CallbackContext callbackContext = null;

    private VideoView videoView;
    private MediaPlayer player;
    private MediaController controller;

    private Integer duration = -1;

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action             The action to execute.
     * @param args               JSONArray of arguments for the plugin.
     * @param callbackContext    The callback id used when calling back into JavaScript.
     * @return                   A PluginResult object with a status and message.
     */
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("play")) {
            this.callbackContext = callbackContext;

            CordovaResourceApi resourceApi = webView.getResourceApi();
            String target = args.getString(0);
            final JSONObject options = args.getJSONObject(1);

            String fileUriStr;
            try {
                Uri targetUri = resourceApi.remapUri(Uri.parse(target));
                fileUriStr = targetUri.toString();
            } catch (IllegalArgumentException e) {
                fileUriStr = target;
            }

            Log.v(LOG_TAG, fileUriStr);

            final String path = fileUriStr;

            // Create dialog in new thread
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Context context = cordova.getActivity().getApplicationContext();
                    openNewActivity(context);
                }

                /*                public void run() {
                    openVideoDialog(path, options);
                } */
            });

            // Don't return any result now
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            callbackContext = null;

            return true;
        }
        else if (action.equals("getCurrentPosition")) {
            this.callbackContext = callbackContext;

            Integer position = 0;

            if(player != null) {
                position = player.getCurrentPosition();
            }

            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, position.toString());
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            callbackContext = null;
            return true;
        }
        else if (action.equals("getDuration")) {
            this.callbackContext = callbackContext;
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, duration.toString());
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            callbackContext = null;
            return true;
        }
        else if (action.equals("close")) {
            if(player.isPlaying()) {
                player.stop();
            }
            player.release();

            if (callbackContext != null) {
                PluginResult result = new PluginResult(PluginResult.Status.OK);
                result.setKeepCallback(false); // release status callback in JS side
                callbackContext.sendPluginResult(result);
                callbackContext = null;
            }

            return true;
        }
        return false;
    }

    private void openNewActivity(Context context) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        this.cordova.getActivity().startActivity(intent);
    }

/*
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void openVideoDialog(String path, JSONObject options) {
        videoView = new VideoView(cordova.getActivity());
        cordova.getActivity().setContentView(videoView);

        player = new MediaPlayer();
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);

        try {
            player.setDataSource(path);
        } catch (Exception e) {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getLocalizedMessage());
            result.setKeepCallback(false); // release status callback in JS side
            callbackContext.sendPluginResult(result);
            callbackContext = null;
            return;
        }

        try {
            float volume = Float.valueOf(options.getString("volume"));
            Log.d(LOG_TAG, "setVolume: " + volume);
            player.setVolume(volume, volume);
        } catch (Exception e) {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getLocalizedMessage());
            result.setKeepCallback(false); // release status callback in JS side
            callbackContext.sendPluginResult(result);
            callbackContext = null;
            return;
        }

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            try {
                int scalingMode = options.getInt("scalingMode");
                switch (scalingMode) {
                    case MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING:
                        Log.d(LOG_TAG, "setVideoScalingMode VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING");
                        player.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                        break;
                    default:
                        Log.d(LOG_TAG, "setVideoScalingMode VIDEO_SCALING_MODE_SCALE_TO_FIT");
                        player.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                }
            } catch (Exception e) {
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getLocalizedMessage());
                result.setKeepCallback(false); // release status callback in JS side
                callbackContext.sendPluginResult(result);
                callbackContext = null;
                return;
            }
        }

        final SurfaceHolder mHolder = videoView.getHolder();
        mHolder.setKeepScreenOn(true);
        mHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                player.setDisplay(holder);
                try {
                    player.prepare();
                } catch (Exception e) {
                    PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getLocalizedMessage());
                    result.setKeepCallback(false); // release status callback in JS side
                    callbackContext.sendPluginResult(result);
                    callbackContext = null;
                }
            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                player.release();
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
        });
    }
*/

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(LOG_TAG, "MediaPlayer.onError(" + what + ", " + extra + ")");
        if(mp.isPlaying()) {
            mp.stop();
        }
        mp.release();
        duration = -1;
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        videoView.requestFocus();
        controller = new MediaController(cordova.getActivity());
        videoView.setMediaController(controller);
        controller.show(1000000);
        duration = player.getDuration();
        Log.d(LOG_TAG, "MediaPlayer started");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(LOG_TAG, "MediaPlayer completed");
        mp.release();
        duration = -1;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.d(LOG_TAG, "Dialog dismissed");
        if (callbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK);
            result.setKeepCallback(false); // release status callback in JS side
            callbackContext.sendPluginResult(result);
            callbackContext = null;
        }
    }
}
