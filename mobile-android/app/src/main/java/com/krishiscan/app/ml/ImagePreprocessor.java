package com.krishiscan.app.ml;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImagePreprocessor {
    public static final int INPUT_SIZE = 224;

    public ByteBuffer preprocess(Bitmap source) {
        Bitmap resized = Bitmap.createScaledBitmap(source, INPUT_SIZE, INPUT_SIZE, true);
        ByteBuffer input = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3);
        input.order(ByteOrder.nativeOrder());

        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        resized.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);

        for (int px : pixels) {
            float r = ((px >> 16) & 0xFF) / 255.0f;
            float g = ((px >> 8) & 0xFF) / 255.0f;
            float b = (px & 0xFF) / 255.0f;
            input.putFloat(r);
            input.putFloat(g);
            input.putFloat(b);
        }

        input.rewind();
        return input;
    }
}
