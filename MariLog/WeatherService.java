import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class WeatherService {
    // Sends the request and returns the reply text, or null if it failed
    private String fetch(String url) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5)).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url)).timeout(Duration.ofSeconds(5)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return null;
            }
            return response.body();
        } catch (Exception ex) {
            return null;
        }
    }

    // Returns NaN for "null" or anything that is not a number
    private double toNumber(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException ex) {
            return Double.NaN;
        }
    }

    public double getSeaTemperature(double latitude, double longitude) {
        try {
        String url = "https://marine-api.open-meteo.com/v1/marine?latitude=" + latitude
                    + "&longitude=" + longitude + "&current=sea_surface_temperature&timezone=Asia%2FManila";

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5)).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url)).timeout(Duration.ofSeconds(5)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return Double.NaN;
            }

            String body = response.body();
            int current = body.indexOf("\"current\":{");
            if (current < 0) {
                return Double.NaN;
            }
            String key = "\"sea_surface_temperature\":";
            int keyPos = body.indexOf(key, current);
            if (keyPos < 0) {
                return Double.NaN;
            }

            int start = keyPos + key.length();
            int end = start;
            while (end < body.length() && "-.0123456789".indexOf(body.charAt(end)) >= 0) {
                end++;
            }
            if (end == start) {
                return Double.NaN;
            }
            return Double.parseDouble(body.substring(start, end));
        } catch (Exception ex) {
            return Double.NaN;
        }
    }

    // Returns the current wave height in meters, or NaN if anything goes wrong
    public double getWaveHeight(double latitude, double longitude) {
        String url = "https://marine-api.open-meteo.com/v1/marine?latitude=" + latitude
                + "&longitude=" + longitude + "&current=wave_height&timezone=Asia%2FManila";

        String body = fetch(url);
        if (body == null) {
            return Double.NaN;
        }

        int current = body.indexOf("\"current\":{");
        if (current < 0) {
            return Double.NaN;
        }
        String key = "\"wave_height\":";
        int keyPos = body.indexOf(key, current);
        if (keyPos < 0) {
            return Double.NaN;
        }

        int start = keyPos + key.length();
        int end = start;
        while (end < body.length() && "-.0123456789".indexOf(body.charAt(end)) >= 0) {
            end++;
        }

        return toNumber(body.substring(start, end));
    }
}