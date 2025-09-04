package com.jendouba.agroconnect.dto;

import com.jendouba.agroconnect.core.Crop;

import java.text.SimpleDateFormat;

/**
 * DTO for Crop entity.
 * Converts Crop entity fields into a simplified structure for frontend transfer.
 */

public class CropDTO {
    public Long crop_id;
    public Long farmer_id;
    public String farmer_name;
    public String crop_name;
    public String crop_type;
    public Double quantity;
    public Double price;
    public String harvest_date;// Harvest date formatted as yyyy-MM-dd
    public boolean availability;
    public String img_url;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");


    public CropDTO() {
    }

    /**
     * Converts a Crop entity to a CropDTO.
     * Formats the harvest date and includes farmer information.
     */
    public CropDTO(Crop c) {
        this.crop_id = c.getCrop_id();
        this.farmer_id = c.getFarmer().getUser_id();
        this.farmer_name=c.getFarmer().getUser_name();
        this.crop_name = c.getCrop_name();
        this.crop_type = c.getCrop_type();
        this.quantity = c.getQuantity();
        this.price = c.getPrice();
        this.harvest_date = c.getHarvest_date() != null ? DATE_FORMAT.format(c.getHarvest_date()) : null;
        this.availability = c.isAvailability();
        if (c.getImage_url() != null) {
            this.img_url = "https://jendouba-agroconnect-backend-1.onrender.com/api/crops/image/" + c.getImage_url();
        } else {
            this.img_url = null;
        }
    }

    // Getters and setters
    public long getCrop_id() {
        return crop_id;
    }
     public void setCrop_id(long crop_id) {
        this.crop_id = crop_id;
    }

    public String getCrop_name() {
        return crop_name;
    }

    public void setCrop_name(String crop_name) {
        this.crop_name = crop_name;
    }

    public String getCrop_type() {
        return crop_type;
    }

    public void setCrop_type(String crop_type) {
        this.crop_type = crop_type;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getHarvest_date() {
        return harvest_date;
    }

    public void setHarvest_date(String harvest_date) {
        this.harvest_date = harvest_date;
    }

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getFarmer_name() {
        return farmer_name;
    }

    public void setFarmer_name(String farmer_name) {
        this.farmer_name = farmer_name;
    }
}