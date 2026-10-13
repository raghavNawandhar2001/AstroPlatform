package com.astroplatform.service;

import com.astroplatform.dto.LocationData;
import com.astroplatform.dto.PanchangRequest;
import com.astroplatform.dto.PanchangResponse;
import com.astroplatform.dto.PlanetPosition;
import com.astroplatform.helper.HouseCalculation;
import com.astroplatform.helper.ZodiacConverter;
import de.thmac.swisseph.SweConst;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@AllArgsConstructor
public class PanchangService {

    private final EphemerisService ephemerisService;
    private final GeocodingService geocodingService;

    private static final String[] TITHIS = {
            "Shukla Pratipada", "Shukla Dwitiya", "Shukla Tritiya", "Shukla Chaturthi", "Shukla Panchami",
            "Shukla Shashti", "Shukla Saptami", "Shukla Ashtami", "Shukla Navami", "Shukla Dashami",
            "Shukla Ekadashi", "Shukla Dwadashi", "Shukla Trayodashi", "Shukla Chaturdashi", "Purnima",
            "Krishna Pratipada", "Krishna Dwitiya", "Krishna Tritiya", "Krishna Chaturthi", "Krishna Panchami",
            "Krishna Shashti", "Krishna Saptami", "Krishna Ashtami", "Krishna Navami", "Krishna Dashami",
            "Krishna Ekadashi", "Krishna Dwadashi", "Krishna Trayodashi", "Krishna Chaturdashi", "Amavasya"
    };

    private static final String[] YOGAS = {
            "Vishkambha", "Priti", "Ayushman", "Saubhagya", "Shobhana", "Atiganda", "Sukarma",
            "Dhriti", "Shula", "Ganda", "Vriddhi", "Dhruva", "Vyaghata", "Harshana", "Vajra",
            "Siddhi", "Vyatipata", "Variyan", "Parigha", "Shiva", "Siddha", "Sadhya", "Shubha",
            "Shukla", "Brahma", "Indra", "Vaidhriti"
    };

    private static final String[] KARANAS = {
            "Bava", "Balava", "Kaulava", "Taitila", "Gara", "Vanija", "Vishti (Bhadra)",
            "Shakuni", "Chatushpada", "Naga", "Kinstughna"
    };

    public PanchangResponse getDailyPanchang(PanchangRequest req) {
        LocationData loc = geocodingService.getCoordinates(req.location());
        String tz = loc.timezone();

        LocalDate localDate = LocalDate.of(req.year(), req.month(), req.day());
        ZonedDateTime startOfDay = localDate.atStartOfDay(ZoneId.of(tz));
        ZonedDateTime utcStartOfDay = startOfDay.withZoneSameInstant(ZoneId.of("UTC"));

        double startOfDayHourUt = utcStartOfDay.getHour() + (utcStartOfDay.getMinute() / 60.0);
        double startOfDayJd = ephemerisService.toJulianDay(
                utcStartOfDay.getYear(), utcStartOfDay.getMonthValue(), utcStartOfDay.getDayOfMonth(), startOfDayHourUt
        );

        double[] riseSetJd = ephemerisService.getSunRiseSet(startOfDayJd, loc.latitude(), loc.longitude());
        double sunriseJd = riseSetJd[0];

        String sunriseTime = ephemerisService.formatJulianDayToLocal(riseSetJd[0], tz);
        String sunsetTime = ephemerisService.formatJulianDayToLocal(riseSetJd[1], tz);

        double sunLongitude = ephemerisService.getPlanetData(sunriseJd, SweConst.SE_SUN)[0];
        double moonLongitude = ephemerisService.getPlanetData(sunriseJd, SweConst.SE_MOON)[0];

        String vara = localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        double distance = moonLongitude - sunLongitude;
        if (distance < 0) distance += 360.0;
        int tithiIndex = (int) (distance / 12.0);
        String tithi = TITHIS[tithiIndex];

        int karanaCount = (int) (distance / 6.0);
        String karana;
        if (karanaCount == 0) karana = "Kinstughna";
        else if (karanaCount >= 57) karana = KARANAS[karanaCount - 50];
        else karana = KARANAS[((karanaCount - 1) % 7)];

        double sum = (moonLongitude + sunLongitude) % 360.0;
        int yogaIndex = (int) (sum / (13.0 + 20.0 / 60.0));
        String yoga = YOGAS[yogaIndex];

        int nakIndex = (int) (moonLongitude / (13.0 + 20.0 / 60.0));
        String nakshatra = ZodiacConverter.NAKSHATRAS[nakIndex];

        HouseCalculation houses = ephemerisService.calculateHouses(sunriseJd, loc.latitude(), loc.longitude(), 'W');
        int ascendantSign = (int) ((houses.ascendant() % 360) / 30) + 1;

        List<PlanetPosition> planets = new ArrayList<>();
        planets.add(ZodiacConverter.formatPosition("Sun", ephemerisService.getPlanetData(sunriseJd, SweConst.SE_SUN), 0.0, ascendantSign));
        planets.add(ZodiacConverter.formatPosition("Moon", ephemerisService.getPlanetData(sunriseJd, SweConst.SE_MOON), 0.0, ascendantSign));
        planets.add(ZodiacConverter.formatPosition("Mercury", ephemerisService.getPlanetData(sunriseJd, SweConst.SE_MERCURY), 0.0, ascendantSign));
        planets.add(ZodiacConverter.formatPosition("Venus", ephemerisService.getPlanetData(sunriseJd, SweConst.SE_VENUS), 0.0, ascendantSign));
        planets.add(ZodiacConverter.formatPosition("Mars", ephemerisService.getPlanetData(sunriseJd, SweConst.SE_MARS), 0.0, ascendantSign));
        planets.add(ZodiacConverter.formatPosition("Jupiter", ephemerisService.getPlanetData(sunriseJd, SweConst.SE_JUPITER), 0.0, ascendantSign));
        planets.add(ZodiacConverter.formatPosition("Saturn", ephemerisService.getPlanetData(sunriseJd, SweConst.SE_SATURN), 0.0, ascendantSign));

        double[] rahuData = ephemerisService.getPlanetData(sunriseJd, SweConst.SE_TRUE_NODE);
        planets.add(ZodiacConverter.formatPosition("Rahu", rahuData, 0.0, ascendantSign));

        double[] ketuData = new double[]{(rahuData[0] + 180.0) % 360.0, 0, 0, rahuData[3], 0, 0};
        planets.add(ZodiacConverter.formatPosition("Ketu", ketuData, 0.0, ascendantSign));

        planets.sort(Comparator.comparingInt(PlanetPosition::houseNumber));

        return new PanchangResponse(
                localDate.toString(),
                loc.locationName(),
                sunriseTime,
                sunsetTime,
                vara,
                tithi,
                nakshatra,
                yoga,
                karana,
                ZodiacConverter.convertDegreeToSign(sunLongitude),
                ZodiacConverter.convertDegreeToSign(moonLongitude),
                planets
        );
    }
}