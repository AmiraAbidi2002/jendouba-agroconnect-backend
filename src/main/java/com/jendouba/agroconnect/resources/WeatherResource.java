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


/**
 * Resource to fetch weather forecasts from OpenWeatherMap API.
 * Returns current weather, hourly forecast (next 24h), daily forecast (5 days), and extrapolated next 2 days.
 */
@Path("/weather")
@Produces(MediaType.APPLICATION_JSON)
public class WeatherResource {

    private final String API_KEY = "def24126ddcdbcea53a18b62e956302f";

    /**
     * GET /weather?lat={lat}&lon={lon}
     * Fetch weather forecast based on latitude and longitude.
     */
    @GET
    public Response getWeather(
            @QueryParam("lat") Double lat,
            @QueryParam("lon") Double lon
    ) {
        try {
            // Build OpenWeatherMap API URL
            String urlString = String.format(
                    "https://api.openweathermap.org/data/2.5/forecast?lat=%f&lon=%f&units=metric&appid=%s",
                    lat, lon, API_KEY
            );

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Read API response
            Scanner scanner = new Scanner(new InputStreamReader(conn.getInputStream()));
            StringBuilder json = new StringBuilder();
            while (scanner.hasNext()) {
                json.append(scanner.nextLine());
            }
            scanner.close();

            JSONObject forecast = new JSONObject(json.toString());
            JSONArray list = forecast.getJSONArray("list");

            JSONObject weather = new JSONObject();

            // Current weather
            weather.put("current", list.getJSONObject(0));

            // Hourly forecast(next 8 periods ~24h)
            JSONArray hourly = new JSONArray();
            for (int i = 0; i < Math.min(8, list.length()); i++) {
                hourly.put(list.getJSONObject(i));
            }
            weather.put("hourly", hourly);

            // Daily forecast (5 days, every 8th period)
            JSONArray daily = new JSONArray();
            for (int i = 0; i < list.length(); i += 8) {
                daily.put(list.getJSONObject(i));
            }

            // Extrapolate next 2 days based on trend
            int n = daily.length();
            JSONObject lastDay = daily.getJSONObject(n - 1);
            JSONObject secondLastDay = daily.getJSONObject(n - 2);

            for (int i = 0; i < 2; i++) {
                JSONObject extrapolated = new JSONObject(lastDay.toString());

                // Adjust temperature based on trend
                double tempDiff = lastDay.getJSONObject("main").getDouble("temp") -
                        secondLastDay.getJSONObject("main").getDouble("temp");
                double newTemp = lastDay.getJSONObject("main").getDouble("temp") + tempDiff;

                extrapolated.getJSONObject("main").put("temp", newTemp);

                 // Add 1 day to timestamp
                long newDt = lastDay.getLong("dt") + (i + 1) * 86400;
                extrapolated.put("dt", newDt);

                daily.put(extrapolated);
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
