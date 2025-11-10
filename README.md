# NMEA Parser Library (Java)

A simple and powerful Java library to parse NMEA sentences (`GPGGA`, `GPRMC`, `GPVTG`, `GPGSA`, `GPGSV`, `GPGLL`, `GPZDA`, `GPGST`) into easily usable objects and summaries. Works with **Core Java**, **Spring Boot**, **Quarkus**, or any Java project.

---

## 📦 Installation

### Maven Dependency

Add this dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.jari</groupId>
    <artifactId>nmea-parser</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
---

## 💻 Example Usage

```java
import com.jari.NMEAParser;
import com.jari.NMEAData;
import com.jari.ChecksumException;
import java.util.Map;

public class NmeaExample {
    public static void main(String[] args) {
        String[] testSentences = {
            "$GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*47",
            "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A",
            "$GPVTG,054.7,T,034.4,M,005.5,N,010.2,K*48"
            // Add more sentences as needed
        };

        for (String sentence : testSentences) {
            try {
                NMEAParser parser = NMEAParser.of(sentence, false);

                System.out.println("=== SUMMARY ===");
                System.out.println(parser.summary());

                NMEAData data = parser.toData();
                System.out.println("Latitude: " + data.formattedLatitude);
                System.out.println("Longitude: " + data.formattedLongitude);
                System.out.println("Satellites Used: " + data.satellitesUsed);
                System.out.println("Satellites In View: " + data.satellitesInView);

                if (data.satelliteDetails != null && !data.satelliteDetails.isEmpty()) {
                    System.out.println("Satellite Details:");
                    for (Map<String, String> sat : data.satelliteDetails) {
                        System.out.println("  ID: " + sat.get("id") +
                                           ", Elevation: " + sat.get("elevation") +
                                           ", Azimuth: " + sat.get("azimuth") +
                                           ", SNR: " + sat.get("snr"));
                    }
                }

                System.out.println("--------------------------------------");

            } catch (ChecksumException e) {
                System.err.println("Checksum failed for sentence: " + sentence);
            }
        }
    }
}
```

---

## 🔑 Parameters / Fields (`NMEAData`)

| **Field**                                                                | **Belongs To** | **Description**                                  |
| ------------------------------------------------------------------------ | -------------- | ------------------------------------------------ |
| `type`                                                                   | `NMEAData`     | Sentence type (GPGGA, GPRMC, etc.)               |
| `time`                                                                   | `NMEAData`     | UTC time                                         |
| `date`                                                                   | `NMEAData`     | Date (DDMMYY)                                    |
| `latitude`                                                               | `NMEAData`     | Raw latitude                                     |
| `longitude`                                                              | `NMEAData`     | Raw longitude                                    |
| `formattedLatitude`                                                      | `NMEAData`     | Decimal degrees latitude                         |
| `formattedLongitude`                                                     | `NMEAData`     | Decimal degrees longitude                        |
| `altitude`                                                               | `NMEAData`     | Altitude above sea level                         |
| `altitudeUnits`                                                          | `NMEAData`     | Unit of altitude (usually meters)                |
| `speedKnots`                                                             | `NMEAData`     | Speed in knots                                   |
| `speedKmh`                                                               | `NMEAData`     | Speed in km/h                                    |
| `hdop`                                                                   | `NMEAData`     | Horizontal dilution of precision                 |
| `vdop`                                                                   | `NMEAData`     | Vertical dilution of precision                   |
| `pdop`                                                                   | `NMEAData`     | Position dilution of precision                   |
| `mode`                                                                   | `NMEAData`     | GPS mode                                         |
| `fixType`                                                                | `NMEAData`     | Fix type (2D/3D)                                 |
| `satellitesUsed`                                                         | `NMEAData`     | Satellites used for fix                          |
| `satellitesInView`                                                       | `NMEAData`     | Visible satellites                               |
| `trackAngle`                                                             | `NMEAData`     | Track angle                                      |
| `status`                                                                 | `NMEAData`     | Status (A = active, V = void)                    |
| `trackTrue`                                                              | `NMEAData`     | True heading                                     |
| `trackMagnetic`                                                          | `NMEAData`     | Magnetic heading                                 |
| `day, month, year`                                                       | `NMEAData`     | Date components (from ZDA)                       |
| `rms, sigmaMajor, sigmaMinor, orientation, sigmaLat, sigmaLon, sigmaAlt` | `NMEAData`     | Error/deviation statistics (GST)                 |
| `satelliteDetails`                                                       | `NMEAData`     | List of satellites (id, elevation, azimuth, snr) |
| `message`                                                                | `NMEAData`     | Optional / unsupported messages                  |

---

## ⚡ User-Accessible Functions (`NMEAParser`)

| **Function**                                    | **Type** | **Description**                                          |
| ----------------------------------------------- | -------- | -------------------------------------------------------- |
| `of(String sentence)`                           | static   | Creates parser instance with checksum validation enabled |
| `of(String sentence, boolean validateChecksum)` | static   | Creates parser with optional checksum validation         |
| `isValidChecksum(String sentence)`              | static   | Validates NMEA sentence checksum                         |
| `getType()`                                     | instance | Returns sentence type                                    |
| `getConnectedSatellites()`                      | instance | Returns number of satellites parsed                      |
| `getSatelliteDetails()`                         | instance | Returns detailed satellite list                          |
| `getFormattedLatitude()`                        | instance | Returns decimal latitude                                 |
| `getFormattedLongitude()`                       | instance | Returns decimal longitude                                |
| `toData()`                                      | instance | Converts parsed data into `NMEAData` object              |
| `summary()`                                     | instance | Returns human-readable summary                           |

---

✅ Now users can **add the dependency, import the library**, and start parsing NMEA sentences in any Java project, including **Spring Boot**, **Quarkus**, or **plain Java**.

---
