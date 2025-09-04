package com.jendouba.agroconnect.resources;

import com.jendouba.agroconnect.core.Crop;
import com.jendouba.agroconnect.core.User;
import com.jendouba.agroconnect.db.CropDAO;
import com.jendouba.agroconnect.db.UserDAO;
import com.jendouba.agroconnect.dto.CropDTO;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Resource handling all CRUD operations on crops.
 * Supports creating, updating, deleting, and fetching crops.
 */
@Path("api/crops")
@Produces(MediaType.APPLICATION_JSON)
public class CropResource {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final Logger LOGGER = LoggerFactory.getLogger(CropResource.class);

    private final CropDAO cropDAO;
    private final UserDAO userDAO;

    public CropResource(CropDAO cropDAO, UserDAO userDAO) {
        this.cropDAO = cropDAO;
        this.userDAO=userDAO;
    }

    /**
     * save  file  in  uploads directory
     */
    private String saveFile(InputStream fileStream) throws IOException {
        // Generate a unique file name
        String fileName = UUID.randomUUID().toString() ;

        // Create the uploads folder if it does not exist
        File uploadDir = new File("uploads");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Save the file
        File file = new File(uploadDir, fileName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        return fileName; // Returns the file name for the DB
    }

    /**
     * Get all crops (from all farmers)
     */
    @GET
    @UnitOfWork
    public Response getAllCrops() {
        List<Crop> crops = cropDAO.findAll();
        // Convert to DTO to include farmer_id
        List<CropDTO> cropDTOs = crops.stream()
                .map(CropDTO::new)
                .collect(Collectors.toList());
        return Response.ok(cropDTOs).build();
    }

    /**
     * Get MY crops only (logged-in farmer)
     */
    @GET
    @Path("/mine")
    @UnitOfWork
    public Response getMyCrops(@Auth User user) {
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\":\"Unauthorized - invalid or missing token\"}")
                    .build();
        }
        List<Crop> crops = cropDAO.findByFarmerId(user.getUser_id());
        // Convert to DTO
        List<CropDTO> cropDTOs = crops.stream()
                .map(CropDTO::new)
                .collect(Collectors.toList());
        return Response.ok(cropDTOs).build();
    }

    /**
     * Create a new crop for the authenticated farmer.
     * Handles multipart form data for crop attributes and optional image upload.
     */
    @POST
    @UnitOfWork
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createCrop(@Auth User user,
                               @FormDataParam("crop_name") String cropName,
                               @FormDataParam("crop_type") String cropType,
                               @FormDataParam("quantity") String quantityStr,
                               @FormDataParam("price") String priceStr,
                               @FormDataParam("harvest_date") String harvestDateStr,
                               @FormDataParam("availability") String availabilityStr,
                               @FormDataParam("image") InputStream imageStream) {
        try {
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\":\"Unauthorized - invalid or missing token\"}")
                        .build();
            }

            LOGGER.info("Creating crop for user_id=" + user.getUser_id());

            Crop crop = new Crop();
            crop.setCrop_name(cropName);
            crop.setCrop_type(cropType);

            if (quantityStr != null) crop.setQuantity(Double.parseDouble(quantityStr));
            if (priceStr != null) crop.setPrice(Double.parseDouble(priceStr));

            // Parse harvest date
            if (harvestDateStr != null && !harvestDateStr.isEmpty()) {
                try {
                    Date harvestDate = DATE_FORMAT.parse(harvestDateStr);
                    crop.setHarvest_date(harvestDate);
                } catch (ParseException e) {
                    LOGGER.error("Invalid date format: " + harvestDateStr, e);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"message\":\"Invalid date format. Use YYYY-MM-DD\"}")
                            .build();
                }
            }

            if (availabilityStr != null) crop.setAvailability(Boolean.parseBoolean(availabilityStr));
            crop.setFarmer(user);

            // Handle image upload if present
            if (imageStream != null) {
                // Implement your image saving logic here
                String imagePath = saveFile(imageStream);
                 crop.setImage_url(imagePath);
                LOGGER.info("Image received for crop: " + cropName);
            }

            Crop saved = cropDAO.create(crop);
            return Response.ok(saved).build();

        } catch (Exception e) {
            LOGGER.error("Error creating crop", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"Invalid input: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Update an existing crop
     * * Validates ownership and updates fields.
     */
    @PUT
    @Path("/{id}")
    @UnitOfWork
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateCrop(@Auth User user,
                               @PathParam("id") Long id,
                               @FormDataParam("crop_name") String cropName,
                               @FormDataParam("crop_type") String cropType,
                               @FormDataParam("quantity") String quantityStr,
                               @FormDataParam("price") String priceStr,
                               @FormDataParam("harvest_date") String harvestDateStr,
                               @FormDataParam("availability") String availabilityStr,
                               @FormDataParam("image") InputStream imageStream) {
        try {
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\":\"Unauthorized - invalid or missing token\"}")
                        .build();
            }

            LOGGER.info("Updating crop ID=" + id + " for user_id=" + user.getUser_id());

            Optional<Crop> optionalCrop = cropDAO.findById(id);
            if (optionalCrop.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\":\"Crop not found\"}")
                        .build();
            }

            Crop crop = optionalCrop.get();

            // Verify that this crop belongs to the logged-in farmer
            if (!crop.getFarmer().getUser_id().equals(user.getUser_id())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"message\":\"You are not allowed to update this crop\"}")
                        .build();
            }

            // Update fields
            if (cropName != null) crop.setCrop_name(cropName);
            if (cropType != null) crop.setCrop_type(cropType);
            if (quantityStr != null) crop.setQuantity(Double.parseDouble(quantityStr));
            if (priceStr != null) crop.setPrice(Double.parseDouble(priceStr));

            if (harvestDateStr != null && !harvestDateStr.isEmpty()) {
                try {
                    Date harvestDate = DATE_FORMAT.parse(harvestDateStr);
                    crop.setHarvest_date(harvestDate);
                } catch (ParseException e) {
                    LOGGER.error("Invalid date format: " + harvestDateStr, e);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"message\":\"Invalid date format. Use YYYY-MM-DD\"}")
                            .build();
                }
            }

            if (availabilityStr != null) crop.setAvailability(Boolean.parseBoolean(availabilityStr));

            // Handle image upload if present
            if (imageStream != null) {
                // Implement your image saving logic here
                 String imagePath = saveFile(imageStream);
                crop.setImage_url(imagePath);
                LOGGER.info("Image updated for crop: " + cropName);
            }

            Crop updated = cropDAO.update(crop);
            return Response.ok(updated).build();

        } catch (Exception e) {
            LOGGER.error("Error updating crop", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"Invalid update: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Delete a crop if it belongs to the logged-in farmer.
     */
    @DELETE
    @Path("/{id}")
    @UnitOfWork
    public Response deleteCrop(@Auth User user, @PathParam("id") Long id) {
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\":\"Unauthorized - invalid or missing token\"}")
                    .build();
        }

        Optional<Crop> optionalCrop = cropDAO.findById(id);
        if (optionalCrop.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\":\"Crop not found\"}")
                    .build();
        }

        Crop crop = optionalCrop.get();

        if (!crop.getFarmer().getUser_id().equals(user.getUser_id())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\":\"You are not allowed to delete this crop\"}")
                    .build();
        }

        cropDAO.delete(crop);
        return Response.noContent().build();
    }


    /**
     * Get crops for a specific farmer by their ID.
     */
    @GET
    @Path("/farmer/{farmerId}")
    @Produces(MediaType.APPLICATION_JSON)
    @UnitOfWork
    public Response getCropsByFarmer(@PathParam("farmerId") Long farmerId) {
        List<Crop> crops = cropDAO.findByFarmerId(farmerId);

        // Convert en DTO
        List<CropDTO> cropDTOs = crops.stream()
                .map(CropDTO::new)
                .collect(Collectors.toList());
        return Response.ok(cropDTOs).build();
    }



    @GET
    @Path("/image/{filename}")
    public Response getImage(@PathParam("filename") String filename) {
        InputStream in = getClass().getClassLoader()
                .getResourceAsStream("uploads" + filename);


        if (in==null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        StreamingOutput stream = output -> {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }

        };

        String mimeType;
        if (filename.endsWith(".png")) {
            mimeType = "image/png";
        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            mimeType = "image/jpeg";
        } else if (filename.endsWith(".gif")) {
            mimeType = "image/gif";
        } else {

            mimeType = "image/jpeg";
        }
        return Response.ok(stream, mimeType)
                .header("Content-Disposition", "inline; filename=\"" )
                .build();
    }

}