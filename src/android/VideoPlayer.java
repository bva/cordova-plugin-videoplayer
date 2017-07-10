package com.moust.cordova.videoplayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.util.Log;
import android.content.Context;
import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;
import android.support.v4.content.LocalBroadcastManager;

public class VideoPlayer extends CordovaPlugin {
    private CallbackContext callbackContext;
    private CallbackContext playCallbackContext;

    private LocalBroadcastManager localBroadcastManager;

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getStringExtra("action");
            Log.d(LOG_TAG, "Message received: " + action);

            if (action.equals("play")) {
                String event = intent.getStringExtra("event");
                Log.d(LOG_TAG, "Play event: " + event);

                PluginResult pluginResult = null;

                if (event.equals("error")) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, intent.getStringExtra("error"));
                    pluginResult.setKeepCallback(false);
                } else {
                    pluginResult = new PluginResult(PluginResult.Status.OK, event);
                    pluginResult.setKeepCallback(true);
                }

                playCallbackContext.sendPluginResult(pluginResult);
            }
            else if (action.equals("getCurrentPosition")) {
                Integer position = intent.getIntExtra("currentPosition", -1);
                Log.d(LOG_TAG, "Position: " + position);
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, position.toString());
                callbackContext.sendPluginResult(pluginResult);
                callbackContext = null;
            }
            else if(action.equals("getDuration")) {
                Integer duration = intent.getIntExtra("duration", -1);
                Log.d(LOG_TAG, "Duration: " + duration);
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, duration.toString());
                callbackContext.sendPluginResult(pluginResult);
                callbackContext = null;
            }
        }
    };

    private static final String LOG_TAG = VideoPlayer.class.getSimpleName();

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        localBroadcastManager = LocalBroadcastManager.getInstance(cordova.getActivity().getApplicationContext());
        localBroadcastManager.registerReceiver(messageReceiver, new IntentFilter(this.getClass().getSimpleName()));
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action             The action to execute.
     * @param args               JSONArray of arguments for the plugin.
     * @param callbackContext    The callback id used when calling back into JavaScript.
     * @return                   A PluginResult object with a status and message.
     */
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        Log.d(LOG_TAG, "Execute action: " + action);

        if (action.equals("play")) {
            final String target = args.getString(0);
            final JSONObject options = args.getJSONObject(1);

            final Activity cordovaActivity = cordova.getActivity();

            cordovaActivity.runOnUiThread(new Runnable() {
                public void run() {
                    openNewActivity(cordovaActivity.getApplicationContext(), target, options);
                }

            });

            playCallbackContext = callbackContext;
            return true;
        }
        else if (action.equals("getCurrentPosition")) {
            this.callbackContext = callbackContext;

            Intent intent = new Intent(VideoPlayerActivity.class.getSimpleName());
            intent.putExtra("action", action);
            localBroadcastManager.sendBroadcast(intent);
            return true;
        }
        else if (action.equals("getDuration")) {
            this.callbackContext = callbackContext;

            Intent intent = new Intent(VideoPlayerActivity.class.getSimpleName());
            intent.putExtra("action", action);
            localBroadcastManager.sendBroadcast(intent);
            return true;
        }
        else if (action.equals("close")) {
            this.callbackContext = callbackContext;

            Intent intent = new Intent(VideoPlayerActivity.class.getSimpleName());
            intent.putExtra("action", action);
            localBroadcastManager.sendBroadcast(intent);
            return true;
        }

        return false;
    }

    private void openNewActivity(Context context, String path, JSONObject options) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra("url", path);
        try {
            intent.putExtra("volume", options.getDouble("volume"));
            intent.putExtra("scalingMode", options.getInt("scalingMode"));
        } catch(JSONException e) {
            Log.e(LOG_TAG, "Error parsing options", e);
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, "Error parsing options: " + e.getMessage());

            pluginResult.setKeepCallback(false);
            playCallbackContext.sendPluginResult(pluginResult);
            return;
        }

        this.cordova.getActivity().startActivity(intent);
    }
}
