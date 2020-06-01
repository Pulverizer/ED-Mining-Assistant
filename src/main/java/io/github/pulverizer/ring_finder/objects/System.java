package io.github.pulverizer.ring_finder.objects;

import io.github.pulverizer.ring_finder.utils.Allegiance;
import io.github.pulverizer.ring_finder.utils.SecurityLevel;

import java.util.HashSet;

public class System {
    private final int id;
    private final String name;
    private final SecurityLevel security;
    private Allegiance allegiance;
    private HashSet<Body> bodies = new HashSet<>();

    public System(int id, String name, String security) {
        this.id = id;
        this.name = name;
        this.security = SecurityLevel.valueOf(security);

    }

    public void setAllegiance(String string) {
        this.allegiance = Allegiance.parseString(string);
    }

    public String getName() {
        return name;
    }

    public SecurityLevel getSecurity() {
        return security;
    }

    public Allegiance getAllegiance() {
        return allegiance;
    }

    public int getId() {
        return id;
    }

    public HashSet<Body> bodies() {
        return bodies;
    }

    @Override
    public int hashCode() {
        return id;
    }
}