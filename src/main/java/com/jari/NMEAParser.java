package com.jari;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

// ===== Custom Exception =====
class ChecksumException extends RuntimeException {
    public ChecksumException(String message) {
        super(message);
    }
}

// ===== DTO for clean data export =====
class NMEAData {
    public String type;
    public String time;
    public String date;
    public String latitude;
    public String longitude;
    public String formattedLatitude;
    public String formattedLongitude;
    public String altitude;
    public String altitudeUnits;
    public String speedKnots;
    public String speedKmh;
    public String hdop;
    public String vdop;
    public String pdop;
    public String mode;
    public String fixType;
    public int satellitesUsed;
    public int satellitesInView;
    public String message;
    public String trackAngle;
    public String status;
    public String trackTrue;
    public String trackMagnetic;
    public String day;
    public String month;
    public String year;
    public String rms;
    public String sigmaMajor;
    public String sigmaMinor;
    public String orientation;
    public String sigmaLat;
    public String sigmaLon;
    public String sigmaAlt;
    public List<Map<String, String>> satelliteDetails;
}

public class NMEAParser {

    private static final Logger LOG = Logger.getLogger(NMEAParser.class.getName());

    private final Map<String, String> data = Collections.synchronizedMap(new HashMap<>());
    private final List<Map<String, String>> satellites = Collections.synchronizedList(new ArrayList<>());
    private final boolean validateChecksum;

    private final List<Integer> gpgsvMessageNumbers = Collections.synchronizedList(new ArrayList<>());

    // ======= CONSTRUCTORS =======
    public static NMEAParser of(String nmeaSentence) {
        return new NMEAParser(nmeaSentence, true);
    }

    public static NMEAParser of(String nmeaSentence, boolean validateChecksum) {
        return new NMEAParser(nmeaSentence, validateChecksum);
    }

    private NMEAParser(String nmeaSentence, boolean validateChecksum) {
        this.validateChecksum = validateChecksum;
        if (nmeaSentence != null && !nmeaSentence.isBlank()) parse(nmeaSentence);
    }

    // ======= PARSING =======
    private void parse(String nmeaSentence) {
        if (nmeaSentence == null || !nmeaSentence.startsWith("$"))
            throw new IllegalArgumentException("Invalid NMEA sentence: " + nmeaSentence);

        if (validateChecksum && !isValidChecksum(nmeaSentence))
            throw new ChecksumException("Checksum validation failed for: " + nmeaSentence);

        String type = nmeaSentence.substring(1, 6);
        String[] p = nmeaSentence.split(",");

        data.put("type", type);

        switch (type) {
            case "GPGGA": parseGPGGA(p); break;
            case "GPRMC": parseGPRMC(p); break;
            case "GPVTG": parseGPVTG(p); break;
            case "GPGSA": parseGPGSA(p); break;
            case "GPGSV": parseGPGSV(p); break;
            case "GPGLL": parseGPGLL(p); break;
            case "GPZDA": parseGPZDA(p); break;
            case "GPGST": parseGPGST(p); break;
            default: data.put("message", "Unsupported sentence type: " + type); break;
        }
    }

    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    // ======= SENTENCE PARSERS =======
    private void parseGPGGA(String[] p) {
        data.put("time", get(p, 1));
        data.put("latitude", get(p, 2));
        data.put("lat_dir", get(p, 3));
        data.put("longitude", get(p, 4));
        data.put("lon_dir", get(p, 5));
        data.put("fix_quality", get(p, 6));
        data.put("satellites", get(p, 7));
        data.put("hdop", get(p, 8));
        data.put("altitude", get(p, 9));
        data.put("altitude_units", get(p, 10));
    }

    private void parseGPRMC(String[] p) {
        data.put("time", get(p, 1));
        data.put("status", get(p, 2));
        data.put("latitude", get(p, 3));
        data.put("lat_dir", get(p, 4));
        data.put("longitude", get(p, 5));
        data.put("lon_dir", get(p, 6));
        data.put("speed_knots", get(p, 7));
        data.put("track_angle", get(p, 8));
        data.put("date", get(p, 9));
    }

    private void parseGPVTG(String[] p) {
        data.put("track_true", get(p, 1));
        data.put("track_magnetic", get(p, 3));
        data.put("speed_knots", get(p, 5));
        data.put("speed_kmh", get(p, 7));
    }

    private void parseGPGSA(String[] p) {
        data.put("mode", get(p, 1));
        data.put("fix_type", get(p, 2));
        StringBuilder used = new StringBuilder();
        for (int i = 3; i <= 14; i++) if (!get(p, i).isEmpty()) used.append(get(p, i)).append(",");
        data.put("satellites_used", used.toString().replaceAll(",$", ""));
        data.put("pdop", get(p, 15));
        data.put("hdop", get(p, 16));
        data.put("vdop", get(p, 17));
    }

    private void parseGPGSV(String[] p) {
        int totalMessages = parseIntSafe(get(p, 1), 1);
        int messageNumber = parseIntSafe(get(p, 2), 1);
        int satellitesInView = parseIntSafe(get(p, 3), 0);

        data.put("total_messages", String.valueOf(totalMessages));
        data.put("message_number", String.valueOf(messageNumber));
        data.put("satellites_in_view", String.valueOf(satellitesInView));

        if (messageNumber == 1) satellites.clear();

        for (int i = 4; i + 3 < p.length; i += 4) {
            Map<String, String> sat = new HashMap<>();
            sat.put("id", get(p, i));
            sat.put("elevation", get(p, i + 1));
            sat.put("azimuth", get(p, i + 2));
            sat.put("snr", cleanup(get(p, i + 3)));
            satellites.add(sat);
            gpgsvMessageNumbers.add(messageNumber);
        }
    }

    private void parseGPGLL(String[] p) {
        data.put("latitude", get(p, 1));
        data.put("lat_dir", get(p, 2));
        data.put("longitude", get(p, 3));
        data.put("lon_dir", get(p, 4));
        data.put("time", get(p, 5));
        data.put("status", cleanup(get(p, 6)));
    }

    private void parseGPZDA(String[] p) {
        data.put("time", get(p, 1));
        data.put("day", get(p, 2));
        data.put("month", get(p, 3));
        data.put("year", get(p, 4));
    }

    private void parseGPGST(String[] p) {
        data.put("time", get(p, 1));
        data.put("rms", get(p, 2));
        data.put("sigma_major", get(p, 3));
        data.put("sigma_minor", get(p, 4));
        data.put("orientation", get(p, 5));
        data.put("sigma_lat", get(p, 6));
        data.put("sigma_lon", get(p, 7));
        data.put("sigma_alt", cleanup(get(p, 8)));
    }

    // ======= HELPERS =======
    private String get(String[] arr, int i) {
        return (i < arr.length) ? arr[i] : "";
    }

    private String cleanup(String s) {
        return (s != null) ? s.replace("*", "") : "";
    }

    public static boolean isValidChecksum(String sentence) {
        int asterisk = sentence.indexOf('*');
        if (asterisk < 0) return true;
        String payload = sentence.substring(1, asterisk);
        String checksumStr = sentence.substring(asterisk + 1).trim();
        if (checksumStr.length() > 2) checksumStr = checksumStr.substring(0, 2);
        int cs = 0;
        for (char c : payload.toCharArray()) cs ^= c;
        return String.format("%02X", cs).equalsIgnoreCase(checksumStr);
    }

    private String convertToDecimal(String value, String direction) {
        if (value == null || value.isEmpty()) return "";
        try {
            int dot = value.indexOf('.');
            int degLen = (dot > 3) ? 3 : 2;
            String degStr = value.substring(0, Math.min(degLen, value.length()));
            String minStr = value.substring(Math.min(degLen, value.length()));
            double deg = Double.parseDouble(degStr);
            double min = minStr.isEmpty() ? 0.0 : Double.parseDouble(minStr);
            double dec = deg + (min / 60.0);
            if ("S".equalsIgnoreCase(direction) || "W".equalsIgnoreCase(direction)) dec = -dec;
            return String.format("%.6f", dec);
        } catch (Exception e) {
            return value;
        }
    }

    // ======= ACCESSORS =======
    public String getType() { return data.getOrDefault("type", ""); }
    public int getConnectedSatellites() { return satellites.size(); }
    public List<Map<String, String>> getSatelliteDetails() { return Collections.unmodifiableList(satellites); }
    public String getFormattedLatitude() { return convertToDecimal(data.get("latitude"), data.get("lat_dir")); }
    public String getFormattedLongitude() { return convertToDecimal(data.get("longitude"), data.get("lon_dir")); }

    // ======= EXPORT TO DTO =======
    public NMEAData toData() {
        NMEAData d = new NMEAData();
        d.type = getType();
        d.time = data.get("time");
        d.date = data.get("date");
        d.latitude = data.get("latitude");
        d.longitude = data.get("longitude");
        d.formattedLatitude = getFormattedLatitude();
        d.formattedLongitude = getFormattedLongitude();
        d.altitude = data.get("altitude");
        d.altitudeUnits = data.get("altitude_units");
        d.speedKnots = data.get("speed_knots");
        d.speedKmh = data.get("speed_kmh");
        d.hdop = data.get("hdop");
        d.vdop = data.get("vdop");
        d.pdop = data.get("pdop");
        d.mode = data.get("mode");
        d.fixType = data.get("fix_type");

        // GPRMC
        d.trackAngle = data.get("track_angle");
        d.status = data.get("status");

        // GPVTG
        d.trackTrue = data.get("track_true");
        d.trackMagnetic = data.get("track_magnetic");

        // GPZDA
        d.day = data.get("day");
        d.month = data.get("month");
        d.year = data.get("year");

        // GPGST
        d.rms = data.get("rms");
        d.sigmaMajor = data.get("sigma_major");
        d.sigmaMinor = data.get("sigma_minor");
        d.orientation = data.get("orientation");
        d.sigmaLat = data.get("sigma_lat");
        d.sigmaLon = data.get("sigma_lon");
        d.sigmaAlt = data.get("sigma_alt");

        // Satellites
        d.satellitesUsed = getConnectedSatellites(); // or parse from satellites_used
        d.satellitesInView = parseIntSafe(data.getOrDefault("satellites_in_view", "0"), 0);
        d.satelliteDetails = getSatelliteDetails();

        // Optional message
        d.message = data.get("message");

        return d;
    }


    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Sentence Type: ").append(getType()).append("\n");

        switch (getType()) {
            case "GPGGA":
                sb.append("Time: ").append(data.getOrDefault("time", "")).append("\n");
                sb.append("Latitude: ").append(data.getOrDefault("latitude", "")).append(" (").append(data.getOrDefault("lat_dir", "")).append(")\n");
                sb.append("Longitude: ").append(data.getOrDefault("longitude", "")).append(" (").append(data.getOrDefault("lon_dir", "")).append(")\n");
                sb.append("Fix Quality: ").append(data.getOrDefault("fix_quality", "")).append("\n");
                sb.append("Satellites: ").append(data.getOrDefault("satellites", "")).append("\n");
                sb.append("HDOP: ").append(data.getOrDefault("hdop", "")).append("\n");
                sb.append("Altitude: ").append(data.getOrDefault("altitude", "")).append(" ").append(data.getOrDefault("altitude_units", "")).append("\n");
                break;

            case "GPRMC":
                sb.append("Time: ").append(data.getOrDefault("time", "")).append("\n");
                sb.append("Date: ").append(data.getOrDefault("date", "")).append("\n");
                sb.append("Latitude: ").append(data.getOrDefault("latitude", "")).append(" (").append(data.getOrDefault("lat_dir", "")).append(")\n");
                sb.append("Longitude: ").append(data.getOrDefault("longitude", "")).append(" (").append(data.getOrDefault("lon_dir", "")).append(")\n");
                sb.append("Speed (knots): ").append(data.getOrDefault("speed_knots", "")).append("\n");
                sb.append("Track Angle: ").append(data.getOrDefault("track_angle", "")).append("\n");
                sb.append("Status: ").append(data.getOrDefault("status", "")).append("\n");
                break;

            case "GPVTG":
                sb.append("Track (True): ").append(data.getOrDefault("track_true", "")).append("\n");
                sb.append("Track (Magnetic): ").append(data.getOrDefault("track_magnetic", "")).append("\n");
                sb.append("Speed: ").append(data.getOrDefault("speed_knots", "")).append(" knots / ").append(data.getOrDefault("speed_kmh", "")).append(" km/h\n");
                break;

            case "GPGSA":
                sb.append("Mode: ").append(data.getOrDefault("mode", "")).append("\n");
                sb.append("Fix Type: ").append(data.getOrDefault("fix_type", "")).append("\n");
                sb.append("Connected Satellites: ").append(data.getOrDefault("satellites_used", "")).append("\n");
                sb.append("PDOP: ").append(data.getOrDefault("pdop", "")).append("\n");
                sb.append("HDOP: ").append(data.getOrDefault("hdop", "")).append("\n");
                sb.append("VDOP: ").append(data.getOrDefault("vdop", "")).append("\n");
                break;

            case "GPGSV":
                sb.append("Total Messages: ").append(data.getOrDefault("total_messages", "")).append("\n");
                sb.append("Message Number: ").append(data.getOrDefault("message_number", "")).append("\n");
                sb.append("Satellites in View: ").append(data.getOrDefault("satellites_in_view", "")).append("\n");
                if (!satellites.isEmpty()) {
                    sb.append("Satellite Details:\n");
                    for (Map<String, String> sat : satellites) {
                        sb.append("  ID: ").append(sat.get("id"))
                        .append(", Elevation: ").append(sat.get("elevation"))
                        .append(", Azimuth: ").append(sat.get("azimuth"))
                        .append(", SNR: ").append(sat.get("snr"))
                        .append("\n");
                    }
                }
                break;

            case "GPGLL":
                sb.append("Latitude: ").append(data.getOrDefault("latitude", "")).append(" (").append(data.getOrDefault("lat_dir", "")).append(")\n");
                sb.append("Longitude: ").append(data.getOrDefault("longitude", "")).append(" (").append(data.getOrDefault("lon_dir", "")).append(")\n");
                sb.append("Time: ").append(data.getOrDefault("time", "")).append("\n");
                sb.append("Status: ").append(data.getOrDefault("status", "")).append("\n");
                break;

            case "GPZDA":
                sb.append("Time: ").append(data.getOrDefault("time", "")).append("\n");
                sb.append("Date: ").append(data.getOrDefault("day", "")).append("/")
                .append(data.getOrDefault("month", "")).append("/")
                .append(data.getOrDefault("year", "")).append("\n");
                break;

            case "GPGST":
                sb.append("Time: ").append(data.getOrDefault("time", "")).append("\n");
                sb.append("RMS: ").append(data.getOrDefault("rms", "")).append("\n");
                sb.append("Sigma Major: ").append(data.getOrDefault("sigma_major", "")).append("\n");
                sb.append("Sigma Minor: ").append(data.getOrDefault("sigma_minor", "")).append("\n");
                sb.append("Orientation: ").append(data.getOrDefault("orientation", "")).append("\n");
                sb.append("Sigma Latitude: ").append(data.getOrDefault("sigma_lat", "")).append("\n");
                sb.append("Sigma Longitude: ").append(data.getOrDefault("sigma_lon", "")).append("\n");
                sb.append("Sigma Altitude: ").append(data.getOrDefault("sigma_alt", "")).append("\n");
                break;

            default:
                sb.append("Unsupported sentence type.\n");
                break;
        }

        return sb.toString();
    }


    // ======= MAIN TEST =======
    // ======= MAIN TEST =======
    public static void main(String[] args) {
        String[] testSentences = {
            "$GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*47",
            "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A",
            "$GPVTG,054.7,T,034.4,M,005.5,N,010.2,K*48",
            "$GPGSA,A,3,04,05,09,12,24,25,29,,,,,1.8,1.0,1.5*33",
            "$GPGSV,2,1,08,01,40,083,41,02,17,063,42,03,13,053,43,04,03,013,42*70",
            "$GPGSV,2,2,08,05,20,123,44,06,05,223,45,07,11,323,43,08,32,073,42*71",
            "$GPGLL,4916.45,N,12311.12,W,225444,A*1D",
            "$GPZDA,201530.00,04,07,2002,00,00*60",
            "$GPGST,024603.00,1.2,0.8,1.0,45.0,0.5,0.6,0.7*5A"
        };

        for (String sentence : testSentences) {
            try {
                // Parse sentence
                NMEAParser parser = NMEAParser.of(sentence, false);

                // Print summary
                System.out.println("=== SUMMARY ===");
                System.out.println(parser.summary());

                // Convert to NMEAData and print fields
                NMEAData data = parser.toData();
                System.out.println("=== TO DATA ===");
                System.out.println("Type: " + data.type);
                System.out.println("Time: " + data.time);
                System.out.println("Date: " + data.date);
                System.out.println("Latitude: " + data.formattedLatitude);
                System.out.println("Longitude: " + data.formattedLongitude);
                System.out.println("Altitude: " + data.altitude + " " + data.altitudeUnits);
                System.out.println("Speed (knots): " + data.speedKnots);
                System.out.println("Speed (km/h): " + data.speedKmh);
                System.out.println("HDOP: " + data.hdop);
                System.out.println("VDOP: " + data.vdop);
                System.out.println("PDOP: " + data.pdop);
                System.out.println("Mode: " + data.mode);
                System.out.println("Fix Type: " + data.fixType);
                System.out.println("Satellites Used: " + data.satellitesUsed);
                System.out.println("Satellites Used Func: " + parser.getConnectedSatellites());
                System.out.println("Satellites In View: " + data.satellitesInView);
                System.out.println("Track Angle: " + data.trackAngle);
                System.out.println("Status: " + data.status);
                System.out.println("Track True: " + data.trackTrue);
                System.out.println("Track Magnetic: " + data.trackMagnetic);
                System.out.println("Day: " + data.day);
                System.out.println("Month: " + data.month);
                System.out.println("Year: " + data.year);
                System.out.println("RMS: " + data.rms);
                System.out.println("Sigma Major: " + data.sigmaMajor);
                System.out.println("Sigma Minor: " + data.sigmaMinor);
                System.out.println("Orientation: " + data.orientation);
                System.out.println("Sigma Lat: " + data.sigmaLat);
                System.out.println("Sigma Lon: " + data.sigmaLon);
                System.out.println("Sigma Alt: " + data.sigmaAlt);

                if (data.satelliteDetails != null && !data.satelliteDetails.isEmpty()) {
                    System.out.println("Satellite Details:");
                    for (Map<String, String> sat : data.satelliteDetails) {
                        System.out.println("  ID: " + sat.get("id") +
                                        ", Elevation: " + sat.get("elevation") +
                                        ", Azimuth: " + sat.get("azimuth") +
                                        ", SNR: " + sat.get("snr"));
                    }
                }

                if (data.message != null) {
                    System.out.println("Message: " + data.message);
                }

                System.out.println("----------------------------------------------------\n");

            } catch (ChecksumException e) {
                System.err.println("Checksum failed for sentence: " + sentence);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
 
}