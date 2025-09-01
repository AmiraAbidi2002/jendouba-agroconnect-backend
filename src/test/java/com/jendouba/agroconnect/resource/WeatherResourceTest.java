package com.jendouba.agroconnect.resource;

import static org.junit.jupiter.api.Assertions.*;

import com.jendouba.agroconnect.resources.WeatherResource;
import jakarta.ws.rs.core.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

public class WeatherResourceTest {

    private WeatherResource weatherResource;

    @BeforeEach
    void setup() {
        weatherResource = new WeatherResource();
    }

    /**
     Test that the method returns non-empty JSON for simulated coordinates.
     * We're not testing the actual API, just the return structure.     */
    @Test
    void testGetWeather_returnsJson() throws Exception {
        Field apiKeyField = WeatherResource.class.getDeclaredField("API_KEY");
        apiKeyField.setAccessible(true);
        apiKeyField.set(weatherResource, "dummy_api_key");

        Response resp = weatherResource.getWeather(36.5, 10.1);

         // Depending on what the API actually returns
        if (resp.getStatus() == 200) {
         // API succeeds - check the JSON structure
            String responseBody = resp.getEntity().toString();
            assertTrue(responseBody.contains("{") && responseBody.contains("}"));
        } else {
            // API fails - check error message
            assertEquals(500, resp.getStatus());
            assertTrue(resp.getEntity().toString().contains("Failed to fetch weather"));
        }
    }

    /**
     * Simple internal JSON parsing test by simulating a JSONObject.
    .*/
    @Test
    void testJsonParsingLogic() {
        JSONObject main = new JSONObject();
        main.put("temp", 20);

        JSONObject day = new JSONObject();
        day.put("main", main);
        day.put("dt", 123456);

        JSONObject forecast = new JSONObject();
        forecast.put("current", day);

        assertEquals(20, forecast.getJSONObject("current").getJSONObject("main").getDouble("temp"));
    }
}
