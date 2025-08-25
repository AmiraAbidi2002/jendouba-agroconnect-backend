package com.jendouba.agroconnect.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

@Path("/weather")
@Produces(MediaType.APPLICATION_JSON)
public class WeatherResource {

    private final String API_KEY = "def24126ddcdbcea53a18b62e956302f";

    @GET
    public Response getWeather(
            @QueryParam("lat") double lat,
            @QueryParam("lon") double lon
    ) {
        try {
            // Endpoint forecast free
            String urlString = String.format(
                    "https://api.openweathermap.org/data/2.5/forecast?lat=%f&lon=%f&units=metric&appid=%s",
                    lat, lon, API_KEY
            );

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            Scanner scanner = new Scanner(new InputStreamReader(conn.getInputStream()));
            StringBuilder json = new StringBuilder();
            while (scanner.hasNext()) {
                json.append(scanner.nextLine());
            }
            scanner.close();

            JSONObject forecast = new JSONObject(json.toString());

            JSONObject weather = new JSONObject();

            JSONArray list = forecast.getJSONArray("list");

            // Current
            weather.put("current", list.getJSONObject(0));

            // Hourly : 8 next period (~24h)
            JSONArray hourly = new JSONArray();
            for (int i = 0; i < Math.min(8, list.length()); i++) {
                hourly.put(list.getJSONObject(i));
            }
            weather.put("hourly", hourly);

            // Daily :  (~1 jour)
            JSONArray daily = new JSONArray();
            for (int i = 0; i < list.length(); i += 8) {
                daily.put(list.getJSONObject(i));
            }
            weather.put("daily", daily);

            return Response.ok(weather.toString()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Failed to fetch weather\"}")
                    .build();
        }
    }
}
