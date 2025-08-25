package com.jendouba.agroconnect.dto;

import java.util.List;

public class FarmDTO {
    public Long farmer_id;
    public String farmer_name;
    public String location;

    public double lat;
    public double lng;
    public List<CropDTO> crops;
}
