package com.krishiscan.app.ml;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImagePreprocessor {
    private final int inputWidth;
    private final int inputHeight;

    public ImagePreprocessor(int inputWidth, int inputHeight) {
        this.inputWidth = inputWidth;
        this.inputHeight = inputHeight;
    }

    public ByteBuffer preprocess(Bitmap source) {
        Bitmap cropped = centerCropToSquare(source);
        Bitmap resized = Bitmap.createScaledBitmap(cropped, inputWidth, inputHeight, true);
        ByteBuffer input = ByteBuffer.allocateDirect(4 * inputWidth * inputHeight * 3);
        input.order(ByteOrder.nativeOrder());

        int[] pixels = new int[inputWidth * inputHeight];
        resized.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        for (int px : pixels) {
            float r = (px >> 16) & 0xFF;
            float g = (px >> 8) & 0xFF;
            float b = px & 0xFF;
            input.putFloat(r);
            input.putFloat(g);
            input.putFloat(b);
        }

        input.rewind();
        return input;
    }

    private Bitmap centerCropToSquare(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        if (width == height) return source;

        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        return Bitmap.createBitmap(source, x, y, size, size);
    }
}
