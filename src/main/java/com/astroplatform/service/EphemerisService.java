package com.astroplatform.service;

import com.astroplatform.helper.HouseCalculation;
import de.thmac.swisseph.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import de.thmac.swisseph.SweDate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EphemerisService {

    @Value("${astro.ephe-path}")
    private String ephePath;

    private SwissEph swisseph;

    @PostConstruct
    public void init() {
        // 1. Initialize the library with the path to your data files
        swisseph = new SwissEph(ephePath);

        // 2. Set the Ayanamsha to Lahiri for Vedic/Sidereal calculations globally
        swisseph.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0, 0);
    }

    /**
     * Converts Gregorian date to Julian Day Number (UT)
     */
    public double toJulianDay(int year, int month, int day, double hourUt) {
        // The constructor automatically handles Gregorian/Julian calendar switch (Oct 1582)
        SweDate sweDate = new SweDate(year, month, day, hourUt);

        // Extract the computed Julian Day
        return sweDate.getJulDay();
    }

    /**
     * Calculates longitude and speed for a planetary body (e.g., SweConst.SE_SUN)
     */
    public double[] getPlanetData(double julianDay, int planetId) {
        double[] result = new double[6]; // [0]=lon, [1]=lat, [2]=dist, [3]=speed in lon
        StringBuffer error = new StringBuffer();

        int flags = SweConst.SEFLG_SPEED | SweConst.SEFLG_SWIEPH | SweConst.SEFLG_SIDEREAL;
        int ret = swisseph.swe_calc_ut(julianDay, planetId, flags, result, error);

        if (ret < 0) {
            throw new RuntimeException("Ephemeris Error: " + error.toString());
        }

        return result; // result[0] is tropical longitude (0° - 360°)
    }

    /*/**
     * Calculates house cusps and Ascendant/MC angles for a specific latitude and longitude.
     * @param houseSystem 'P' for Placidus, 'W' for Whole Sign, 'K' for Koch, etc.
     */
    public HouseCalculation calculateHouses(double julianDay, double latitude, double longitude, char houseSystem) {
        double[] cusps = new double[14]; // Cusps 1 to 12 stored at indices 1-12
        double[] ascmc = new double[10]; // Ascendant at 0, MC at 1, etc.

        // Set flag to 0 for standard Tropical calculations.
        // If you are calculating Vedic/Sidereal houses, you would use SweConst.SEFLG_SIDEREAL here instead.
        int flags = SweConst.SEFLG_SIDEREAL;

        // Note the added 'flags' parameter in the 2nd position!
        swisseph.swe_houses(julianDay, flags, latitude, longitude, (int) houseSystem, cusps, ascmc);

        double ascendant = ascmc[SweConst.SE_ASC];
        double midheaven = ascmc[SweConst.SE_MC];

        return new HouseCalculation(cusps, ascendant, midheaven);
    }
    public double getAyanamsa(double julianDay) {
        return swisseph.swe_get_ayanamsa_ut(julianDay);
    }

    public double getSiderealTime(double julianDay) {
         SwissLib swissLib = new SwissLib();
        return swissLib.swe_sidtime(julianDay);
    }

    public double[] getSunRiseSet(double julianDay, double lat, double lon) {
        DblObj tret = new DblObj();
        StringBuffer serr = new StringBuffer();
        double[] geopos = new double[]{lon, lat, 0}; // lon, lat, elevation

        // Sunrise
        swisseph.swe_rise_trans(julianDay, SweConst.SE_SUN, null, SweConst.SEFLG_SWIEPH,
                SweConst.SE_CALC_RISE, geopos, 0, 0, tret, serr);
        double sunriseJd = tret.val;

        // Sunset
        swisseph.swe_rise_trans(julianDay, SweConst.SE_SUN, null, SweConst.SEFLG_SWIEPH,
                SweConst.SE_CALC_SET, geopos, 0, 0, tret, serr);
        double sunsetJd = tret.val;

        return new double[]{sunriseJd, sunsetJd};
    }

    public double getNextSolarEclipse(double julianDay, double lat, double lon) {
        double[] tret = new double[10]; // Time array: [0] is time of maximum eclipse
        double[] attr = new double[20];
        StringBuffer serr = new StringBuffer();
        double[] geopos = new double[]{lon, lat, 0};

        // 0 at the end means search forward in time (not backward)
        swisseph.swe_sol_eclipse_when_loc(julianDay, SweConst.SEFLG_SWIEPH, geopos, tret, attr, 0, serr);

        return tret[0];
    }


// ... inside EphemerisService ...

    /**
     * Converts a Julian Day decimal back to a human-readable local time string.
     */
    public String formatJulianDayToLocal(double julianDay, String timezoneId) {
        // 1. Reverse JD back into UTC date and time components
        SweDate sweDate = new SweDate(julianDay);

        int year = sweDate.getYear();
        int month = sweDate.getMonth();
        int day = sweDate.getDay();

        // Convert decimal hour back to hours, minutes, seconds
        double hourDecimal = sweDate.getHour();
        int hour = (int) hourDecimal;
        int minute = (int) ((hourDecimal - hour) * 60);
        int second = (int) Math.round((((hourDecimal - hour) * 60) - minute) * 60);

        if (second == 60) {
            second = 0;
            minute += 1;
        }
        if (minute == 60) {
            minute = 0;
            hour += 1;
        }

        // 2. Build the UTC time
        LocalDateTime utcTime = LocalDateTime.of(year, month, day, hour, minute, second);
        ZonedDateTime utcZoned = utcTime.atZone(ZoneId.of("UTC"));

        // 3. Convert to the requested local timezone
        ZonedDateTime localZoned = utcZoned.withZoneSameInstant(ZoneId.of(timezoneId));

        // 4. Return as an ISO 8601 string (e.g., "2001-12-02T07:12:45+05:30")
        return localZoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
    public double[] getEquatorialData(double julianDay, int planetId) {
        double[] result = new double[6]; // [0]=RA, [1]=Declination
        StringBuffer error = new StringBuffer();
        // The SEFLG_EQUATORIAL flag gets Declination instead of Longitude
        int flags = SweConst.SEFLG_SPEED | SweConst.SEFLG_SWIEPH | SweConst.SEFLG_EQUATORIAL;
        swisseph.swe_calc_ut(julianDay, planetId, flags, result, error);
        return result;
    }
}