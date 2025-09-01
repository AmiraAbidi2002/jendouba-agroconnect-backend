package com.jendouba.agroconnect.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jendouba.agroconnect.core.Crop;
import com.jendouba.agroconnect.core.User;
import com.jendouba.agroconnect.db.CropDAO;
import com.jendouba.agroconnect.db.UserDAO;
import com.jendouba.agroconnect.dto.FarmDTO;
import com.jendouba.agroconnect.resources.FarmResource;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class FarmResourceTest {

    private UserDAO userDAO;
    private CropDAO cropDAO;
    private FarmResource farmResource;

    private User mockUser;
    private Crop mockCrop;

    @BeforeEach
    void setup() {
        userDAO = mock(UserDAO.class);
        cropDAO = mock(CropDAO.class);
        farmResource = new FarmResource(userDAO, cropDAO);

        // Mock User (farmer)
        mockUser = new User("Amira", "amira@test.com", "password123", "FARMER", "36.5,9.2");
        mockUser.setUser_id(1L);

        // Mock Crop
        mockCrop = new Crop();
        mockCrop.setCrop_id(10L);
        mockCrop.setFarmer(mockUser);
        mockCrop.setCrop_name("Tomato");
        mockCrop.setCrop_type("food crops");
        mockCrop.setQuantity(50.0);
        mockCrop.setPrice(3.5);
        mockCrop.setHarvest_date(new Date());
        mockCrop.setAvailability(true);
        mockCrop.setImage_url(null);
    }

    @Test
    void testGetAllFarms() {
        when(userDAO.findByType("FARMER")).thenReturn(Arrays.asList(mockUser));
        when(cropDAO.findByFarmerId(1L)).thenReturn(Arrays.asList(mockCrop));

        List<FarmDTO> farms = farmResource.getAllFarms();
        assertEquals(1, farms.size());
        assertEquals("Amira", farms.get(0).farmer_name);
        assertEquals(1, farms.get(0).crops.size());
    }

    @Test
    void testGetMyFarm_success() {
        when(userDAO.findById(1L)).thenReturn(Optional.of(mockUser));
        when(cropDAO.findByFarmerId(1L)).thenReturn(Arrays.asList(mockCrop));

        Response resp = farmResource.getMyFarm(1L);
        assertEquals(200, resp.getStatus());

        FarmDTO farm = (FarmDTO) resp.getEntity();
        assertEquals("Amira", farm.farmer_name);
        assertEquals(1, farm.crops.size());
    }

    @Test
    void testGetMyFarm_missingUserId() {
        Response resp = farmResource.getMyFarm(null);
        assertEquals(400, resp.getStatus());
    }

    @Test
    void testGetMyFarm_userNotFound() {
        when(userDAO.findById(999L)).thenReturn(Optional.empty());
        Response resp = farmResource.getMyFarm(999L);
        assertEquals(404, resp.getStatus());
    }
}
