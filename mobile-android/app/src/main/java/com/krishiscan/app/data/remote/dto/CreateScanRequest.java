package com.krishiscan.app.data.remote.dto;

public class CreateScanRequest {
    public String cropName;
    public String diseaseName;
    public double confidence;
    public String imageUrl;
    public String imagePath;
    public LocationDto location;

    public static class LocationDto {
        public double lat;
        public double lng;
    }
}

