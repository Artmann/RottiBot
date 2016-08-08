package co.artmann.builds;

import bwapi.UnitType;

public class Building {
    private String name;
    private boolean wall;

    public UnitType getType() {
        switch (this.name) {
            case "Gateway": return UnitType.Protoss_Gateway;
            case "Assimilator": return UnitType.Protoss_Assimilator;
            case "Cybernetics Core": return UnitType.Protoss_Cybernetics_Core;
            case "Nexus": return UnitType.Protoss_Nexus;
            case "Citadel of Adun": return UnitType.Protoss_Citadel_of_Adun;
            case "Robotics Facility": return UnitType.Protoss_Robotics_Facility;
            case "Templar Archives": return UnitType.Protoss_Templar_Archives;
        }
        return null;
    }
    public boolean isWall() {
        return wall;
    }
}
