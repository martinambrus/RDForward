package com.github.martinambrus.rdapi.game;

import com.github.martinambrus.rdapi.keyboard.KeyboardListener;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public abstract class Game implements GameInterface {

    private final String apiVersion = "0.1.2";
    protected List<KeyboardListener> keyboardListeners = new ArrayList<KeyboardListener>();

    public void addKeyboardListener(KeyboardListener kl) {
        this.keyboardListeners.add( kl );
    }

    public boolean render( float a ) {
        while(Keyboard.next()) {
            if ( Keyboard.getEventKeyState() ) {
                for (KeyboardListener kl : this.keyboardListeners) {
                    kl.onKeyPressed(Keyboard.getEventKey());
                }
            }
        }

        return true;
    }

    public String getVersion() {
        return this.apiVersion;
    }
}
