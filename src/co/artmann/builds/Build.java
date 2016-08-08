package co.artmann.builds;

import bwapi.UpgradeType;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;


public class Build {
    private String name;
    private int maxWorkers;
    private int attackAt;
    private Building[] buildings;
    private String[] upgrades;

    public Build() {}

    public UpgradeType[] getUpgrades() {
        UpgradeType[] list = new UpgradeType[upgrades.length];
        for (int i = 0; i < list.length; i++) {
            list[i] = getUpgradeType(upgrades[i]);
        }
        return list;
    }

    private UpgradeType getUpgradeType(String name) {
        switch (name) {
            case "Singularity Charge": return UpgradeType.Singularity_Charge;
            case "Leg Enhancements": return UpgradeType.Leg_Enhancements;
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

    public static Build load(String data) throws IOException {
        //String text = readFile(path);

        Gson gson = new Gson();
        Build build = gson.fromJson(data, Build.class);
        return build;
    }

    private static String readFile(String path) throws IOException {
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        return new String(data, "UTF-8");
    }
}
