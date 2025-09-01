package com.jendouba.agroconnect.dto;

import java.util.List;
/**
 * DTO representing a farm and its associated crops.
 * Includes farmer information and the list of CropDTOs.
 */
public class FarmDTO {
    public Long farmer_id;
    public String farmer_name;
    public String location;

    public double lat;
    public double lng;
    public List<CropDTO> crops;

    // Getters and setters
    public Long getFarmer_id() {
        return farmer_id;
    }

    public void setFarmer_id(Long farmer_id) {
        this.farmer_id = farmer_id;
    }

    public String getFarmer_name() {
        return farmer_name;
    }

    public void setFarmer_name(String farmer_name) {
        this.farmer_name = farmer_name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public List<CropDTO> getCrops() {
        return crops;
    }

    public void setCrops(List<CropDTO> crops) {
        this.crops = crops;
    }
}
