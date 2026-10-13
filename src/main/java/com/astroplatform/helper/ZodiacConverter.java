package com.astroplatform.helper;

import com.astroplatform.dto.PlanetPosition;

public class ZodiacConverter {

    private static final String[] ZODIAC_SIGNS = {
            "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
            "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    };

    public static final String[] NAKSHATRAS = {
            "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra",
            "Punarvasu", "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni",
            "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha",
            "Mula", "Purva Ashadha", "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha",
            "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    };

    public static PlanetPosition formatPosition(String name, double[] sweData, double declination, int house) {
        double absDeg = sweData[0];
        double speed = sweData[3];

        // HIGH-PRECISION CORRECTION
        double epsilon = 1e-9;

        int signNumber = (int) Math.floor((absDeg + epsilon) / 30) + 1;
        if (signNumber > 12) signNumber = 12;

        String signName = getSignName(signNumber);

        double degreeInSign = (absDeg + epsilon) % 30;
        int degrees = (int) degreeInSign;
        int minutes = (int) ((degreeInSign - degrees) * 60);

        double nakshatraDegree = 360.0 / 27.0;
        int nakshatraNumber = (int) Math.floor((absDeg + epsilon) / nakshatraDegree) + 1;
        String nakshatraName = NAKSHATRAS[nakshatraNumber - 1];

        double padaDegree = nakshatraDegree / 4.0;
        int pada = (int) Math.floor(((absDeg + epsilon) % nakshatraDegree) / padaDegree) + 1;

        boolean isRetrograde = speed < 0;

        return new PlanetPosition(
                name,
                signName,
                signNumber,
                degrees,
                minutes,
                absDeg,
                isRetrograde,
                house,
                nakshatraName,
                nakshatraNumber,
                pada,
                declination, // 12th argument
                speed        // 13th argument
        );
    }

    public static String getSignName(int signNumber) {
        if (signNumber < 1 || signNumber > 12) {
            return "Unknown";
        }
        return ZODIAC_SIGNS[signNumber - 1];
    }

    public static String convertDegreeToSign(double degree) {
        double epsilon = 1e-9;
        int signNumber = (int) Math.floor((degree + epsilon) / 30) + 1;
        double degreeInSign = (degree + epsilon) % 30;
        int deg = (int) degreeInSign;
        int min = (int) ((degreeInSign - deg) * 60);
        return String.format("%s %d° %02d'", getSignName(signNumber), deg, min);
    }

    public static PlanetPosition getAscendantPosition(double absoluteDegree) {
        double epsilon = 1e-9;
        int signNumber = (int) Math.floor((absoluteDegree + epsilon) / 30) + 1;
        if (signNumber > 12) signNumber = 12;

        double degreeInSign = (absoluteDegree + epsilon) % 30;
        int degrees = (int) Math.floor(degreeInSign);
        int minutes = (int) Math.floor((degreeInSign - degrees) * 60);

        double nakshatraDegree = 360.0 / 27.0;
        int nakshatraNumber = (int) Math.floor((absoluteDegree + epsilon) / nakshatraDegree) + 1;
        String nakshatraName = NAKSHATRAS[nakshatraNumber - 1];

        double padaDegree = nakshatraDegree / 4.0;
        int pada = (int) Math.floor(((absoluteDegree + epsilon) % nakshatraDegree) / padaDegree) + 1;

        return new PlanetPosition(
                "Lagna",
                getSignName(signNumber),
                signNumber,
                degrees,
                minutes,
                absoluteDegree,
                false,
                1,
                nakshatraName,
                nakshatraNumber,
                pada,
                0.0, // 12th argument (Ascendant has no declination)
                0.0  // 13th argument (Ascendant has no velocity)
        );
    }
}