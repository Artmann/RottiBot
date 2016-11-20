package RottiBot.builds;

import bwapi.TechType;
import bwapi.UpgradeType;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class Build {
    private String name;
    private int maxWorkers;
    private int attackAt;
    private Building[] buildings;
    private String[] upgrades;
    private String[] techs;

    public Build() {}

    public UpgradeType[] getUpgrades() {
        UpgradeType[] list = new UpgradeType[upgrades.length];
        for (int i = 0; i < list.length; i++) {
            list[i] = getUpgradeType(upgrades[i]);
        }
        return list;
    }

    public TechType[] getTechs() {
        TechType[] list = new TechType[techs.length];
        for (int i = 0; i < list.length; i++) {
            list[i] = getTechType(techs[i]);
        }
        return list;
    }

    private TechType getTechType(String name) {
        switch (name) {
            case "Psionic Storm": return TechType.Psionic_Storm;
        }
        return null;
    }


    private UpgradeType getUpgradeType(String name) {
        switch (name) {
            case "Singularity Charge": return UpgradeType.Singularity_Charge;
            case "Leg Enhancements": return UpgradeType.Leg_Enhancements;
            case "Weapons": return UpgradeType.Protoss_Ground_Weapons;
            case "Armor": return UpgradeType.Protoss_Ground_Armor;
            case "Khaydarin Amulet": return UpgradeType.Khaydarin_Amulet;
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public int getMaxWorkers() {
        return maxWorkers;
    }

    public int getAttackAt() {
        return attackAt;
    }

    public Building[] getBuildings() {
        return buildings;
    }
}
