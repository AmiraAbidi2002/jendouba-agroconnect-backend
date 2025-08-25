package com.jendouba.agroconnect.core;

import jakarta.persistence.*;

import java.util.Date;


@Entity
@Table(name = "Crops")
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crop_id")
    private Long crop_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id",nullable = false)
    private User farmer;
    @Column(name = "crop_name",nullable = false)
    private  String crop_name;
    @Column(name = "quantity",nullable = false)
    private Double quantity;
    @Column(name = "crop_type",nullable = false)
    private String crop_type;

    @Temporal(TemporalType.DATE)
    @Column(name = "harvest_date",nullable = false)
    private Date harvest_date;
    @Column(name = "availability",nullable = false)
    private boolean availability=true;
    @Column(name = "image_url")
    private String image_url;
    @Column(name = "price",nullable = false)
    private Double price;

    public Crop() {
    }

    public long getCrop_id() {
        return crop_id;
    }

    public void setCrop_id(long crop_id) {
        this.crop_id = crop_id;
    }

    public User getFarmer() {
        return farmer;
    }

    public void setFarmer(User farmer) {
        this.farmer = farmer;
    }

    public String getCrop_name() {
        return crop_name;
    }

    public void setCrop_name(String crop_name) {
        this.crop_name = crop_name;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getCrop_type() {
        return crop_type;
    }

    public void setCrop_type(String crop_type) {
        this.crop_type = crop_type;
    }

    public Date getHarvest_date() {
        return harvest_date;
    }

    public void setHarvest_date(Date harvest_date) {
        this.harvest_date = harvest_date;
    }

    public boolean isAvailability() { return availability;}

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
