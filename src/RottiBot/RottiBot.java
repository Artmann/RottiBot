package RottiBot;

import bwapi.*;
import bwta.BWTA;
import bwta.Chokepoint;
import co.artmann.builds.Build;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RottiBot extends DefaultBWListener {
    private boolean isDevelopment = true;

    private Mirror mirror = new Mirror();
    private Game game;
    private Player self;
    private Player enemyPlayer;
    private int buildCounter = 0;

    private BaseManager baseManager = null;
    private ArmyManager armyManager = null;
    UnitType[] armyTypes = null;
    private List<Chokepoint> chokepoints = null;

    private Build build;
    public int attackTiming;

    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }
    @Override
    public void onUnitCreate(Unit unit) {
//        System.out.println("New unit discovered " + unit.getType());
        UnitType type = unit.getType();
        if (type == UnitType.Protoss_Probe) {
            baseManager.recruit(unit);
        }
        if (type.isBuilding()) {
            baseManager.onUnitCreate(unit);
        }

        if (unit.getPlayer().getID() == self.getID()) {
            for (UnitType t : this.armyTypes) {
                if (unit.getType() == t ) {
                    System.out.println("Recruited: "+unit.getType());
                    armyManager.recruit(unit);
                }
            }
        }

    }

    @Override
    public void onUnitDiscover(Unit unit) {
        if (unit.getType().isBuilding() && unit.getPlayer().getID() == enemyPlayer.getID()) {
            armyManager.foundEnemyBuilding(unit);
        }
    }

    @Override
    public void onUnitMorph(Unit unit) {
        if (unit.getType().isBuilding()) {
            baseManager.onUnitCreate(unit);
        }
    }

    @Override
    public void onUnitDestroy(Unit unit) {
        if (unit.getPlayer().getID() == self.getID()) {
            for (UnitType t : armyTypes) {
                if (unit.getType() == t ) {
                    //System.out.println("Unit Died " + unit.getType());
                    armyManager.unitDied(unit);
                }
            }
        }
    }

    @Override
    public void onStart() {
        isDevelopment = true;
        game = mirror.getGame();
        game.enableFlag(1);
        self = game.self();
        enemyPlayer = game.enemy();
        this.armyTypes = new UnitType[] {
            UnitType.Protoss_Dragoon, UnitType.Protoss_Zealot, UnitType.Protoss_Dark_Templar ,
                UnitType.Protoss_High_Templar, UnitType.Protoss_Observer
        };

        Race enemyRace = enemyPlayer.getRace();
        this.build = null;
        try {
            this.build = this.getBuild(enemyRace);
        } catch (IOException e) {
            System.out.println("COULD NOT LOAD BUILD");
            e.printStackTrace();
        }
        if (this.build == null) {
            System.out.println("NO BUILD; Exiting");
            System.exit(1);
        }

        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");

        TilePosition startPosition = null;
        for (Unit unit : game.getAllUnits()) {
            if (unit.getType() == UnitType.Protoss_Nexus) {
                startPosition = unit.getTilePosition();
            }
        }

        chokepoints = BWTA.getChokepoints();
        final TilePosition pos = startPosition;
        Collections.sort(chokepoints,
                (Chokepoint a, Chokepoint b) ->
                        Double.compare(
                                BWTA.getGroundDistance(pos, a.getCenter().toTilePosition()),
                                BWTA.getGroundDistance(pos, b.getCenter().toTilePosition())
                        ));


        this.baseManager = new BaseManager(this.game, this.self, startPosition, chokepoints, this.build);
        this.armyManager = new ArmyManager(this, this.game, this.self, enemyPlayer, chokepoints, this.build);
    }

    private void saveGameData(boolean isWinner) {
        String name = this.game.enemy().getName();
        String build = this.build.getName();
        String winner = isWinner ? "1" : "0";

        String[] row = new String[] { name, build, this.attackTiming+"", winner};
        String output = String.join(",", row) + "\n";
        try {
            Path path = Paths.get("Starcraft/bwapi-data/write/match-history.csv");
            File file = path.toFile();
            if (!file.exists()) { Files.createFile(path); }
            Files.write(path, output.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnd(boolean b) {
        System.out.println("GAME ENDED");
        this.saveGameData(b);
    }

    @Override
    public void onFrame() {
        if (this.buildCounter++ >= 10) {
            this.baseManager.update();
            this.armyManager.update();
            this.buildCounter = 0;
        }
        if (isDevelopment) {
            armyManager.draw();
            baseManager.draw();
        }
    }


    public static void main(String[] args) {
        new RottiBot().run();

    }

    public Build getBuild(Race enemyRace) throws IOException {
        Build build = null;
        String name;
        if (enemyRace == Race.Terran) {
            name = "dragoon-dts";
        }  else if (enemyRace == Race.Zerg) {
            name = "5-gate-goons";
        } else {
            name = "4-gate-goons";
        }

        InputStream is = null;
        BufferedReader br;
        is = getClass().getResourceAsStream("/"+name+".json");
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
        String data = br.lines().collect(Collectors.joining());
        System.out.println(data);

        build = Build.load(data);
        return build;
    }
}