<!-- In Your pom.xml paste this dependency -->

<dependency>
    <groupId>com.jari</groupId>
    <artifactId>nmea-parser</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>


<!-- Example Code -->

import com.jari.NMEAParser;
import com.jari.NMEAData;

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


<!-- Parameters / Fields Used (. operator) - NmeaData -->

| **Parameter / Field**                                                                | **Belongs To** | **Purpose / Description (Short)**                     |
| ------------------------------------------------------------------------------------ | -------------- | ----------------------------------------------------- |
| `data`                                                                               | `NMEAParser`   | Holds parsed NMEA key-value data.                     |
| `satellites`                                                                         | `NMEAParser`   | List of satellites extracted from GPGSV sentences.    |
| `validateChecksum`                                                                   | `NMEAParser`   | Enables/disables checksum validation.                 |
| `gpgsvMessageNumbers`                                                                | `NMEAParser`   | Tracks message sequence numbers for GPGSV.            |
| `type`                                                                               | `NMEAData`     | Sentence type (e.g., GPGGA, GPRMC).                   |
| `time`                                                                               | `NMEAData`     | UTC time extracted from sentence.                     |
| `date`                                                                               | `NMEAData`     | Date in DDMMYY or similar format.                     |
| `latitude`                                                                           | `NMEAData`     | Raw latitude value.                                   |
| `longitude`                                                                          | `NMEAData`     | Raw longitude value.                                  |
| `formattedLatitude`                                                                  | `NMEAData`     | Decimal degrees latitude.                             |
| `formattedLongitude`                                                                 | `NMEAData`     | Decimal degrees longitude.                            |
| `altitude`                                                                           | `NMEAData`     | Altitude above mean sea level.                        |
| `altitudeUnits`                                                                      | `NMEAData`     | Unit of altitude (usually ‘M’ for meters).            |
| `speedKnots`                                                                         | `NMEAData`     | Speed in knots.                                       |
| `speedKmh`                                                                           | `NMEAData`     | Speed in kilometers per hour.                         |
| `hdop`                                                                               | `NMEAData`     | Horizontal dilution of precision.                     |
| `vdop`                                                                               | `NMEAData`     | Vertical dilution of precision.                       |
| `pdop`                                                                               | `NMEAData`     | Position dilution of precision.                       |
| `mode`                                                                               | `NMEAData`     | GPS mode (manual/auto).                               |
| `fixType`                                                                            | `NMEAData`     | Fix type (2D/3D fix).                                 |
| `satellitesUsed`                                                                     | `NMEAData`     | Number of satellites used for fix.                    |
| `satellitesInView`                                                                   | `NMEAData`     | Number of satellites visible.                         |
| `message`                                                                            | `NMEAData`     | Optional or unsupported message type.                 |
| `trackAngle`                                                                         | `NMEAData`     | Track angle from RMC sentence.                        |
| `status`                                                                             | `NMEAData`     | Status (A = active, V = void).                        |
| `trackTrue`                                                                          | `NMEAData`     | True heading (degrees).                               |
| `trackMagnetic`                                                                      | `NMEAData`     | Magnetic heading (degrees).                           |
| `day`, `month`, `year`                                                               | `NMEAData`     | Date components (from ZDA).                           |
| `rms`, `sigmaMajor`, `sigmaMinor`, `orientation`, `sigmaLat`, `sigmaLon`, `sigmaAlt` | `NMEAData`     | Error and deviation statistics (from GST).            |
| `satelliteDetails`                                                                   | `NMEAData`     | List of satellite info (id, elevation, azimuth, snr). |


<!-- User-Accessible Functions - NmeaParser -->

| **Function Name**                               | **Type** | **Purpose / Usage (Short)**                                                 |
| ----------------------------------------------- | -------- | --------------------------------------------------------------------------- |
| `of(String sentence)`                           | `static` | Creates parser instance with checksum validation enabled.                   |
| `of(String sentence, boolean validateChecksum)` | `static` | Creates parser instance with optional checksum validation.                  |
| `isValidChecksum(String sentence)`              | `static` | Validates the NMEA sentence checksum.                                       |
| `getType()`                                     | instance | Returns sentence type (e.g., GPGGA, GPRMC).                                 |
| `getConnectedSatellites()`                      | instance | Returns number of satellites currently parsed. same as data.satellitesUsed  |
| `getSatelliteDetails()`                         | instance | Returns detailed list of satellites (ID, elevation, azimuth, SNR).          |
| `getFormattedLatitude()`                        | instance | Returns decimal-formatted latitude.                                         |
| `getFormattedLongitude()`                       | instance | Returns decimal-formatted longitude.                                        |
| `toData()`                                      | instance | Converts internal map into `NMEAData` object (DTO) for easy access.         |
| `summary()`                                     | instance | Returns a readable string summary of the parsed sentence.                   |
