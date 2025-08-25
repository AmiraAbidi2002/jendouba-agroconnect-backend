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
import org.checkerframework.checker.units.qual.C;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/crops")
public class CropResource {

    private final CropDAO cropDAO;
    private final UserDAO userDAO;
    private static final String UPLOAD_DIR = "uploads/";

    public CropResource(CropDAO cropDAO, UserDAO userDAO) {
        this.cropDAO = cropDAO;
        this.userDAO = userDAO;
        // create the upload directory
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            System.err.println("Cannot create upload directory: " + e.getMessage());
        }
    }

    @OPTIONS
    @Path("{path: .*}")
    public Response handleOptions() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "http://localhost:5173")
                .header("Access-Control-Allow-Headers", "X-Requested-With,Content-Type,Accept,Origin,Authorization,Content-Disposition")
                .header("Access-Control-Allow-Methods", "OPTIONS,GET,PUT,POST,DELETE,HEAD")
                .header("Access-Control-Allow-Credentials", "true")
                .build();
    }

    @POST
    @UnitOfWork
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createCrop(
            @Auth User user,
            @FormDataParam("crop_name") String cropName,
            @FormDataParam("crop_type") String cropType,
            @FormDataParam("quantity") String quantityStr,
            @FormDataParam("price") String priceStr,
            @FormDataParam("harvest_date") String harvestDateStr,
            @FormDataParam("availability") String availabilityStr,
            @FormDataParam("image") InputStream uploadedInputStream,
            @FormDataParam("image") FormDataContentDisposition fileDetail) {

        try {
            Double quantity ;
            Double price ;
            try {
                quantity = Double.parseDouble(quantityStr);
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                System.out.println("❌ NUMBER FORMAT ERROR: " + e.getMessage());
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Invalid number format\"}")
                        .build();
            }
            Boolean availability = availabilityStr != null ? Boolean.parseBoolean(availabilityStr) : true;

            // Validation
            if (cropName == null || cropType == null || quantity == null || price == null || harvestDateStr == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Missing required fields\"}")
                        .build();
            }


            Crop crop = new Crop();
            crop.setCrop_name(cropName);
            crop.setCrop_type(cropType);
            crop.setQuantity(quantity);
            crop.setPrice(price);
            crop.setAvailability(availability != null ? availability : true);
            crop.setHarvest_date(java.sql.Date.valueOf(harvestDateStr));
            crop.setFarmer(user);

            // Gestion de l'image
            if (uploadedInputStream != null && fileDetail != null &&
                    fileDetail.getFileName() != null && !fileDetail.getFileName().isEmpty()) {
                String fileName = saveImage(uploadedInputStream, fileDetail.getFileName());
                crop.setImage_url(fileName);
            }
            System.out.println("✅ Attempting to save crop...");

            Crop persisted = cropDAO.create(crop);
            System.out.println("✅ Crop saved successfully with ID: " + persisted.getCrop_id());
            return Response.status(Response.Status.CREATED).entity(persisted).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error creating crop: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    // save image
    private String saveImage(InputStream uploadedInputStream, String originalFileName) throws IOException {
        String fileExtension = "";
        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        java.nio.file.Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);

        Files.copy(uploadedInputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFileName;
    }


    @GET
    @UnitOfWork
    public List<Crop> getAllCrops() {
        return cropDAO.findAll();
    }

    @GET
    @Path("/{id}")
    @UnitOfWork
    public Response getCropById(@PathParam("id") Long id) {
        Optional<Crop> crop = cropDAO.findById(id);
        return crop.map(c -> Response.ok(c).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("/{id}")
    @UnitOfWork
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateCrop(
            @Auth User user,
            @PathParam("id") Long id,
            @FormDataParam("crop_name") String cropName,
            @FormDataParam("crop_type") String cropType,
            @FormDataParam("quantity") String quantityStr,
            @FormDataParam("price") String priceStr,
            @FormDataParam("harvest_date") String harvestDateStr,
            @FormDataParam("availability") String availabilityStr,
            @FormDataParam("image") InputStream uploadedInputStream,
            @FormDataParam("image") FormDataContentDisposition fileDetail) {

        try {

            Optional<Crop> optionalCrop = cropDAO.findById(id);
            if (!optionalCrop.isPresent()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"Crop not found\"}")
                        .build();
            }

            // Parser les types
            Double quantity = Double.parseDouble((quantityStr));
            Double price = Double.parseDouble(priceStr);
            Boolean availability = availabilityStr != null ? Boolean.parseBoolean(availabilityStr) : true;



            Crop crop = optionalCrop.get();

            // Update
            if (cropName != null) crop.setCrop_name(cropName);
            if (cropType != null) crop.setCrop_type(cropType);
            if (quantity != null) crop.setQuantity(quantity);
            if (price != null) crop.setPrice(price);
            if (harvestDateStr != null) crop.setHarvest_date(java.sql.Date.valueOf(harvestDateStr));
            if (availability != null) crop.setAvailability(availability);

            // image
            if (uploadedInputStream != null && fileDetail != null &&
                    fileDetail.getFileName() != null && !fileDetail.getFileName().isEmpty()) {
                String fileName = saveImage(uploadedInputStream, fileDetail.getFileName());
                crop.setImage_url(fileName);
            }

            Crop updated = cropDAO.update(crop);
            System.out.println("✅ Crop updated successfully: " + updated.getCrop_id());
            return Response.ok(updated).build();

        } catch (Exception e) {
            System.out.println("❌ Error updating crop: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error updating crop: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @UnitOfWork
    public Response deleteCrop(@PathParam("id") Long id) {
        Optional<Crop> optionalCrop = cropDAO.findById(id);
        if (!optionalCrop.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        cropDAO.delete(optionalCrop.get());
        return Response.ok().build();
    }

    @GET
    @Path("/farmer/{farmerId}")
    @UnitOfWork
    public List<CropDTO> getCropsByFarmer(@PathParam("farmerId") Long farmerId) {
        System.out.println("Fetching crops for farmer ID: " + farmerId);
        List<Crop> crops=cropDAO.findByFarmerId(farmerId);
        return crops.stream().map(CropDTO::new).toList();

    }


    @GET
    @Path("/all")
    @UnitOfWork
    public List<CropDTO> getAllCropsDTO() {
        List<Crop> crops = cropDAO.findAll();
        return crops.stream().map(CropDTO::new).toList();
    }
}