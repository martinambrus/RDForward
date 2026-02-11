package com.badlogic.gdx.backends.android;

import com.badlogic.gdx.ApplicationListener;

/**
 * Stub for {@code com.badlogic.gdx.backends.android.AndroidApplication} so
 * the Android module compiles in desktop CI without the Android SDK.
 * Replaced by the real libGDX class at Android build time.
 */
public class AndroidApplication {

    protected void onCreate(android.os.Bundle savedInstanceState) {
    }

    public void initialize(ApplicationListener listener,
                           AndroidApplicationConfiguration config) {
    }
}
