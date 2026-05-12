package com.krishiscan.app.ml;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DiseaseClassifier {
    private static final String MODEL_FILE = "krishiscan_model.tflite";
    private static final String LABELS_FILE = "labels.txt";

    private final Interpreter interpreter;
    private final List<String> labels;
    private final ImagePreprocessor preprocessor;
    private final int outputClassCount;

    public DiseaseClassifier(Context context) throws IOException {
        this.interpreter = new Interpreter(loadModelFile(context));
        this.labels = loadLabels(context);
        int[] inputShape = interpreter.getInputTensor(0).shape();
        int[] outputShape = interpreter.getOutputTensor(0).shape();

        if (inputShape.length < 4) {
            throw new IOException("Unexpected model input shape");
        }
        if (outputShape.length < 2) {
            throw new IOException("Unexpected model output shape");
        }

        int inputHeight = inputShape[1];
        int inputWidth = inputShape[2];
        this.outputClassCount = outputShape[outputShape.length - 1];

        if (labels.size() != outputClassCount) {
            throw new IOException("labels.txt count (" + labels.size() + ") does not match model output classes (" + outputClassCount + ")");
        }

        this.preprocessor = new ImagePreprocessor(inputWidth, inputHeight);
    }

    public Prediction classify(android.graphics.Bitmap bitmap) {
        ByteBuffer input = preprocessor.preprocess(bitmap);
        float[][] output = new float[1][outputClassCount];
        interpreter.run(input, output);

        int bestIndex = 0;
        float bestScore = output[0][0];
        for (int i = 1; i < output[0].length; i++) {
            if (output[0][i] > bestScore) {
                bestScore = output[0][i];
                bestIndex = i;
            }
        }

        String raw = labels.get(bestIndex);
        ParsedLabel parsed = parseLabel(raw);
        return new Prediction(parsed.cropName, parsed.diseaseName, bestScore, raw);
    }

    public void close() {
        interpreter.close();
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_FILE);
        try (FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
             FileChannel fileChannel = inputStream.getChannel()) {
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    private List<String> loadLabels(Context context) throws IOException {
        List<String> list = new ArrayList<>();
        InputStream in = context.getAssets().open(LABELS_FILE);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) list.add(trimmed);
            }
        }
        if (list.isEmpty()) throw new IOException("labels.txt is empty");
        return list;
    }

    private ParsedLabel parseLabel(String raw) {
        String normalized = raw.replace("___", "__").replace('_', ' ');
        String[] split = normalized.split("__");

        if (split.length >= 2) {
            return new ParsedLabel(clean(split[0]), clean(split[1]));
        }

        String[] words = normalized.trim().split(" ");
        if (words.length >= 2) {
            String crop = clean(words[0]);
            String disease = clean(normalized.substring(words[0].length()).trim());
            return new ParsedLabel(crop, disease);
        }

        return new ParsedLabel("Unknown", clean(raw));
    }

    private String clean(String value) {
        String compact = value.trim().replaceAll("\\s+", " ");
        if (compact.isEmpty()) return compact;
        String lower = compact.toLowerCase(Locale.US);
        StringBuilder sb = new StringBuilder();
        boolean cap = true;
        for (char c : lower.toCharArray()) {
            if (cap && Character.isLetter(c)) {
                sb.append(Character.toUpperCase(c));
                cap = false;
            } else {
                sb.append(c);
            }
            if (c == ' ') cap = true;
        }
        return sb.toString();
    }

    private static class ParsedLabel {
        final String cropName;
        final String diseaseName;

        ParsedLabel(String cropName, String diseaseName) {
            this.cropName = cropName;
            this.diseaseName = diseaseName;
        }
    }
}
