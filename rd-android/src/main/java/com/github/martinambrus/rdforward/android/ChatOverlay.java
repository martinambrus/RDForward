package com.github.martinambrus.rdforward.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.martinambrus.rdforward.multiplayer.MultiplayerState;
import com.github.martinambrus.rdforward.multiplayer.RDClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Chat overlay for Android: displays chat messages and a chat button.
 * Messages fade out after {@link #DISPLAY_DURATION_MS}. The chat button
 * (top-right corner) opens an Android native text input dialog.
 */
public class ChatOverlay {

    private static final int MAX_VISIBLE = 8;
    private static final long DISPLAY_DURATION_MS = 10_000;
    private static final long FADE_DURATION_MS = 2_000;

    private final SpriteBatch spriteBatch;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;
    private final Texture whitePixel;
    private final List<ChatEntry> messages = new ArrayList<>();

    // Chat button bounds (y-up coords, computed during render)
    private float btnX, btnY, btnSize;

    public ChatOverlay(SpriteBatch spriteBatch, BitmapFont font,
                       GlyphLayout glyphLayout, Texture whitePixel) {
        this.spriteBatch = spriteBatch;
        this.font = font;
        this.glyphLayout = glyphLayout;
        this.whitePixel = whitePixel;
    }

    /** Add a chat message to the display. */
    public void addMessage(String message) {
        messages.add(new ChatEntry(message, System.currentTimeMillis()));
        while (messages.size() > 100) messages.remove(0);
    }

    /** Poll chat messages from MultiplayerState and add to display. */
    public void pollMessages() {
        String msg;
        while ((msg = MultiplayerState.getInstance().pollChatMessage()) != null) {
            addMessage(msg);
        }
    }

    /**
     * Render chat messages and the chat button.
     * Manages its own SpriteBatch begin/end and GL state.
     */
    public void render(int screenWidth, int screenHeight) {
        float scale = Math.max(1f, screenHeight / 480f);

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);
        spriteBatch.begin();

        renderMessages(screenWidth, screenHeight, scale);
        renderChatButton(screenWidth, screenHeight, scale);

        font.getData().setScale(1f);
        font.setColor(1, 1, 1, 1);
        spriteBatch.end();

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
    }

    private void renderMessages(int screenWidth, int screenHeight, float scale) {
        long now = System.currentTimeMillis();
        List<VisibleMessage> visible = new ArrayList<>();

        for (int i = messages.size() - 1; i >= 0 && visible.size() < MAX_VISIBLE; i--) {
            ChatEntry entry = messages.get(i);
            long age = now - entry.timestamp;
            if (age < DISPLAY_DURATION_MS) {
                visible.add(0, new VisibleMessage(entry.message, 1.0f));
            } else if (age < DISPLAY_DURATION_MS + FADE_DURATION_MS) {
                float alpha = 1.0f - (float) (age - DISPLAY_DURATION_MS) / FADE_DURATION_MS;
                visible.add(0, new VisibleMessage(entry.message, alpha));
            }
        }

        if (visible.isEmpty()) return;

        font.getData().setScale(scale * 1.0f);
        float pad = 4 * scale;
        float lineHeight = font.getLineHeight();
        float totalHeight = lineHeight * visible.size() + pad * 2;
        float maxWidth = 0;
        for (VisibleMessage vm : visible) {
            glyphLayout.setText(font, vm.text);
            if (glyphLayout.width > maxWidth) maxWidth = glyphLayout.width;
        }
        float bgWidth = maxWidth + pad * 4;

        // Minimum alpha for background
        float minAlpha = 1.0f;
        for (VisibleMessage vm : visible) {
            if (vm.alpha < minAlpha) minAlpha = vm.alpha;
        }

        // Background at bottom-left
        spriteBatch.setColor(0, 0, 0, 0.5f * minAlpha);
        spriteBatch.draw(whitePixel, pad, pad, bgWidth, totalHeight);
        spriteBatch.setColor(1, 1, 1, 1);

        // Messages (bottom-up in libGDX y-up coords)
        for (int i = 0; i < visible.size(); i++) {
            VisibleMessage vm = visible.get(i);
            float textY = pad + pad + (visible.size() - i) * lineHeight;

            // Shadow
            font.setColor(0, 0, 0, 0.8f * vm.alpha);
            font.draw(spriteBatch, vm.text, pad * 2 + 1, textY - 1);

            // Text
            font.setColor(1, 1, 1, vm.alpha);
            font.draw(spriteBatch, vm.text, pad * 2, textY);
        }
    }

    private void renderChatButton(int screenWidth, int screenHeight, float scale) {
        if (!RDClient.getInstance().isConnected()) {
            btnSize = 0; // ensure isChatButtonTapped returns false
            return;
        }

        btnSize = scale * 36f;
        float pad = scale * 8f;
        btnX = screenWidth - btnSize - pad;
        btnY = screenHeight - btnSize - pad;

        // Button background
        spriteBatch.setColor(0.15f, 0.15f, 0.35f, 0.7f);
        spriteBatch.draw(whitePixel, btnX, btnY, btnSize, btnSize);
        spriteBatch.setColor(1, 1, 1, 1);

        // "T" label
        font.getData().setScale(scale * 1.4f);
        font.setColor(1, 1, 1, 0.9f);
        glyphLayout.setText(font, "T");
        float textX = btnX + (btnSize - glyphLayout.width) / 2;
        float textY = btnY + (btnSize + glyphLayout.height) / 2;
        font.draw(spriteBatch, glyphLayout, textX, textY);
    }

    /**
     * Check if a touch hits the chat button.
     * @param touchX raw screen X from Gdx.input.getX()
     * @param touchY raw screen Y from Gdx.input.getY() (origin top-left)
     */
    public boolean isChatButtonTapped(float touchX, float touchY) {
        if (btnSize <= 0) return false;
        float ty = Gdx.graphics.getHeight() - touchY; // flip to y-up
        return touchX >= btnX && touchX <= btnX + btnSize
            && ty >= btnY && ty <= btnY + btnSize;
    }

    /** Show the Android native text input dialog for chat. */
    public void openChatInput() {
        Gdx.input.getTextInput(new Input.TextInputListener() {
            @Override
            public void input(String text) {
                String trimmed = text.trim();
                if (!trimmed.isEmpty() && RDClient.getInstance().isConnected()) {
                    RDClient.getInstance().sendChat(trimmed);
                }
            }

            @Override
            public void canceled() {
                // User cancelled — do nothing
            }
        }, "Chat", "", "Type a message...");
    }

    public void dispose() {
        messages.clear();
    }

    private static class ChatEntry {
        final String message;
        final long timestamp;
        ChatEntry(String message, long timestamp) {
            this.message = message;
            this.timestamp = timestamp;
        }
    }

    private static class VisibleMessage {
        final String text;
        final float alpha;
        VisibleMessage(String text, float alpha) {
            this.text = text;
            this.alpha = alpha;
        }
    }
}
