import java.util.ArrayList;
import java.util.Scanner;

/*
 * LogEntryApp.java
 * Member 1: Gene Kenneth V. Limjoco (data logging + Log Entry screen)
*/
abstract class Entry {
    private int id;
    private String location;
    private double latitude;
    private double longitude;
    private String date;        // YYYY-MM-DD
    private String severity;    // "high" or "normal"
    

    public Entry(String location, double latitude, double longitude, String date) {
        this.id = 0;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.severity = "normal";
    }

    public int getId() { return id; }
    public String getLocation() { return location; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getDate() { return date; }
    public String getSeverity() { return severity; }

    public void setId(int id) { this.id = id; }
    public void setLocation(String location) { this.location = location; }
    public void setDate(String date) { this.date = date; }
    public void setSeverity(String severity) { this.severity = severity; }

    // Each child class must write its own version of these methods
    public abstract String getType();          // "species", "coral", or "debris"
    public abstract boolean isHighSeverity();  // true if this entry is serious
    public abstract String getSummary();       // short text about this entry
}

class SpeciesEntry extends Entry {
    private String speciesName;
    private int count;

    public SpeciesEntry(String location, double latitude, double longitude, String date, String speciesName, int count) {
        super(location, latitude, longitude, date);
        this.speciesName = speciesName;
        this.count = count;
    }

    public String getSpeciesName() { return speciesName; }
    public int getCount() { return count; }

    @Override
    public String getType() { return "species"; }

    @Override
    public boolean isHighSeverity() { return false; }   // a sighting is never an emergency

    @Override
    public String getSummary() { return speciesName + ", count " + count; }
}

class CoralEntry extends Entry {
    public static final double HIGH_BLEACHING = 70;     // bleaching ABOVE this is high severity

    private double bleachingPercent;

    public CoralEntry(String location, double latitude, double longitude, String date, double bleachingPercent) {
        super(location, latitude, longitude, date);
        this.bleachingPercent = bleachingPercent;
    }

    public double getBleachingPercent() { return bleachingPercent; }

    @Override
    public String getType() { return "coral"; }

    @Override
    public boolean isHighSeverity() { return bleachingPercent > HIGH_BLEACHING; }

    @Override
    public String getSummary() { return "bleaching " + bleachingPercent + "%"; }
}

class DebrisEntry extends Entry {
    public static final double HAZARDOUS_CONCENTRATION = 50;   // this or more is high severity

    private String debrisType;
    private double concentration;   // items per 100 m2

    public DebrisEntry(String location, double latitude, double longitude, String date, String debrisType, double concentration) {
        super(location, latitude, longitude, date);
        this.debrisType = debrisType;
        this.concentration = concentration;
    }

    public String getDebrisType() { return debrisType; }
    public double getConcentration() { return concentration; }

    @Override
    public String getType() { return "debris"; }

    @Override
    public boolean isHighSeverity() { return concentration >= HAZARDOUS_CONCENTRATION; }

    @Override
    public String getSummary() { return debrisType + ", concentration " + concentration; }
}

class DataLogger {
    // All the entries logged so far (private: others use getAllEntries())
    private ArrayList<Entry> entries;

    public DataLogger() {
        entries = new ArrayList<Entry>();
    }

    // The log methods return "" if the entry was saved,
    // or the error message(s) if something is wrong.

    public String logSpeciesSighting(String species, String location, double latitude, double longitude, String date, int count) {
        Entry e = new SpeciesEntry(location.trim(), latitude, longitude, date.trim(), species.trim(), count);
        return saveEntry(e);
    }

    public String logCoralHealth(String location, double latitude, double longitude, String date, double bleachingPercent) {
        Entry e = new CoralEntry(location.trim(), latitude, longitude, date.trim(), bleachingPercent);
        return saveEntry(e);
    }

    public String logDebris(String location, double latitude, double longitude, String date, String debrisType, double concentration) {
        Entry e = new DebrisEntry(location.trim(), latitude, longitude, date.trim(), debrisType.trim(), concentration);
        return saveEntry(e);
    }

    // Checks an entry. Returns "" if it is valid, or the error message(s) if not.
    public String validateEntry(Entry e) {
        String errors = "";

        if (e.getLocation().equals("")) {
            errors += "- Location is required.\n";
        }

        if (Double.isNaN(e.getLatitude()) || e.getLatitude() < -90 || e.getLatitude() > 90) {
            errors += "Latitude must be between -90 and 90.\n";
        } else if (Double.isNaN(e.getLongitude()) || e.getLongitude() < -180 || e.getLongitude() > 180) {
            errors += "Longitude must be between -180 and 180.\n";
        }

        if (!isValidDate(e.getDate())) {
            errors += "- Date must be in YYYY-MM-DD format (example: 2026-11-06).\n";
        } else if (e.getDate().compareTo(java.time.LocalDate.now().toString()) > 0) {
            errors += "- Date cannot be in the future.\n";
        }

        if (e instanceof SpeciesEntry) {
            SpeciesEntry s = (SpeciesEntry) e;
            if (s.getSpeciesName().equals("")) {
                errors += "- Species name is required.\n";
            }
            if (s.getCount() < 1) {
                errors += "- Count must be a whole number of at least 1.\n";
            }
        } else if (e instanceof CoralEntry) {
            CoralEntry c = (CoralEntry) e;
            double percent = c.getBleachingPercent();
            if (Double.isNaN(percent) || percent < 0 || percent > 100) {
                errors += "- Bleaching percentage must be from 0 to 100.\n";
            }
        } else if (e instanceof DebrisEntry) {
            DebrisEntry d = (DebrisEntry) e;
            double amount = d.getConcentration();
            if (d.getDebrisType().equals("")) {
                errors += "- Debris type is required.\n";
            }
            if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < 0) {
                errors += "- Concentration must be 0 or higher.\n";
            }
        }

        return errors; 
    }

    public ArrayList<Entry> getAllEntries() {
        return new ArrayList<Entry>(entries);
    }

    // Validates the entry, then gives it an id and severity and saves it
    private String saveEntry(Entry e) {
        String errors = validateEntry(e);
        if (!errors.equals("")) {
            return errors;
        }

        e.setId(entries.size() + 1);
        if (e.isHighSeverity()) {
            e.setSeverity("high");
        }
        entries.add(e);
        return "";
    }

    // Checks that the date looks like YYYY-MM-DD
    private boolean isValidDate(String date) {
        if (date.length() != 10 || date.charAt(4) != '-' || date.charAt(7) != '-') {
            return false;
        }
        for (int i = 0; i < 10; i++) {
            if (i != 4 && i != 7 && !Character.isDigit(date.charAt(i))) {
                return false;
            }
        }
        int month = Integer.parseInt(date.substring(5, 7));
        int day = Integer.parseInt(date.substring(8, 10));
        return month >= 1 && month <= 12 && day >= 1 && day <= 31;
    }
}

class LogEntryScreen {
    private DataLogger logger;

    public LogEntryScreen(DataLogger logger) {
        this.logger = logger;
    }

    // Shows the screen once. Returns the entry that was saved,
    // or null if the user went back without saving.
    // (The main window can then pass the returned entry to Jann's alert methods.)

    public Entry show(Scanner input) {
        String choice = askEntryType(input);
        if (choice.equals("0")) {
            return null;
        }

        // Keep asking for the form until the entry is saved (or the user gives up)
        while (true) {
            String errors;
            if (choice.equals("1")) {
                errors = askSpecies(input);
            } else if (choice.equals("2")) {
                errors = askCoral(input);
            } else {
                errors = askDebris(input);
            }

            if (errors.equals("")) {
                break;
            }

            System.out.println("\nERROR: Entry not saved. Please fix:");
            System.out.print(errors);
            System.out.print("Try again? (y/n): ");
            if (!input.nextLine().trim().equalsIgnoreCase("y")) {
                return null;
            }
        }

        ArrayList<Entry> all = logger.getAllEntries();
        Entry saved = all.get(all.size() - 1);
        System.out.println("\nSUCCESS: Entry #" + saved.getId() + " saved.");
        showRecentEntries(all);
        return saved;
    }

    private String askEntryType(Scanner input) {
        while (true) {
            System.out.println();
            System.out.println("===== LOG ENTRY =====");
            System.out.println("1. Species sighting");
            System.out.println("2. Coral health");
            System.out.println("3. Coastal debris");
            System.out.println("0. Back to main menu");
            System.out.print("Choose entry type: ");
            String choice = input.nextLine().trim();

            if (choice.equals("0") || choice.equals("1") || choice.equals("2") || choice.equals("3")) {
                return choice;
            }
            System.out.println("Please type 1, 2, 3, or 0.");
        }
    }

    private String askSpecies(Scanner input) {
        System.out.print("Species name: ");
        String species = input.nextLine();

        System.out.print("City: "); 
        String location = input.nextLine();
            System.out.print("Latitude: ");
            double latitude = Double.parseDouble(input.nextLine().trim());
            System.out.print("Longitude: ");
            double longitude = Double.parseDouble(input.nextLine().trim());
        
        System.out.print("Date (YYYY-MM-DD): ");
        String date = input.nextLine();
        System.out.print("Count: ");
        String countText = input.nextLine();

        int count;
        try {
            count = Integer.parseInt(countText.trim());
        } catch (NumberFormatException ex) {
            count = -1;   // not allowed, so validateEntry will show the error message
        }

        return logger.logSpeciesSighting(species, location, latitude, longitude, date, count);
    }

    private String askCoral(Scanner input) {
        System.out.print("City: "); 
        String location = input.nextLine();
            System.out.print("Latitude: ");
            double latitude = Double.parseDouble(input.nextLine().trim());
            System.out.print("Longitude: ");
            double longitude = Double.parseDouble(input.nextLine().trim());

        System.out.print("Date (YYYY-MM-DD): ");
        String date = input.nextLine();
        System.out.println("Bleaching percent (HEALTHY / PALE / PARTIAL / SEVERE / OVERRIDE): ");
        String health = input.nextLine().trim();

    double percent;

    try {
        switch (health.toUpperCase()) {
            case "HEALTHY":
                percent = 5;
                break;

            case "PALE":
                percent = 25;
                break;

            case "PARTIAL":
                percent = 55;
                break;

            case "SEVERE":
                percent = 85;
                break;

            case "OVERRIDE":
                System.out.print("Bleaching percent: ");
                percent = Double.parseDouble(input.nextLine().trim());
                break;

            default:
                percent = -1;
                break;
        }

    } catch (NumberFormatException ex) {
        percent = -1;
    }

    return logger.logCoralHealth(location, latitude, longitude, date, percent);
}

    private String askDebris(Scanner input) {
        System.out.print("Debris type: ");
        String debrisType = input.nextLine();

        System.out.print("City: "); 
        String location = input.nextLine();
            System.out.print("Latitude: ");
            double latitude = Double.parseDouble(input.nextLine().trim());
            System.out.print("Longitude: ");
            double longitude = Double.parseDouble(input.nextLine().trim());

        System.out.print("Date (YYYY-MM-DD): ");
        String date = input.nextLine();
        System.out.print("Concentration (items per 100 m2): ");
        String concentrationText = input.nextLine();

        double concentration;
        try {
            concentration = Double.parseDouble(concentrationText.trim());
        } catch (NumberFormatException ex) {
            concentration = -1;
        }

        return logger.logDebris(location, latitude, longitude, date, debrisType, concentration);
    }

    private void showRecentEntries(ArrayList<Entry> all) {
        System.out.println("\nRecently logged entries:");
        System.out.printf("%-4s %-8s %-20s %-11s %-30s %-8s%n",
                "ID", "Type", "Location", "Date", "Details", "Severity");

        int shown = 0;
        for (int i = all.size() - 1; i >= 0 && shown < 10; i--) {
            Entry e = all.get(i);
            System.out.printf("%-4d %-8s %-20s %-11s %-30s %-8s%n",
                    e.getId(), e.getType(), e.getLocation(), e.getDate(),
                    e.getSummary(), e.getSeverity());
            shown++;
        }
    }
}

// ===================================================================
// FOR TESTING MY PART ONLY (Kylie's MainApp replaces this later)
// ===================================================================
public class LogEntryApp {
    public static void main(String[] args) {
    Scanner input = new Scanner(System.in);
    DataLogger logger = new DataLogger();
    LogEntryScreen screen = new LogEntryScreen(logger);
    AlertScreen alerts = new AlertScreen();

    alerts.showWeatherWarnings(0, 0);

    boolean running = true;
    while (running) {
        Entry saved = screen.show(input);

        if (saved != null) {
            alerts.triggerAlert(saved);

            alerts.checkWeatherWarnings(
                saved.getLatitude(),
                saved.getLongitude()
            );
        }
        
        System.out.print("\nOpen the Log Entry screen again? (y/n): ");
        running = input.nextLine().trim().equalsIgnoreCase("y");
    }
}
}