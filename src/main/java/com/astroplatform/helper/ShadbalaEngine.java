package com.astroplatform.helper;

import com.astroplatform.dto.PlanetPosition;
import com.astroplatform.dto.ShadbalaItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShadbalaEngine {

    private static final Map<String, Double> NAISARGIKA = Map.of(
            "Sun", 60.0, "Moon", 51.43, "Venus", 42.85,
            "Jupiter", 34.28, "Mercury", 25.71, "Mars", 17.14, "Saturn", 8.57
    );

    private static final Map<String, Double> REQUIRED_RUPAS = Map.of(
            "Sun", 5.0, "Moon", 6.0, "Mars", 5.0, "Mercury", 7.0,
            "Jupiter", 6.5, "Venus", 5.5, "Saturn", 5.0
    );

    private static final Map<String, Double> UCHCHA_DEG = Map.of(
            "Sun", 10.0, "Moon", 33.0, "Mars", 298.0,
            "Mercury", 165.0, "Jupiter", 95.0, "Venus", 357.0, "Saturn", 200.0
    );

    private static final Map<String, List<String>> NATURAL_FRIENDS = Map.of(
            "Sun", List.of("Moon", "Mars", "Jupiter"), "Moon", List.of("Sun", "Mercury"),
            "Mars", List.of("Sun", "Moon", "Jupiter"), "Mercury", List.of("Sun", "Venus"),
            "Jupiter", List.of("Sun", "Moon", "Mars"), "Venus", List.of("Mercury", "Saturn"),
            "Saturn", List.of("Mercury", "Venus")
    );

    private static final Map<String, List<String>> NATURAL_ENEMIES = Map.of(
            "Sun", List.of("Venus", "Saturn"), "Moon", List.of(),
            "Mars", List.of("Mercury"), "Mercury", List.of("Moon"),
            "Jupiter", List.of("Mercury", "Venus"), "Venus", List.of("Sun", "Moon"),
            "Saturn", List.of("Sun", "Moon", "Mars")
    );

    private static final Map<String, List<Integer>> OWN_SIGNS = Map.of(
            "Sun", List.of(5), "Moon", List.of(4), "Mars", List.of(1, 8),
            "Mercury", List.of(3, 6), "Jupiter", List.of(9, 12),
            "Venus", List.of(2, 7), "Saturn", List.of(10, 11)
    );

    public static List<ShadbalaItem> calculate(List<PlanetPosition> d1, List<PlanetPosition> d2, List<PlanetPosition> d3, List<PlanetPosition> d4, List<PlanetPosition> d7, List<PlanetPosition> d9, List<PlanetPosition> d12, double ascDegree, double jd, double sunriseJd) {
        List<ShadbalaItem> results = new ArrayList<>();
        PlanetPosition sun = getPlanet(d1, "Sun");
        PlanetPosition moon = getPlanet(d1, "Moon");

        for (String pName : NAISARGIKA.keySet()) {
            PlanetPosition p = getPlanet(d1, pName);
            PlanetPosition pD9 = getPlanet(d9, pName);
            if (p == null || pD9 == null) continue;

            // 1. STHANA BALA (Includes newly added Saptavargaja)
            double sthanaBala = calculateUchchaBala(p.absoluteDegree(), UCHCHA_DEG.get(pName)) +
                    calculateKendradiBala(p.houseNumber()) +
                    calculateDrekkanaBala(pName, p.absoluteDegree()) +
                    calculateOjhayugmaBala(pName, p.signNumber(), pD9.signNumber()) +
                    calculateSaptavargajaBala(pName, d1, d2, d3, d4, d7, d9, d12);

            // 2. DIG BALA
            double digBala = calculateDigBala(pName, p.absoluteDegree(), ascDegree);

            // 3. KALA BALA (Includes newly added Ahargana Time Lords)
            double ayanaBala = 0.0;
            double kranti = p.declination();
            if (pName.equals("Sun") || pName.equals("Mars") || pName.equals("Jupiter") || pName.equals("Venus")) ayanaBala = ((24.0 + kranti) / 48.0) * 60.0;
            else if (pName.equals("Moon") || pName.equals("Saturn")) ayanaBala = ((24.0 - kranti) / 48.0) * 60.0;
            else if (pName.equals("Mercury")) ayanaBala = ((24.0 + Math.abs(kranti)) / 48.0) * 60.0;
            if (pName.equals("Sun")) ayanaBala *= 2.0;

            double kalaBala = calculateNathonnathaBala(pName, sun.absoluteDegree(), ascDegree) +
                    calculatePakshaBala(pName, moon.absoluteDegree(), sun.absoluteDegree()) +
                    ayanaBala +
                    calculateTribhagaBala(pName, sun.absoluteDegree(), ascDegree) +
                    calculateKalaLordsBala(pName, jd, sunriseJd);
            if (pName.equals("Mercury")) kalaBala += 60;

            // 4. CHESTA BALA
            double chestaBala = (pName.equals("Sun") || pName.equals("Moon")) ?
                    calculatePakshaBala("Moon", moon.absoluteDegree(), sun.absoluteDegree()) :
                    (p.speed() < 0 ? 60.0 : p.speed() < 0.1 ? 45.0 : p.speed() > 1.0 ? 15.0 : 30.0);

            // 5. NAISARGIKA BALA
            double naisargikaBala = NAISARGIKA.get(pName);

            // 6. DRIK BALA
            double drikBala = calculateDrikBala(p, d1);

            double totalVirupas = sthanaBala + digBala + kalaBala + chestaBala + naisargikaBala + drikBala;
            results.add(new ShadbalaItem(pName, round(sthanaBala), round(digBala), round(kalaBala), round(chestaBala), round(naisargikaBala), round(drikBala), round(Math.max(0, totalVirupas / 60.0)), REQUIRED_RUPAS.get(pName)));
        }
        results.sort((a, b) -> Double.compare(b.totalRupas(), a.totalRupas()));
        return results;
    }

    private static PlanetPosition getPlanet(List<PlanetPosition> planets, String name) { return planets.stream().filter(p -> p.name().equalsIgnoreCase(name)).findFirst().orElse(null); }
    private static double calculateUchchaBala(double pDeg, double exDeg) { double dist = Math.abs(pDeg - ((exDeg + 180) % 360)); return (dist > 180 ? 360 - dist : dist) / 3.0; }
    private static double calculateKendradiBala(int h) { return (h == 1 || h == 4 || h == 7 || h == 10) ? 60.0 : (h == 2 || h == 5 || h == 8 || h == 11) ? 30.0 : 15.0; }

    private static double calculateDrekkanaBala(String pName, double deg) {
        int drekkana = (int) ((deg % 30) / 10) + 1;
        if ((pName.equals("Sun") || pName.equals("Mars") || pName.equals("Jupiter")) && drekkana == 1) return 15.0;
        if ((pName.equals("Mercury") || pName.equals("Saturn")) && drekkana == 2) return 15.0;
        if ((pName.equals("Moon") || pName.equals("Venus")) && drekkana == 3) return 15.0;
        return 0.0;
    }

    private static double calculateOjhayugmaBala(String pName, int d1Sign, int d9Sign) {
        double pts = 0; boolean isFem = pName.equals("Moon") || pName.equals("Venus");
        if ((isFem && d1Sign % 2 == 0) || (!isFem && d1Sign % 2 != 0)) pts += 15.0;
        if ((isFem && d9Sign % 2 == 0) || (!isFem && d9Sign % 2 != 0)) pts += 15.0;
        return pts;
    }

    // THE 7-VARGA DIGNITY MATRIX
    private static double calculateSaptavargajaBala(String pName, List<PlanetPosition> d1, List<PlanetPosition> d2, List<PlanetPosition> d3, List<PlanetPosition> d4, List<PlanetPosition> d7, List<PlanetPosition> d9, List<PlanetPosition> d12) {
        double total = 0;
        for (List<PlanetPosition> varga : List.of(d1, d2, d3, d4, d7, d9, d12)) {
            PlanetPosition target = getPlanet(varga, pName);
            if (target == null) continue;
            int sign = target.signNumber();
            if (OWN_SIGNS.get(pName).contains(sign)) {
                total += (varga == d1 && isMoolatrikona(pName, sign, target.degrees())) ? 45.0 : 30.0; continue;
            }
            String lord = getSignLord(sign);
            boolean isNatFriend = NATURAL_FRIENDS.get(pName).contains(lord);
            boolean isNatEnemy = NATURAL_ENEMIES.get(pName).contains(lord);
            int diff = getPlanet(d1, lord).houseNumber() - getPlanet(d1, pName).houseNumber() + 1;
            if (diff <= 0) diff += 12;
            boolean isTempFriend = diff == 2 || diff == 3 || diff == 4 || diff == 10 || diff == 11 || diff == 12;

            if (isNatFriend && isTempFriend) total += 22.5;
            else if ((isNatFriend && !isTempFriend) || (!isNatFriend && !isNatEnemy && isTempFriend)) total += 15.0;
            else if ((!isNatFriend && !isNatEnemy && !isTempFriend) || (isNatEnemy && isTempFriend)) total += 7.5;
            else if (isNatEnemy && !isTempFriend) total += 3.75;
            else total += 1.875;
        }
        return total;
    }

    private static String getSignLord(int s) { return (s==1||s==8)?"Mars":(s==2||s==7)?"Venus":(s==3||s==6)?"Mercury":(s==4)?"Moon":(s==5)?"Sun":(s==9||s==12)?"Jupiter":"Saturn"; }
    private static boolean isMoolatrikona(String p, int s, int d) { return (p.equals("Sun")&&s==5&&d<=20) || (p.equals("Moon")&&s==2&&d>3) || (p.equals("Mars")&&s==1&&d<=12) || (p.equals("Mercury")&&s==6&&d>15&&d<=20) || (p.equals("Jupiter")&&s==9&&d<=10) || (p.equals("Venus")&&s==7&&d<=15) || (p.equals("Saturn")&&s==11&&d<=20); }

    private static double calculateDigBala(String pName, double pDeg, double ascDeg) {
        double strPt = ascDeg;
        if (pName.equals("Sun") || pName.equals("Mars")) strPt = (ascDeg + 270) % 360;
        else if (pName.equals("Saturn")) strPt = (ascDeg + 180) % 360;
        else if (pName.equals("Moon") || pName.equals("Venus")) strPt = (ascDeg + 90) % 360;
        double dist = Math.abs(pDeg - ((strPt + 180) % 360));
        return (dist > 180 ? 360 - dist : dist) / 3.0;
    }

    private static double calculateNathonnathaBala(String pName, double sunDeg, double ascDeg) {
        double dist = Math.abs(sunDeg - ((ascDeg + 90) % 360));
        double midStr = (180.0 - (dist > 180 ? 360 - dist : dist)) / 3.0;
        if (pName.equals("Moon") || pName.equals("Mars") || pName.equals("Saturn")) return midStr;
        else if (pName.equals("Sun") || pName.equals("Jupiter") || pName.equals("Venus")) return 60.0 - midStr;
        return 60.0;
    }

    private static double calculatePakshaBala(String pName, double mDeg, double sDeg) {
        double angle = (mDeg - sDeg + 360) % 360;
        double v = (angle <= 180) ? (angle / 3.0) : ((360 - angle) / 3.0);
        if (pName.equals("Moon") || pName.equals("Venus") || pName.equals("Mars")) return v;
        else if (pName.equals("Sun") || pName.equals("Jupiter") || pName.equals("Saturn")) return 60.0 - v;
        return 30.0;
    }

    private static double calculateTribhagaBala(String pName, double sDeg, double aDeg) {
        double dist = Math.abs(sDeg - ((aDeg + 90) % 360));
        boolean isNight = (dist > 180 ? 360 - dist : dist) < 90;
        double frac = (Math.abs(sDeg - aDeg) % 180) / 180.0;
        if (!isNight) {
            if (frac <= 0.33 && pName.equals("Mercury")) return 60.0;
            if (frac > 0.33 && frac <= 0.66 && pName.equals("Sun")) return 60.0;
            if (frac > 0.66 && pName.equals("Saturn")) return 60.0;
        } else {
            if (frac <= 0.33 && pName.equals("Moon")) return 60.0;
            if (frac > 0.33 && frac <= 0.66 && pName.equals("Venus")) return 60.0;
            if (frac > 0.66 && pName.equals("Mars")) return 60.0;
        }
        return pName.equals("Jupiter") ? 60.0 : 0.0;
    }

    // THE AHARGANA TIME LORDS CALCULATOR
    private static double calculateKalaLordsBala(String pName, double jd, double sunriseJd) {
        double ahargana = Math.floor(sunriseJd - 588465.5);
        int dow = (int) ((ahargana + 5) % 7);
        String[] lds = {"Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn"};
        double pts = 0;
        if (pName.equals(lds[dow])) pts += 45.0; // Day Lord
        double hrs = (jd - sunriseJd) * 24.0;
        int[] seq = {6, 4, 2, 0, 5, 3, 1};
        int rIdx = 0; for (int i = 0; i < 7; i++) if (seq[i] == dow) rIdx = i;
        if (pName.equals(lds[seq[(rIdx + (int) Math.floor(hrs < 0 ? hrs + 24.0 : hrs)) % 7]])) pts += 60.0; // Hour Lord
        if (pName.equals(lds[(dow + (((int) ((ahargana / 30) % 7)) * 2)) % 7])) pts += 30.0; // Month Lord
        if (pName.equals(lds[(dow + (((int) ((ahargana / 360) % 7)) * 3)) % 7])) pts += 15.0; // Year Lord
        return pts;
    }

    private static double calculateDrikBala(PlanetPosition t, List<PlanetPosition> pl) {
        double drik = 0;
        for (PlanetPosition a : pl) {
            if (a.name().equals(t.name()) || a.name().equals("Rahu") || a.name().equals("Ketu")) continue;
            double d = (t.absoluteDegree() - a.absoluteDegree() + 360) % 360;
            double str = (d>=30&&d<60)?(d-30)/2.0:(d>=60&&d<90)?15+((d-60)/2.0):(d>=90&&d<120)?30+((d-90)/2.0):(d>=120&&d<150)?45-((d-120)/2.0):(d>=150&&d<180)?30+(d-150):(d>=180&&d<300)?(300-d)/2.0:0;
            if (a.name().equals("Saturn") && ((d>=60&&d<=90) || (d>=270&&d<=300))) str = 60.0;
            if (a.name().equals("Jupiter") && ((d>=120&&d<=150) || (d>=240&&d<=270))) str = 60.0;
            if (a.name().equals("Mars") && ((d>=90&&d<=120) || (d>=210&&d<=240))) str = 60.0;
            drik += (a.name().equals("Jupiter") || a.name().equals("Venus") || a.name().equals("Mercury")) ? (str / 4.0) : -(str / 4.0);
        }
        return drik;
    }

    private static double round(double v) { return Math.round(v * 100.0) / 100.0; }
}