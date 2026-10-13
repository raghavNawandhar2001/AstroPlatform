package com.astroplatform.service;

import com.astroplatform.dto.*;
import com.astroplatform.helper.*;
import de.thmac.swisseph.SweConst;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@AllArgsConstructor
public class ChartService {

    private final EphemerisService ephemerisService;
    private final GeocodingService geocodingService;

    private static final String[] DASHA_PLANETS = {"Ketu", "Venus", "Sun", "Moon", "Mars", "Rahu", "Jupiter", "Saturn", "Mercury"};
    private static final int[] DASHA_YEARS = {7, 20, 6, 10, 7, 18, 16, 19, 17};
    private static final double DAYS_IN_YEAR = 365.2425; // Change to 360.0 to strictly follow traditional Savana Year

    public CompleteChartResponse generateBirthChart(ChartDetailsRequest req) {

        LocationData loc = geocodingService.getCoordinates(req.getLocation());
        String tz = loc.timezone();

        LocalDateTime localBirthTime = LocalDateTime.of(req.getYear(), req.getMonth(), req.getDay(), req.getHour(), req.getMinute());
        ZonedDateTime localZoned = localBirthTime.atZone(ZoneId.of(tz));
        ZonedDateTime utcZoned = localZoned.withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(tz));

        double hourUt = utcZoned.getHour() + (utcZoned.getMinute() / 60.0);
        double jd = ephemerisService.toJulianDay(utcZoned.getYear(), utcZoned.getMonthValue(), utcZoned.getDayOfMonth(), hourUt);

        ZonedDateTime localStartOfDay = localZoned.toLocalDate().atStartOfDay(ZoneId.of(tz));
        ZonedDateTime utcStartOfDay = localStartOfDay.withZoneSameInstant(ZoneOffset.UTC);
        double startOfDayHourUt = utcStartOfDay.getHour() + (utcStartOfDay.getMinute() / 60.0);
        double startOfDayJd = ephemerisService.toJulianDay(utcStartOfDay.getYear(), utcStartOfDay.getMonthValue(), utcStartOfDay.getDayOfMonth(), startOfDayHourUt);

        HouseCalculation houses = ephemerisService.calculateHouses(jd, loc.latitude(), loc.longitude(), 'O');

        List<PlanetPosition> basePlanets = new ArrayList<>();
        int[] swePlanets = {SweConst.SE_SUN, SweConst.SE_MOON, SweConst.SE_MERCURY, SweConst.SE_VENUS, SweConst.SE_MARS, SweConst.SE_JUPITER, SweConst.SE_SATURN};
        String[] planetNames = {"Sun", "Moon", "Mercury", "Venus", "Mars", "Jupiter", "Saturn"};

        for (int i = 0; i < swePlanets.length; i++) {
            double[] lonData = ephemerisService.getPlanetData(jd, swePlanets[i]);
            double[] eqData = ephemerisService.getEquatorialData(jd, swePlanets[i]);
            double declination = eqData[1];
            basePlanets.add(ZodiacConverter.formatPosition(planetNames[i], lonData, declination, 1));
        }

        double[] rahuData = ephemerisService.getPlanetData(jd, SweConst.SE_MEAN_NODE);
        basePlanets.add(ZodiacConverter.formatPosition("Rahu", rahuData, 0.0, 1));
        double[] ketuData = new double[]{(rahuData[0] + 180.0) % 360.0, 0, 0, rahuData[3], 0, 0};
        basePlanets.add(ZodiacConverter.formatPosition("Ketu", ketuData, 0.0, 1));

        List<String> formattedCusps = new ArrayList<>();
        for (int i = 1; i <= 12; i++) { formattedCusps.add("House " + i + ": " + ZodiacConverter.convertDegreeToSign(houses.cusps()[i])); }

        Map<String, SingleChartData> divisionalCharts = new LinkedHashMap<>();
        int[] vargas = {1, 2, 3, 4, 7, 9, 10, 12, 16, 20, 24, 27, 30, 40, 45, 60};
        String[] vargaNames = {"D1", "D2", "D3", "D4", "D7", "D9", "D10", "D12", "D16", "D20", "D24", "D27", "D30", "D40", "D45", "D60"};

        for (int i = 0; i < vargas.length; i++) {
            int div = vargas[i];
            int ascSign = VargaCalculator.getVargaSign(houses.ascendant(), div);
            PlanetPosition ascPos = ZodiacConverter.getAscendantPosition(houses.ascendant());

            List<PlanetPosition> vargaPlanets = new ArrayList<>();
            for (PlanetPosition bp : basePlanets) {
                int pSign = VargaCalculator.getVargaSign(bp.absoluteDegree(), div);
                int houseNum = pSign - ascSign + 1;
                if (houseNum <= 0) houseNum += 12;

                vargaPlanets.add(new PlanetPosition(
                        bp.name(), ZodiacConverter.getSignName(pSign), pSign, bp.degrees(), bp.minutes(),
                        bp.absoluteDegree(), bp.isRetrograde(), houseNum, bp.nakshatraName(),
                        bp.nakshatraNumber(), bp.nakshatraPada(), bp.declination(), bp.speed()
                ));
            }
            vargaPlanets.sort(Comparator.comparingInt(PlanetPosition::houseNumber));
            divisionalCharts.put(vargaNames[i], new SingleChartData(houses.ascendant(), ascSign, ZodiacConverter.getSignName(ascSign), String.format("%d° %02d'", ascPos.degrees(), ascPos.minutes()), ascPos.nakshatraName(), ascPos.nakshatraPada(), (div == 1) ? formattedCusps : new ArrayList<>(), vargaPlanets));
        }

        PlanetPosition chalitAscPos = ZodiacConverter.getAscendantPosition(houses.ascendant());
        int chalitAscSign = chalitAscPos.signNumber();
        List<PlanetPosition> chalitPlanets = new ArrayList<>();

        for (PlanetPosition bp : basePlanets) {
            int houseNum = getChalitHouse(bp.absoluteDegree(), houses.cusps());
            chalitPlanets.add(new PlanetPosition(
                    bp.name(), bp.signName(), bp.signNumber(), bp.degrees(), bp.minutes(),
                    bp.absoluteDegree(), bp.isRetrograde(), houseNum, bp.nakshatraName(),
                    bp.nakshatraNumber(), bp.nakshatraPada(), bp.declination(), bp.speed()
            ));
        }
        chalitPlanets.sort(Comparator.comparingInt(PlanetPosition::houseNumber));
        divisionalCharts.put("Chalit", new SingleChartData(houses.ascendant(), chalitAscSign, ZodiacConverter.getSignName(chalitAscSign), String.format("%d° %02d'", chalitAscPos.degrees(), chalitAscPos.minutes()), chalitAscPos.nakshatraName(), chalitAscPos.nakshatraPada(), formattedCusps, chalitPlanets));

        // 4. ADVANCED METRICS (Updated with full 7 charts & JD times)
        JaiminiDetails jaimini = AdvancedAstroCalculator.calculateJaimini(basePlanets, divisionalCharts.get("D9").planets(), divisionalCharts.get("D9").ascendantSignName());

        List<PlanetPosition> d1 = divisionalCharts.get("D1").planets();
        List<PlanetPosition> d2 = divisionalCharts.get("D2").planets();
        List<PlanetPosition> d3 = divisionalCharts.get("D3").planets();
        List<PlanetPosition> d4 = divisionalCharts.get("D4").planets();
        List<PlanetPosition> d7 = divisionalCharts.get("D7").planets();
        List<PlanetPosition> d9 = divisionalCharts.get("D9").planets();
        List<PlanetPosition> d12 = divisionalCharts.get("D12").planets();

        List<ShadbalaItem> shadbala = ShadbalaEngine.calculate(d1, d2, d3, d4, d7, d9, d12, houses.ascendant(), jd, startOfDayJd);
        Map<String, int[]> ashtakvarga = AdvancedAstroCalculator.calculateAshtakvarga(basePlanets, chalitAscSign);

        // 5. JAIMINI CHARTS
        PlanetPosition akPlanetD1 = basePlanets.stream().filter(p -> p.name().equalsIgnoreCase(jaimini.atmakaraka())).findFirst().orElse(basePlanets.get(0));
        PlanetPosition akPlanetD9 = divisionalCharts.get("D9").planets().stream().filter(p -> p.name().equalsIgnoreCase(akPlanetD1.name())).findFirst().orElse(divisionalCharts.get("D9").planets().get(0));

        int karakamshaSignNumber = akPlanetD9.signNumber();
        double pseudoAscendantDegree = (karakamshaSignNumber - 1) * 30.0 + 15.0;
        PlanetPosition pseudoAscPos = ZodiacConverter.getAscendantPosition(pseudoAscendantDegree);

        List<PlanetPosition> swamshaPlanets = new ArrayList<>();
        for (PlanetPosition p : divisionalCharts.get("D9").planets()) {
            int newHouseNumber = p.signNumber() - karakamshaSignNumber + 1;
            if (newHouseNumber <= 0) newHouseNumber += 12;
            swamshaPlanets.add(new PlanetPosition(p.name(), p.signName(), p.signNumber(), p.degrees(), p.minutes(), p.absoluteDegree(), p.isRetrograde(), newHouseNumber, p.nakshatraName(), p.nakshatraNumber(), p.nakshatraPada(), p.declination(), p.speed()));
        }
        swamshaPlanets.sort(Comparator.comparingInt(PlanetPosition::houseNumber));
        divisionalCharts.put("Swamsha", new SingleChartData(pseudoAscendantDegree, karakamshaSignNumber, ZodiacConverter.getSignName(karakamshaSignNumber), String.format("%d° %02d'", pseudoAscPos.degrees(), pseudoAscPos.minutes()), pseudoAscPos.nakshatraName(), pseudoAscPos.nakshatraPada(), new ArrayList<>(), swamshaPlanets));

        List<PlanetPosition> karakamshaPlanets = new ArrayList<>();
        for (PlanetPosition p : basePlanets) {
            int newHouseNumber = p.signNumber() - karakamshaSignNumber + 1;
            if (newHouseNumber <= 0) newHouseNumber += 12;
            karakamshaPlanets.add(new PlanetPosition(p.name(), p.signName(), p.signNumber(), p.degrees(), p.minutes(), p.absoluteDegree(), p.isRetrograde(), newHouseNumber, p.nakshatraName(), p.nakshatraNumber(), p.nakshatraPada(), p.declination(), p.speed()));
        }
        karakamshaPlanets.sort(Comparator.comparingInt(PlanetPosition::houseNumber));
        divisionalCharts.put("Karakamsha", new SingleChartData(pseudoAscendantDegree, karakamshaSignNumber, ZodiacConverter.getSignName(karakamshaSignNumber), String.format("%d° %02d'", pseudoAscPos.degrees(), pseudoAscPos.minutes()), pseudoAscPos.nakshatraName(), pseudoAscPos.nakshatraPada(), new ArrayList<>(), karakamshaPlanets));

        // 6. ASTRONOMICAL DATA
        double[] riseSetJd = ephemerisService.getSunRiseSet(startOfDayJd, loc.latitude(), loc.longitude());
        double nextEclipseJd = ephemerisService.getNextSolarEclipse(jd, loc.latitude(), loc.longitude());
        AstronomicalData astroData = new AstronomicalData(ephemerisService.getAyanamsa(jd), ephemerisService.getSiderealTime(jd), ephemerisService.formatJulianDayToLocal(riseSetJd[0], tz), ephemerisService.formatJulianDayToLocal(riseSetJd[1], tz), ephemerisService.formatJulianDayToLocal(nextEclipseJd, tz));

        // 7. VIMSHOTTARI DASHA
        double moonAbsoluteDegree = basePlanets.stream().filter(p -> p.name().equalsIgnoreCase("Moon")).findFirst().get().absoluteDegree();
        double nakshatraSpan = 360.0 / 27.0;
        int dashaStartIndex = ((int) (moonAbsoluteDegree / nakshatraSpan)) % 9;
        double yearsPassedBeforeBirth = DASHA_YEARS[dashaStartIndex] * ((moonAbsoluteDegree % nakshatraSpan) / nakshatraSpan);
        ZonedDateTime rootStartDate = localZoned.minusSeconds((long) (yearsPassedBeforeBirth * 365.2425 * 24 * 3600));

        List<DashaNode> fullDashaTree = generateDashaTree(rootStartDate, dashaStartIndex, localZoned, now);

        return new CompleteChartResponse(req.getName(), req.getGender(), req.getLocation(), tz, astroData, divisionalCharts, fullDashaTree, jaimini, shadbala, ashtakvarga);
    }

    private int getChalitHouse(double planetDegree, double[] cusps) {
        for (int i = 1; i <= 11; i++) {
            if (isBetween(planetDegree, cusps[i], cusps[i + 1])) return i;
        }
        return 12;
    }

    private boolean isBetween(double deg, double start, double end) {
        if (start < end) return deg >= start && deg < end;
        return deg >= start || deg < end;
    }

    private List<DashaNode> generateDashaTree(ZonedDateTime rootStartDate, int dashaStartIndex, ZonedDateTime birthTime, ZonedDateTime now) {
        List<DashaNode> mahaDashas = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
        double currentOffsetSecs = 0.0;
        long nowSec = now.toEpochSecond();

        for (int i = 0; i < 9; i++) {
            int pIndex = (dashaStartIndex + i) % 9;
            double mahaYears = DASHA_YEARS[pIndex];
            double durationSecs = mahaYears * DAYS_IN_YEAR * 24 * 3600;

            ZonedDateTime mahaStart = rootStartDate.plusSeconds((long) currentOffsetSecs);
            currentOffsetSecs += durationSecs;
            ZonedDateTime mahaEnd = rootStartDate.plusSeconds((long) currentOffsetSecs);

            if (mahaEnd.isAfter(birthTime)) {
                boolean active = (nowSec >= mahaStart.toEpochSecond()) && (nowSec < mahaEnd.toEpochSecond());
                List<DashaNode> subDashas = generateDashaLevel(mahaStart, mahaYears, pIndex, 2, 5, birthTime, now, nowSec, formatter);

                mahaDashas.add(new DashaNode(DASHA_PLANETS[pIndex], mahaStart.format(formatter), mahaEnd.format(formatter), active, subDashas));
            }
        }
        return mahaDashas;
    }

    private List<DashaNode> generateDashaLevel(ZonedDateTime start, double parentYears, int parentLordIndex, int currentLevel, int maxLevel, ZonedDateTime birthTime, ZonedDateTime now, long nowSec, DateTimeFormatter formatter) {
        List<DashaNode> nodes = new ArrayList<>();
        if (currentLevel > maxLevel) return nodes;
        double currentOffsetSecs = 0.0;

        for (int i = 0; i < 9; i++) {
            int pIndex = (parentLordIndex + i) % 9;
            double subYears = (parentYears * DASHA_YEARS[pIndex]) / 120.0;
            double durationSecs = subYears * DAYS_IN_YEAR * 24.0 * 3600.0;

            ZonedDateTime subStart = start.plusSeconds((long) currentOffsetSecs);
            currentOffsetSecs += durationSecs;
            ZonedDateTime subEnd = start.plusSeconds((long) currentOffsetSecs);

            if (subEnd.isAfter(birthTime)) {
                boolean active = (nowSec >= subStart.toEpochSecond()) && (nowSec < subEnd.toEpochSecond());
                List<DashaNode> children = generateDashaLevel(subStart, subYears, pIndex, currentLevel + 1, maxLevel, birthTime, now, nowSec, formatter);

                nodes.add(new DashaNode(DASHA_PLANETS[pIndex], subStart.format(formatter), subEnd.format(formatter), active, children));
            }
        }
        return nodes;
    }
}