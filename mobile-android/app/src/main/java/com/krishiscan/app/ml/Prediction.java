package com.krishiscan.app.ml;

public class Prediction {
    public final String cropName;
    public final String diseaseName;
    public final double confidence;
    public final String rawLabel;

    public Prediction(String cropName, String diseaseName, double confidence, String rawLabel) {
        this.cropName = cropName;
        this.diseaseName = diseaseName;
        this.confidence = confidence;
        this.rawLabel = rawLabel;
    }
}
