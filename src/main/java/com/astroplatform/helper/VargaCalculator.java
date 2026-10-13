package com.astroplatform.helper;

public class VargaCalculator {
    public static int getVargaSign(double absoluteDegree, int div) {
        int sign = (int) (absoluteDegree / 30) + 1;
        double degreeInSign = absoluteDegree % 30;
        double epsilon = 1e-9; // Prevents boundary truncation

        int p = (int) Math.floor(((degreeInSign * div) / 30.0) + epsilon);
        if (p >= div) p = div - 1;

        switch (div) {
            case 1: return sign;
            case 2:
                if (sign % 2 != 0) return (degreeInSign < 15) ? 5 : 4;
                else return (degreeInSign < 15) ? 4 : 5;
            case 3: return ((sign - 1 + (p * 4)) % 12) + 1;
            case 4: return ((sign - 1 + (p * 3)) % 12) + 1;
            case 7:
                int start7 = (sign % 2 != 0) ? sign : sign + 6;
                return ((start7 - 1 + p) % 12) + 1;
            case 9: return (int) ((((absoluteDegree + epsilon) * 9) % 360) / 30) + 1;
            case 10:
                int start10 = (sign % 2 != 0) ? sign : sign + 8;
                return ((start10 - 1 + p) % 12) + 1;
            case 12: return ((sign - 1 + p) % 12) + 1;
            case 16:
                int start16 = (sign % 3 == 1) ? 1 : (sign % 3 == 2) ? 5 : 9;
                return ((start16 - 1 + p) % 12) + 1;
            case 20:
                int start20 = (sign % 3 == 1) ? 1 : (sign % 3 == 2) ? 9 : 5;
                return ((start20 - 1 + p) % 12) + 1;
            case 24:
                int start24 = (sign % 2 != 0) ? 5 : 4;
                return ((start24 - 1 + p) % 12) + 1;
            case 27:
                int start27 = (sign % 4 == 1) ? 1 : (sign % 4 == 2) ? 4 : (sign % 4 == 3) ? 7 : 10;
                return ((start27 - 1 + p) % 12) + 1;
            case 30:
                double deg = degreeInSign + epsilon;
                if (sign % 2 != 0) {
                    if (deg < 5) return 1; if (deg < 10) return 11; if (deg < 18) return 9; if (deg < 25) return 3; return 7;
                } else {
                    if (deg < 5) return 2; if (deg < 12) return 6; if (deg < 20) return 12; if (deg < 25) return 10; return 8;
                }
            case 40:
                int start40 = (sign % 2 != 0) ? 1 : 7;
                return ((start40 - 1 + p) % 12) + 1;
            case 45:
                int start45 = (sign % 3 == 1) ? 1 : (sign % 3 == 2) ? 5 : 9;
                return ((start45 - 1 + p) % 12) + 1;
            case 60: return ((sign - 1 + p) % 12) + 1;
            default: return sign;
        }
    }
}