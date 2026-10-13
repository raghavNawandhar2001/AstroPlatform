package com.astroplatform.helper;

import com.astroplatform.dto.JaiminiDetails;
import com.astroplatform.dto.PlanetPosition;
import com.astroplatform.dto.ShadbalaItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdvancedAstroCalculator {

    public static JaiminiDetails calculateJaimini(List<PlanetPosition> d1Planets, List<PlanetPosition> d9Planets, String d9AscSignName) {
        List<PlanetPosition> charaKarakas = d1Planets.stream()
                .filter(p -> !p.name().equalsIgnoreCase("Rahu") && !p.name().equalsIgnoreCase("Ketu"))
                .sorted((p1, p2) -> Double.compare(p2.absoluteDegree() % 30, p1.absoluteDegree() % 30))
                .collect(Collectors.toList());

        String ak = charaKarakas.get(0).name();
        String karakamsha = d9Planets.stream().filter(p -> p.name().equalsIgnoreCase(ak)).findFirst().map(PlanetPosition::signName).orElse("Unknown");

        return new JaiminiDetails(
                ak, charaKarakas.get(1).name(), charaKarakas.get(2).name(), charaKarakas.get(3).name(),
                charaKarakas.get(4).name(), charaKarakas.get(5).name(), charaKarakas.get(6).name(),
                karakamsha, d9AscSignName
        );
    }

    public static List<ShadbalaItem> calculateShadbala(List<PlanetPosition> d1Planets, double ascendantAbsolute) {
        List<ShadbalaItem> shadbalaList = new ArrayList<>();
        String[] planets = {"Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn"};
        double[] uchchaDeg = {10, 33, 298, 165, 95, 357, 200};
        Map<String, Double> naisargikaBala = Map.of("Sun", 60.0, "Moon", 51.43, "Venus", 42.85, "Jupiter", 34.28, "Mercury", 25.71, "Mars", 17.14, "Saturn", 8.57);
        Map<String, Double> requiredRupas = Map.of("Sun", 5.0, "Moon", 6.0, "Mars", 5.0, "Mercury", 7.0, "Jupiter", 6.5, "Venus", 5.5, "Saturn", 5.0);

        PlanetPosition sun = d1Planets.stream().filter(p -> p.name().equalsIgnoreCase("Sun")).findFirst().get();
        boolean isDayBirth = sun.houseNumber() >= 7 && sun.houseNumber() <= 12;

        for (int i = 0; i < planets.length; i++) {
            String pName = planets[i];
            PlanetPosition p = d1Planets.stream().filter(pl -> pl.name().equalsIgnoreCase(pName)).findFirst().orElse(null);

            if (p != null) {
                double distanceToDeb = Math.abs(p.absoluteDegree() - ((uchchaDeg[i] + 180) % 360));
                if (distanceToDeb > 180) distanceToDeb = 360 - distanceToDeb;
                double uchchaBala = distanceToDeb / 3.0;
                double kendraBala = (p.houseNumber() % 3 == 1) ? 60 : (p.houseNumber() % 3 == 2) ? 30 : 15;
                double sthanaVirupas = uchchaBala + kendraBala;

                double strongestPoint;
                if (pName.equals("Sun") || pName.equals("Mars")) strongestPoint = (ascendantAbsolute + 270) % 360;
                else if (pName.equals("Jupiter") || pName.equals("Mercury")) strongestPoint = ascendantAbsolute;
                else if (pName.equals("Saturn")) strongestPoint = (ascendantAbsolute + 180) % 360;
                else strongestPoint = (ascendantAbsolute + 90) % 360;

                double distanceToWeak = Math.abs(p.absoluteDegree() - ((strongestPoint + 180) % 360));
                if (distanceToWeak > 180) distanceToWeak = 360 - distanceToWeak;
                double digVirupas = distanceToWeak / 3.0;

                double kalaVirupas = 0;
                if (pName.equals("Mercury")) kalaVirupas = 60;
                else if (isDayBirth && (pName.equals("Sun") || pName.equals("Jupiter") || pName.equals("Venus"))) kalaVirupas = 60;
                else if (!isDayBirth && (pName.equals("Moon") || pName.equals("Mars") || pName.equals("Saturn"))) kalaVirupas = 60;

                if (pName.equals("Moon")) {
                    double moonDist = (p.absoluteDegree() - sun.absoluteDegree() + 360) % 360;
                    kalaVirupas += (moonDist <= 180) ? (moonDist / 3.0) : ((360 - moonDist) / 3.0);
                } else {
                    kalaVirupas += 30;
                }

                double chestaVirupas = p.isRetrograde() ? 60.0 : 30.0;
                if (pName.equals("Sun") || pName.equals("Moon")) chestaVirupas = 45.0;

                double naisargikaVirupas = naisargikaBala.get(pName);

                double drikVirupas = 0;
                for (PlanetPosition aspector : d1Planets) {
                    if (aspector.name().equals(pName) || aspector.name().equals("Rahu") || aspector.name().equals("Ketu")) continue;
                    double diff = (p.absoluteDegree() - aspector.absoluteDegree() + 360) % 360;
                    double aspectStrength = 0;
                    if (diff >= 150 && diff <= 210) aspectStrength = 15;
                    else if (diff >= 60 && diff <= 120) aspectStrength = 7.5;
                    if (aspector.name().equals("Jupiter") || aspector.name().equals("Venus") || aspector.name().equals("Mercury")) drikVirupas += aspectStrength;
                    else drikVirupas -= aspectStrength;
                }

                double totalVirupas = sthanaVirupas + digVirupas + kalaVirupas + chestaVirupas + naisargikaVirupas + drikVirupas;
                double totalRupas = Math.max(0, totalVirupas / 60.0);

                shadbalaList.add(new ShadbalaItem(pName,
                        Math.round(sthanaVirupas * 100.0) / 100.0, Math.round(digVirupas * 100.0) / 100.0,
                        Math.round(kalaVirupas * 100.0) / 100.0, Math.round(chestaVirupas * 100.0) / 100.0,
                        Math.round(naisargikaVirupas * 100.0) / 100.0, Math.round(drikVirupas * 100.0) / 100.0,
                        Math.round(totalRupas * 100.0) / 100.0, requiredRupas.get(pName)));
            }
        }
        shadbalaList.sort((a, b) -> Double.compare(b.totalRupas(), a.totalRupas()));
        return shadbalaList;
    }

    public static int[] calculateSarvashtakvarga(List<PlanetPosition> d1Planets) {
        int[] sav = new int[12];
        for (PlanetPosition p : d1Planets) {
            if (p.name().equalsIgnoreCase("Rahu") || p.name().equalsIgnoreCase("Ketu")) continue;
            int signIndex = p.signNumber() - 1;
            int[] beneficHousesFromPlanet = {3, 6, 10, 11};
            for (int h : beneficHousesFromPlanet) sav[(signIndex + h - 1) % 12] += 3;
            for(int i=0; i<12; i++) sav[i] += 2;
        }
        for(int i=0; i<12; i++) sav[i] = Math.min(Math.max(sav[i], 20), 45);
        return sav;
    }
    // --- 3. EXACT BPHS BHINNASHTAKVARGA (BAV) ---
    // --- 3. EXACT BPHS BHINNASHTAKVARGA (BAV - 337 Bindus) ---
    public static Map<String, int[]> calculateAshtakvarga(List<PlanetPosition> planets, int ascendantSign) {
        Map<String, Integer> pos = planets.stream()
                .filter(p -> !p.name().equalsIgnoreCase("Rahu") && !p.name().equalsIgnoreCase("Ketu"))
                .collect(Collectors.toMap(PlanetPosition::name, p -> p.signNumber() - 1));
        int asc = ascendantSign - 1;

        Map<String, int[]> bav = new LinkedHashMap<>();
        String[] pNames = {"Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn"};
        for (String p : pNames) bav.put(p, new int[12]);

        // 1. Sun BAV (Total 48)
        applyBav(bav.get("Sun"), pos.get("Sun"), new int[]{1,2,4,7,8,9,10,11});
        applyBav(bav.get("Sun"), pos.get("Moon"), new int[]{3,6,10,11});
        applyBav(bav.get("Sun"), pos.get("Mars"), new int[]{1,2,4,7,8,9,10,11});
        applyBav(bav.get("Sun"), pos.get("Mercury"), new int[]{3,5,6,9,10,11,12});
        applyBav(bav.get("Sun"), pos.get("Jupiter"), new int[]{5,6,9,11});
        applyBav(bav.get("Sun"), pos.get("Venus"), new int[]{6,7,12});
        applyBav(bav.get("Sun"), pos.get("Saturn"), new int[]{1,2,4,7,8,9,10,11});
        applyBav(bav.get("Sun"), asc, new int[]{3,4,6,10,11,12});

        // 2. Moon BAV (Total 49)
        applyBav(bav.get("Moon"), pos.get("Sun"), new int[]{3,6,7,8,10,11}); // Fixed
        applyBav(bav.get("Moon"), pos.get("Moon"), new int[]{1,3,6,7,10,11});
        applyBav(bav.get("Moon"), pos.get("Mars"), new int[]{2,3,5,6,9,10,11});
        applyBav(bav.get("Moon"), pos.get("Mercury"), new int[]{1,3,4,5,7,8,10,11});
        applyBav(bav.get("Moon"), pos.get("Jupiter"), new int[]{1,4,7,8,10,11,12});
        applyBav(bav.get("Moon"), pos.get("Venus"), new int[]{3,4,5,7,9,10,11});
        applyBav(bav.get("Moon"), pos.get("Saturn"), new int[]{3,5,6,11});
        applyBav(bav.get("Moon"), asc, new int[]{3,6,10,11});

        // 3. Mars BAV (Total 39)
        applyBav(bav.get("Mars"), pos.get("Sun"), new int[]{3,5,6,10,11});
        applyBav(bav.get("Mars"), pos.get("Moon"), new int[]{3,6,11});
        applyBav(bav.get("Mars"), pos.get("Mars"), new int[]{1,2,4,7,8,10,11});
        applyBav(bav.get("Mars"), pos.get("Mercury"), new int[]{3,5,6,11});
        applyBav(bav.get("Mars"), pos.get("Jupiter"), new int[]{6,10,11,12});
        applyBav(bav.get("Mars"), pos.get("Venus"), new int[]{6,8,11,12});
        applyBav(bav.get("Mars"), pos.get("Saturn"), new int[]{1,4,7,8,9,10,11});
        applyBav(bav.get("Mars"), asc, new int[]{1,3,6,10,11});

        // 4. Mercury BAV (Total 54)
        applyBav(bav.get("Mercury"), pos.get("Sun"), new int[]{5,6,9,11,12});
        applyBav(bav.get("Mercury"), pos.get("Moon"), new int[]{2,4,6,8,10,11});
        applyBav(bav.get("Mercury"), pos.get("Mars"), new int[]{1,2,4,7,8,9,10,11});
        applyBav(bav.get("Mercury"), pos.get("Mercury"), new int[]{1,3,5,6,9,10,11,12});
        applyBav(bav.get("Mercury"), pos.get("Jupiter"), new int[]{6,8,11,12});
        applyBav(bav.get("Mercury"), pos.get("Venus"), new int[]{1,2,3,4,5,8,9,11});
        applyBav(bav.get("Mercury"), pos.get("Saturn"), new int[]{1,2,4,7,8,9,10,11});
        applyBav(bav.get("Mercury"), asc, new int[]{1,2,4,6,8,10,11});

        // 5. Jupiter BAV (Total 56)
        applyBav(bav.get("Jupiter"), pos.get("Sun"), new int[]{1,2,3,4,7,8,9,10,11});
        applyBav(bav.get("Jupiter"), pos.get("Moon"), new int[]{2,5,7,9,11});
        applyBav(bav.get("Jupiter"), pos.get("Mars"), new int[]{1,2,4,7,8,10,11});
        applyBav(bav.get("Jupiter"), pos.get("Mercury"), new int[]{1,2,4,5,6,9,10,11});
        applyBav(bav.get("Jupiter"), pos.get("Jupiter"), new int[]{1,2,3,4,7,8,10,11});
        applyBav(bav.get("Jupiter"), pos.get("Venus"), new int[]{2,5,6,9,10,11});
        applyBav(bav.get("Jupiter"), pos.get("Saturn"), new int[]{3,5,6,12});
        applyBav(bav.get("Jupiter"), asc, new int[]{1,2,4,5,6,9,10,11,12}); // Fixed

        // 6. Venus BAV (Total 52)
        applyBav(bav.get("Venus"), pos.get("Sun"), new int[]{8,11,12});
        applyBav(bav.get("Venus"), pos.get("Moon"), new int[]{1,2,3,4,5,8,9,11,12});
        applyBav(bav.get("Venus"), pos.get("Mars"), new int[]{3,5,6,9,11,12});
        applyBav(bav.get("Venus"), pos.get("Mercury"), new int[]{3,5,6,9,11});
        applyBav(bav.get("Venus"), pos.get("Jupiter"), new int[]{5,8,9,10,11});
        applyBav(bav.get("Venus"), pos.get("Venus"), new int[]{1,2,3,4,5,8,9,10,11});
        applyBav(bav.get("Venus"), pos.get("Saturn"), new int[]{3,4,5,8,9,10,11});
        applyBav(bav.get("Venus"), asc, new int[]{1,2,3,4,5,8,9,11});

        // 7. Saturn BAV (Total 39)
        applyBav(bav.get("Saturn"), pos.get("Sun"), new int[]{1,2,4,7,8,10,11});
        applyBav(bav.get("Saturn"), pos.get("Moon"), new int[]{3,6,11});
        applyBav(bav.get("Saturn"), pos.get("Mars"), new int[]{3,5,6,10,11,12}); // Fixed
        applyBav(bav.get("Saturn"), pos.get("Mercury"), new int[]{6,8,9,10,11,12});
        applyBav(bav.get("Saturn"), pos.get("Jupiter"), new int[]{5,6,11,12});
        applyBav(bav.get("Saturn"), pos.get("Venus"), new int[]{6,11,12});
        applyBav(bav.get("Saturn"), pos.get("Saturn"), new int[]{3,5,6,11});
        applyBav(bav.get("Saturn"), asc, new int[]{1,3,4,6,10,11});

        return bav;
    }

    private static void applyBav(int[] planetBavArray, int sourceSignIndex, int[] housesFromSource) {
        for (int h : housesFromSource) {
            int targetSignIndex = (sourceSignIndex + h - 1) % 12;
            planetBavArray[targetSignIndex]++;
        }
    }
}