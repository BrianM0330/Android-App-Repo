package com.example.defendergame;

import android.content.Context;
import android.media.SoundPool;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;

class SoundPlayer {

    private static final String TAG = "SoundPlayer";
    private static boolean initialized = false;

    private static SoundPool soundPool;
    private static final int MAX_STREAMS = 10;
    private static final HashSet<Integer> loaded = new HashSet<>();
    private static final HashMap<String, Integer> soundNameToStreamId = new HashMap<>();

    private static void init() {
        initialized = true;

        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(MAX_STREAMS);
        soundPool = builder.build();
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            Log.d(TAG, "onLoadComplete: #" + sampleId + "  " + status);
            loaded.add(sampleId);
        });
    }

    public static void setupSound(Context context, String soundName, int resource) {
        if (!initialized)
            init();

        int streamId = soundPool.load(context, resource, 1);
        soundNameToStreamId.put(soundName, streamId);

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    public static void start(final String soundName) {
        if (!initialized)
            init();

        if (!loaded.contains(soundNameToStreamId.get(soundName))) {
            Log.d(TAG, "start: SOUND NOT LOADED: " + soundName);
            return;
        }

        Integer streamId = soundNameToStreamId.get(soundName);
        if (streamId == null)
            return;
        soundPool.play(streamId, 1f, 1f, 1, 0, 1f);
    }

    public static void startLoop(final String soundName) {
        if (!initialized)
            init();

        if (!loaded.contains(soundNameToStreamId.get(soundName))) {
            Log.d(TAG, "start: SOUND NOT LOADED: " + soundName);
            return;
        }

        Integer streamId = soundNameToStreamId.get(soundName);
        if (streamId == null)
            return;
        soundPool.play(streamId, 1f, 1f, 1, -1, 1f);
    }

    public static void stop() {
        Integer x = soundNameToStreamId.get("background");
        if (x != null) soundPool.stop(x);
    }
}
