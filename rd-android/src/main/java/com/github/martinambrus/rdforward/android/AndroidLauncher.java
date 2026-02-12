package com.github.martinambrus.rdforward.android;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

/**
 * Android entry point for RDForward. Launches the game using libGDX's
 * Android backend with OpenGL ES 2.0.
 * <p>
 * The rendering uses the same shader-based pipeline as the desktop libGDX
 * backend ({@code LibGDXGraphics}), ensuring visual parity across platforms.
 * The networking layer (Netty, rd-protocol) works unchanged on Android.
 */
public class AndroidLauncher extends AndroidApplication {

    /**
     * Called when the Android Activity is created. Initialises the libGDX
     * application with the game's rendering adapter.
     *
     * @param savedInstanceState Android saved instance state (unused)
     */
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useGyroscope = false;
        // Request OpenGL ES 2.0 (minimum for shader-based rendering)
        config.useGL30 = false;

        // Use the full screen for rendering
        config.useImmersiveMode = true;

        // Launch with the game adapter (integrates rd-render + rd-protocol)
        initialize(new RDForwardGameAdapter(this), config);
    }
}
