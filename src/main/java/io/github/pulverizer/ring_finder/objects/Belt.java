package io.github.pulverizer.ring_finder.objects;

import io.github.pulverizer.ring_finder.utils.BeltType;

public class Belt {
    private final String name;
    private final BeltType type;

    private final float density;
    private final float sizeDensityIndex;

    public Belt(String name, String type, float mass, int innerRadius, int outerRadius) {
        this.name = name;
        this.type = BeltType.parseString(type);

        float outerArea = (float) (Math.PI * (float) Math.pow(outerRadius, 2));
        float innerArea = (float) (Math.PI * (float) Math.pow(innerRadius, 2));

        float area = outerArea - innerArea;
        density = mass / area;
        sizeDensityIndex = (mass * density) / 1e9f;
    }

    public String getName() {
        return name;
    }

    public BeltType getType() {
        return type;
    }

    public float getDensity(){
        return density;
    }

    public float getSizeDensityIndex(){
        return sizeDensityIndex;
    }
}
