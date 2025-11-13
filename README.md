````markdown
# NMEA Parser Library (Java)

A simple and powerful Java library to parse NMEA sentences (`GPGGA`, `GPRMC`, `GPVTG`, `GPGSA`, `GPGSV`, `GPGLL`, `GPZDA`, `GPGST`) into easily usable objects and summaries. Works with **Core Java**, **Spring Boot**, **Quarkus**, or any Java project.

---

## 📦 Installation

### 1️⃣ Maven Dependency (GitHub Packages)

Add this dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.jari</groupId>
    <artifactId>nmea-parser</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
````

Since this package is not on Maven Central, you must also add the GitHub repository:

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/YOUR_USERNAME/YOUR_REPO</url>
    </repository>
</repositories>
```

> ⚠️ **Important:** You need a GitHub personal access token with `read:packages` permission to download this package.

---

### 2️⃣ Local Installation

1. Clone the repository:

```bash
git clone https://github.com/YOUR_USERNAME/nmea-parser.git
cd nmea-parser
```

2. Build and install locally:

```bash
mvn clean install
```

3. Now the library is in your local Maven repository. Add the dependency in any project:

```xml
<dependency>
    <groupId>com.jari</groupId>
    <artifactId>nmea-parser</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

---

### 3️⃣ Manual JAR Usage

1. Build the JAR:

```bash
mvn clean package
```

2. Add `nmea-parser-1.0-SNAPSHOT.jar` to your project’s `libs` folder.
3. Add it to your IDE’s classpath.

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
        };

        for (String sentence : testSentences) {
            try {
                NMEAParser parser = NMEAParser.of(sentence, false);
                System.out.println("=== SUMMARY ===");
                System.out.println(parser.summary());
                NMEAData data = parser.toData();
                System.out.println("Latitude: " + data.formattedLatitude);
                System.out.println("Longitude: " + data.formattedLongitude);
            } catch (ChecksumException e) {
                System.err.println("Checksum failed for sentence: " + sentence);
            }
        }
    }
}
```

---

## 🌱 Using in Spring Boot & Quarkus

### Spring Boot Example

**Service Layer:**

```java
import org.springframework.stereotype.Service;
import com.jari.NMEAParser;
import com.jari.NMEAData;
import com.jari.ChecksumException;

@Service
public class NmeaService {
    public NMEAData parseSentence(String sentence) throws ChecksumException {
        return NMEAParser.of(sentence, true).toData();
    }
}
```

**Controller Layer:**

```java
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nmea")
public class NmeaController {

    private final NmeaService nmeaService;

    public NmeaController(NmeaService nmeaService) {
        this.nmeaService = nmeaService;
    }

    @GetMapping("/parse")
    public NMEAData parse(@RequestParam String sentence) throws Exception {
        return nmeaService.parseSentence(sentence);
    }
}
```

---

### Quarkus Example

**Application Scoped Bean:**

```java
import jakarta.enterprise.context.ApplicationScoped;
import com.jari.NMEAParser;
import com.jari.NMEAData;
import com.jari.ChecksumException;

@ApplicationScoped
public class NmeaBean {
    public NMEAData parse(String sentence) throws ChecksumException {
        return NMEAParser.of(sentence, true).toData();
    }
}
```

**REST Resource:**

```java
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/nmea")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NmeaResource {

    @Inject
    NmeaBean nmeaBean;

    @GET
    @Path("/parse")
    public NMEAData parse(@QueryParam("sentence") String sentence) throws Exception {
        return nmeaBean.parse(sentence);
    }
}
```

---

## 🔑 Parameters / Fields (`NMEAData`)

| **Field**            | **Description**                                  |
| -------------------- | ------------------------------------------------ |
| `type`               | Sentence type (GPGGA, GPRMC, etc.)               |
| `time`               | UTC time                                         |
| `date`               | Date (DDMMYY)                                    |
| `latitude`           | Raw latitude                                     |
| `longitude`          | Raw longitude                                    |
| `formattedLatitude`  | Decimal degrees latitude                         |
| `formattedLongitude` | Decimal degrees longitude                        |
| `altitude`           | Altitude above sea level                         |
| `speedKnots`         | Speed in knots                                   |
| `speedKmh`           | Speed in km/h                                    |
| `satellitesUsed`     | Satellites used for fix                          |
| `satellitesInView`   | Visible satellites                               |
| `satelliteDetails`   | List of satellites (id, elevation, azimuth, snr) |
| `message`            | Optional / unsupported messages                  |

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

### Notes

* Works with **any Java project**: plain Java, Spring Boot, Quarkus.
* Checksum validation can be enabled or disabled.
* GitHub Packages requires authentication.
* Local install via `mvn install` works for testing or internal usage.

```

Chahiye banau?
```
