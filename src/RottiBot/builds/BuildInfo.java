package RottiBot.builds;

import bwapi.Race;

public class BuildInfo {
    private String name;
    private Race race;

    public BuildInfo(String name, Race race) {
        this.name = name;
        this.race = race;
    }

    public String getName() {
        return name;
    }

    public Race getRace() {
        return race;
    }
}
