package ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import input.CameraPreviewReceiver;

// Draws the live camera preview overlay in-game.
public class CameraPreviewOverlay {
    private static final float PREVIEW_MARGIN = 16f;
    private static final float PREVIEW_MAX_WIDTH = 320f;

    private final CameraPreviewReceiver receiver;
    private Texture previewTexture;
    private int previewWidth;
    private int previewHeight;

    public CameraPreviewOverlay(CameraPreviewReceiver receiver) {
        this.receiver = receiver;
    }

    public void start() {
        receiver.start();
    }

    public void stop() {
        receiver.stop();
    }

    public void update() {
        byte[] frame = receiver.pollFrame();
        if (frame == null) return;

        Pixmap pixmap = new Pixmap(frame, 0, frame.length);
        if (previewTexture == null
            || previewTexture.getWidth() != pixmap.getWidth()
            || previewTexture.getHeight() != pixmap.getHeight()) {
            if (previewTexture != null) {
                previewTexture.dispose();
            }
            previewTexture = new Texture(pixmap);
        } else {
            previewTexture.draw(pixmap, 0, 0);
        }
        previewWidth = pixmap.getWidth();
        previewHeight = pixmap.getHeight();
        pixmap.dispose();
    }

    public void draw(SpriteBatch batch) {
        if (previewTexture == null || previewWidth <= 0 || previewHeight <= 0) return;
        float drawWidth = previewWidth;
        float drawHeight = previewHeight;
        if (drawWidth > PREVIEW_MAX_WIDTH) {
            float scale = PREVIEW_MAX_WIDTH / drawWidth;
            drawWidth *= scale;
            drawHeight *= scale;
        }
        batch.draw(previewTexture, PREVIEW_MARGIN, PREVIEW_MARGIN, drawWidth, drawHeight);
    }

    public void dispose() {
        if (previewTexture != null) previewTexture.dispose();
    }
}
