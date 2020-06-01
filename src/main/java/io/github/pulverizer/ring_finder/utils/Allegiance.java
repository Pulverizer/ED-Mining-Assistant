package io.github.pulverizer.ring_finder.utils;

public enum Allegiance {
    NULL,
    Federation,
    Pilots_Federation,
    Empire,
    Alliance,
    Independent;

    public static Allegiance parseString(String string) {
        switch (string) {
            case "Independent":
                return Independent;
            case "Alliance":
                return Alliance;
            case "Empire":
                return Empire;
            case "Federation":
                return Federation;
            case "Pilots Federation":
                return Pilots_Federation;
            default:
                return NULL;
        }
    }
}
