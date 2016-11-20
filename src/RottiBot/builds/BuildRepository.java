package RottiBot.builds;

import bwapi.Race;
import com.google.gson.Gson;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class BuildRepository {

    private ArrayList<BuildInfo> builds;

    public BuildRepository() {
        builds = new ArrayList<>();
        builds.add(new BuildInfo("5-gate-goons", Race.Zerg));
        builds.add(new BuildInfo("dragoon-dts", Race.Terran));
        builds.add(new BuildInfo("4-gate-goons", Race.Protoss));
    }

    public Build find(String name) {
        String data = readFile(name);
        try {
            return load(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Build getBuildFor(String enemyName, Race enemyRace) {
        Build build = getBuildForPlayer(enemyName);
        if (build != null) {
            return build;
        }
        return getBuildForRace(enemyRace);
    }

    private Build getBuildForRace(Race enemyRace) {
        List<BuildInfo> list = this.builds.stream().filter(b -> b.getRace() == enemyRace).collect(Collectors.toList());
        BuildInfo info = randomBuildInfo(list);
        if (info == null) {
            info = randomBuildInfo(this.builds);
        }
        return find(info.getName());
    }

    private BuildInfo randomBuildInfo(List<BuildInfo> list) {
        int rnd = new Random().nextInt(list.size());
        return list.get(rnd);
    }

    private Build getBuildForPlayer(String name) {
        return null;
    }

    private Build load(String data) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(data, Build.class);
    }

    private String readFile(String name) {
        BufferedReader br;
        InputStream is = getClass().getResourceAsStream("/"+name+".json");
        if (is == null) {
            FileReader in = null;
            try {
                in = new FileReader("builds/"+name+".json");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            br = new BufferedReader(in);
        } else {
            br = new BufferedReader(new InputStreamReader(is));
        }
        return br.lines().collect(Collectors.joining());
    }
}
