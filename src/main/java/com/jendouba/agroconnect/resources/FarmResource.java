package com.jendouba.agroconnect.resources;


import com.jendouba.agroconnect.core.User;
import com.jendouba.agroconnect.db.CropDAO;
import com.jendouba.agroconnect.db.UserDAO;
import com.jendouba.agroconnect.dto.CropDTO;
import com.jendouba.agroconnect.dto.FarmDTO;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.errors.ErrorMessage;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.Optional;

@Path("api/farms")
@Produces(MediaType.APPLICATION_JSON)
public class FarmResource {
    private final UserDAO userDAO;
    private final CropDAO cropDAO;

    public FarmResource(UserDAO userDAO, CropDAO cropDAO) {
        this.userDAO = userDAO;
        this.cropDAO = cropDAO;
    }

    @GET
    @UnitOfWork
    public List<FarmDTO> getAllFarms() {
        List<User> farmers = userDAO.findByType("FARMER");
        return farmers.stream().map(this::toFarmDTO).toList();
    }
    @GET
    @Path("/mine")
    @UnitOfWork
    public Response getMyFarm(@Auth User user) {
        try {
            System.out.println("User authenticated : " + user);
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("User not authenticated")
                        .build();
            }
            FarmDTO farm = toFarmDTO(user);
            System.out.println("FarmDTO : " + farm);
            return Response.ok(farm).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity("Error server: " + e.getMessage()).build();
        }
    }

    private FarmDTO toFarmDTO(User farmer) {
        FarmDTO dto = new FarmDTO();
        dto.farmer_id = farmer.getUser_id();
        dto.farmer_name = farmer.getUser_name();
        dto.location = farmer.getLocation();

        double[] coords = parseLatLngFromUrl(farmer.getLocation());
        dto.lat = coords[0];
        dto.lng = coords[1];

        dto.crops = cropDAO.findByFarmerId(farmer.getUser_id())
                .stream().map(c -> {
            CropDTO cd = new CropDTO();
            cd.crop_id = c.getCrop_id();
            cd.crop_name = c.getCrop_name();
            cd.crop_type = c.getCrop_type();
            cd.quantity = c.getQuantity();
            cd.price = c.getPrice();
            if (c.getHarvest_date() != null) {
                cd.harvest_date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(c.getHarvest_date());
            } else {
                cd.harvest_date = null;
            }
            cd.availability = c.isAvailability();
            cd.img_url = c.getImage_url();
            return cd;
        }).toList();
        System.out.println(">>> Building FarmDTO for farmer " + farmer.getUser_id());
        System.out.println("Location = " + farmer.getLocation());

        return dto;
    }

    // Parse "https://www.google.com/maps?q=36.5,8.8"
    private double[] parseLatLngFromUrl(String url) {
        try {
            if (url == null || !url.contains("?q=")) return new double[]{0,0};
            String[] sp = url.substring(url.indexOf("?q=")+3).split(",");
            if (sp.length != 2) return new double[]{0,0};
            return new double[]{Double.parseDouble(sp[0]), Double.parseDouble(sp[1])};
        } catch (Exception e) {
            return new double[]{0,0};
        }
    }

}

