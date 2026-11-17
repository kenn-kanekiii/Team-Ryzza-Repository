import java.util.ArrayList;


class Alert {
    private int entryId;
    private String level;      
    private String message;
    private String date;

    public Alert(int entryId, String level, String message, String date) {
        this.entryId = entryId;
        this.level = level;
        this.message = message;
        this.date = date;
    }

    public int getEntryId() { return entryId; }
    public String getLevel() { return level; }
    public String getMessage() { return message; }
    public String getDate() { return date; }
}

public class AlertScreen {
    private ArrayList<Alert> alertHistory = new ArrayList<Alert>();

    public boolean checkSeverity(Entry entry) {
        if (entry == null) {
            return false;
        }
        return entry.isHighSeverity();
    }

    public Alert triggerAlert(Entry entry) {
        if (!checkSeverity(entry)) {
            return null;
        }

        String message = entry.getType().toUpperCase() + " at " + entry.getLocation()
                + ": " + entry.getSummary();
        Alert alert = new Alert(entry.getId(), "hIGH", message, entry.getDate());
        alertHistory.add(alert);

        System.out.println();
        System.out.println("============================================================");
        System.out.println("  [HIGH SEVERITY ALERT] Entry #" + entry.getId());
        System.out.println("  " + message);
        System.out.println("============================================================");
        return alert;
    }

    public ArrayList<String> checkSeasonalAlerts(String location, String date) {
        ArrayList<String> alerts = new ArrayList<String>();
            // TODO: hardcoded seasonal rules
        return alerts;
    }

    public static final double HEAT_WAVE_TEMP = 10; //31 dapat , for demonstration lang
    public static final double ROUGH_SEA_WAVE = 2.5;   // wave height in m, this or higher (adjustable)


    private WeatherService weather = new WeatherService();   

    public ArrayList<String> checkWeatherWarnings(double latitude, double longitude) {
        ArrayList<String> warnings = new ArrayList<String>();

        double seaTemp = weather.getSeaTemperature(latitude, longitude);
        double waveHeight = weather.getWaveHeight(latitude, longitude);

        if (Double.isNaN(seaTemp) && Double.isNaN(waveHeight)) {
            warnings.add("Weather data unavailable (no connection, or no ocean data here).");
            return warnings;
        }
        if (!Double.isNaN(waveHeight) && waveHeight >= ROUGH_SEA_WAVE) {
            warnings.add(String.format("Rough seas: wave height %.1f m", waveHeight));
        }

        if (seaTemp >= HEAT_WAVE_TEMP) {
            warnings.add(String.format("Heat wave: sea surface temperature %.1f C", seaTemp));
        }

            if (warnings.isEmpty()) {
                System.out.println("No weather warnings.");
            }

            for (String w : warnings) {
                System.out.println("[WARNING] " + w);
            }

        return warnings;
    }
     public void showWeatherWarnings(double latitude, double longitude) {
    ArrayList<String> warnings = checkWeatherWarnings(latitude, longitude);

    if (warnings.isEmpty()) {
        System.out.println("No weather warnings.");
    }
    for (String w : warnings) {
        System.out.println("[WARNING] " + w);
    }
    }
    

}