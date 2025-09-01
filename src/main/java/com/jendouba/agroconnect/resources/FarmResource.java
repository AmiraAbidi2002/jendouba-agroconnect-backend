package com.jendouba.agroconnect.resources;

import com.jendouba.agroconnect.core.Crop;
import com.jendouba.agroconnect.core.User;
import com.jendouba.agroconnect.db.CropDAO;
import com.jendouba.agroconnect.db.UserDAO;
import com.jendouba.agroconnect.dto.CropDTO;
import com.jendouba.agroconnect.dto.FarmDTO;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

/**
 * Resource for handling farms.
 * Provides endpoints to get all farms, the logged-in farmer’s farm, and convert User->FarmDTO.
 */
@Path("api/farms")
@Produces(MediaType.APPLICATION_JSON)
public class FarmResource {
    private final UserDAO userDAO;
    private final CropDAO cropDAO;

    public FarmResource(UserDAO userDAO, CropDAO cropDAO) {
        this.userDAO = userDAO;
        this.cropDAO = cropDAO;
    }

    /**
     * GET /api/farms
     * Get all farms (all users of type FARMER).
     */
    @GET
    @UnitOfWork
    public List<FarmDTO> getAllFarms() {
        List<User> farmers = userDAO.findByType("FARMER");
        return farmers.stream().map(this::toFarmDTO).toList();
    }

    /**
     * GET /api/farms/mine?userId={id}
     * Get the farm of a specific farmer (usually the logged-in user).
     */
    @GET
    @Path("/mine")
    @UnitOfWork
    public Response getMyFarm(@QueryParam("userId") Long userId) {
        if (userId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing userId")
                    .build();
        }

        Optional<User> farmerOpt = userDAO.findById(userId);
        if (farmerOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Farmer not found")
                    .build();
        }

        return Response.ok(toFarmDTO(farmerOpt.get())).build();
    }

    /**
     * Convert a User entity to FarmDTO including farmer info, location, and crops.
     */
    private FarmDTO toFarmDTO(User farmer) {
        FarmDTO dto = new FarmDTO();
        dto.farmer_id = farmer.getUser_id();
        dto.farmer_name = farmer.getUser_name();
        dto.location = farmer.getLocation();

        // Parse latitude/longitude from Google Maps URL if available
        double[] coords = parseLatLngFromUrl(farmer.getLocation());
        dto.lat = coords[0];
        dto.lng = coords[1];

        // Map crops to CropDTO
        dto.crops = cropDAO.findByFarmerId(farmer.getUser_id())
                .stream()
                .map(c -> {
                    CropDTO cd = new CropDTO();
                    cd.crop_id = c.getCrop_id();
                    cd.crop_name = c.getCrop_name();
                    cd.crop_type = c.getCrop_type();
                    cd.quantity = c.getQuantity();
                    cd.price = c.getPrice();
                    cd.harvest_date = c.getHarvest_date() != null
                            ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(c.getHarvest_date())
                            : null;
                    cd.availability = c.isAvailability();
                    cd.img_url = c.getImage_url();
                    return cd;
                }).toList();

        return dto;
    }

    /**
     * Parse latitude and longitude from a Google Maps URL.
     * Returns {0,0} if parsing fails or URL is invalid.
     */
    private double[] parseLatLngFromUrl(String url) {
        try {
            if (url == null || !url.contains("?q=")) return new double[]{0,0};
            String[] sp = url.substring(url.indexOf("?q=") + 3).split(",");
            if (sp.length != 2) return new double[]{0,0};
            return new double[]{
                    Double.parseDouble(sp[0].trim()),
                    Double.parseDouble(sp[1].trim())
            };
        } catch (Exception e) {
            return new double[]{0,0};
        }
    }


}
