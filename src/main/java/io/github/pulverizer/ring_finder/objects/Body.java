package io.github.pulverizer.ring_finder.objects;

import io.github.pulverizer.ring_finder.utils.ReserveLevel;

import java.util.ArrayList;

public class Body {

    private final int id;
    private final String name;
    private final int distance;
    private ReserveLevel reserveLevel = ReserveLevel.NULL;

    private final ArrayList<Belt> belts = new ArrayList<>();

    public Body(int id, String name, int distance) {
        this.id = id;
        this.name = name;
        this.distance = distance;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDistance() {
        return distance;
    }

    public boolean hasAsteroids() {
        return !belts.isEmpty();
    }

    public ReserveLevel getReserveLevel() {
        return reserveLevel;
    }

    public void setReserveLevel(String reserveLevel) {
        this.reserveLevel = ReserveLevel.valueOf(reserveLevel);
    }

    public ArrayList<Belt> belts() {
        return belts;
    }

    @Override
    public int hashCode() {
        return id;
    }
}