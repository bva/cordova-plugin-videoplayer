var exec = require("cordova/exec");

module.exports = {

    DEFAULT_OPTIONS: {
        volume: 1.0,
        scalingMode: 1
    },

    SCALING_MODE: {
        SCALE_TO_FIT: 1,
        SCALE_TO_FIT_WITH_CROPPING: 2
    },

    load: function (path, options, successCallback, errorCallback) {
        options = this.merge(this.DEFAULT_OPTIONS, options);
        exec(successCallback, errorCallback, "VideoPlayer", "load", [path, options]);
    },

    start: function () {
        exec(null, null, "VideoPlayer", "start", []);
    },

    selectAudioTrack: function (i, successCallback, errorCallback) {
        exec(null, null, "VideoPlayer", "selectAudioTrack", [i]);
    },

    getCurrentPosition: function (successCallback, errorCallback) {
        return exec(successCallback, errorCallback, "VideoPlayer", "getCurrentPosition", []);
    },

    getDuration: function (successCallback, errorCallback) {
        return exec(successCallback, errorCallback, "VideoPlayer", "getDuration", []);
    },

    close: function (successCallback, errorCallback) {
        exec(successCallback, errorCallback, "VideoPlayer", "close", []);
    },

    merge: function () {
        var obj = {};
        Array.prototype.slice.call(arguments).forEach(function(source) {
            for (var prop in source) {
                obj[prop] = source[prop];
            }
        });
        return obj;
    }

};
