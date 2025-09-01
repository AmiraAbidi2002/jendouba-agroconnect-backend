package com.jendouba.agroconnect.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jendouba.agroconnect.core.Crop;
import com.jendouba.agroconnect.core.User;
import com.jendouba.agroconnect.db.CropDAO;
import com.jendouba.agroconnect.db.UserDAO;
import com.jendouba.agroconnect.resources.CropResource;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class CropResourceTest {

    private CropDAO cropDAO;
    private UserDAO userDAO;
    private CropResource cropResource;
    private User mockUser;

    @BeforeEach
    void setup() {
        cropDAO = mock(CropDAO.class);
        userDAO = mock(UserDAO.class);
        cropResource = new CropResource(cropDAO, userDAO);

        // Simulated user for @Auth
        mockUser = new User("Amira", "amira@test.com", "password123", "FARMER", "Jendouba");
        mockUser.setUser_id(1L);
    }

    @Test
    void testCreateCrop_success() throws Exception {
        Crop crop = new Crop(
                1L,
                mockUser,
                "Tomato",
                50.0,
                "food crops",
                null,
                true,
                null,
                3.5
        );
        when(cropDAO.create(any(Crop.class))).thenReturn(crop);

        ByteArrayInputStream imageStream = new ByteArrayInputStream(new byte[]{1,2,3});
        Response resp = cropResource.createCrop(
                mockUser,
                "Tomato",
                "food crops",
                "50",
                "3.5",
                "2025-09-30",
                "true",
                imageStream
        );

        assertEquals(200, resp.getStatus());
        Crop saved = (Crop) resp.getEntity();
        assertEquals("Tomato", saved.getCrop_name());
    }

    @Test
    void testGetAllCrops() {
        Crop c1 = new Crop(1L, mockUser, "Tomato", 50.0, "food crops", null, true, null, 3.5);
        Crop c2 = new Crop(2L, mockUser, "corn", 30.0, "food crops", null, true, null, 2.5);

        when(cropDAO.findAll()).thenReturn(Arrays.asList(c1, c2));

        Response resp = cropResource.getAllCrops();
        assertEquals(200, resp.getStatus());

        List<?> crops = (List<?>) resp.getEntity();
        assertEquals(2, crops.size());
    }

    @Test
    void testGetMyCrops() {
        Crop c1 = new Crop(1L, mockUser, "Tomato", 50.0, "food crops", null, true, null, 3.5);
        when(cropDAO.findByFarmerId(1L)).thenReturn(Arrays.asList(c1));

        Response resp = cropResource.getMyCrops(mockUser);
        assertEquals(200, resp.getStatus());

        List<?> crops = (List<?>) resp.getEntity();
        assertEquals(1, crops.size());
    }

    @Test
    void testUpdateCrop_success() throws Exception {
        Crop existing = new Crop(10L, mockUser, "Tomato", 50.0, "Vegetable", null, true, null, 3.5);
        existing.setFarmer(mockUser);

        when(cropDAO.findById(10L)).thenReturn(Optional.of(existing));
        when(cropDAO.update(any(Crop.class))).thenReturn(existing);

        ByteArrayInputStream imageStream = new ByteArrayInputStream(new byte[]{1,2,3});
        Response resp = cropResource.updateCrop(
                mockUser,
                10L,
                "Tomato Update",
                null,
                "60",
                "4.0",
                "2025-10-01",
                "true",
                imageStream
        );

        assertEquals(200, resp.getStatus());
        Crop updated = (Crop) resp.getEntity();
        assertEquals("Tomato Update", updated.getCrop_name());
        assertEquals(60.0, updated.getQuantity());
    }

    @Test
    void testDeleteCrop_success() {
        Crop crop = new Crop(10L, mockUser, "Tomato", 50.0, "food crops", null, true, null, 3.5);
        crop.setFarmer(mockUser);

        when(cropDAO.findById(10L)).thenReturn(Optional.of(crop));
        doNothing().when(cropDAO).delete(crop);

        Response resp = cropResource.deleteCrop(mockUser, 10L);
        assertEquals(204, resp.getStatus());
    }

    @Test
    void testGetCropsByFarmer() {
        Crop c1 = new Crop(1L, mockUser, "Tomato", 50.0, "food crops", null, true, null, 3.5);
        when(cropDAO.findByFarmerId(1L)).thenReturn(Arrays.asList(c1));

        Response resp = cropResource.getCropsByFarmer(1L);
        assertEquals(200, resp.getStatus());

        List<?> crops = (List<?>) resp.getEntity();
        assertEquals(1, crops.size());
    }
}
