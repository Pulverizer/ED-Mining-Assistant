package io.github.pulverizer.ring_finder.utils;

public enum BeltType {
    NULL,
    Rocky,
    Icy,
    Metal_Rich,
    Metallic;

    public static BeltType parseString(String string) {
        switch (string) {
            case "Rocky":
                return Rocky;
            case "Icy":
                return Icy;
            case "Metal Rich":
                return Metal_Rich;
            case "Metallic":
                return Metallic;
            default:
                return NULL;
        }
    }
}
