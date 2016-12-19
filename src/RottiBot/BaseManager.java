package RottiBot;

import RottiBot.Commands.ScoutCommand;
import RottiBot.Units.Probe;
import bwapi.*;
import bwta.Chokepoint;
import RottiBot.builds.Build;
import RottiBot.builds.Building;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class BaseManager {
    private Game game;
    private Player player;
    private TilePosition startPosition;
    private ArrayList<Probe> probes = new ArrayList<>();
    private ArrayList<UnitType> buildingQueue = new ArrayList<>();
    private ArrayList<UpgradeType> techs = new ArrayList<>();
    private UnitType nextBuilding = null;
    private BuildingPositionFinder positionFinder = null;
    private ScoutCommand scout = null;
    private List<Chokepoint> chokepoints;
    private Build build = null;
    private BasePlanner basePlanner = null;

    BaseManager(Game game, Player player, TilePosition startPosition, List<Chokepoint> chokepoints, Build build) {
        this.game = game;
        this.player = player;
        this.startPosition = startPosition;
        this.positionFinder = new BuildingPositionFinder(startPosition, game, player, chokepoints);
        this.chokepoints = chokepoints;
        this.build = build;

        for (Building building : build.getBuildings()) {
            UnitType type = building.getType();
            if (type != null) {
                System.out.println(type);
                buildingQueue.add(type);
            }
        }
        for (UpgradeType type : build.getUpgrades()) {
            if (type != null) {
                techs.add(type);
            }
        }
    }

    void update() {
        if (unitCount(UnitType.Protoss_Probe) > 5 && this.scout == null) {
            Probe p = getProbes(Probe.STATE.MINING).get(0);
            List<TilePosition> locations = game.getStartLocations();
            locations.remove(player.getStartLocation());
            this.scout = new ScoutCommand(p, locations);
        }
        if (this.scout != null) {
            this.scout.update();
        }

        for (Probe p : this.probes) {
            p.tick();
        }

        mineWithIdleProbes();
        researchTech();
        buildStuff();
        trainUnits();
    }


    void draw() {
        for (Probe probe : probes) {
            probe.draw(this.game);
        }
        if (this.basePlanner != null) {
            this.basePlanner.draw();
        }
    }

    private void issueBuildingCommand(Probe probe, UnitType building, TilePosition position) {
        probe.build(building, position);
    }

    private void researchTech() {
        for (TechType tech : build.getTechs()) {
            if (player.minerals() > tech.mineralPrice() && player.gas() > tech.gasPrice()) {
                UnitType buildingType = tech.whatResearches();
                if (buildingCount(buildingType) > 0) {
                    getUnits(buildingType).get(0).research(tech);
                }
            }
        }
        for (UpgradeType upgrade : techs) {
            if (player.minerals() > upgrade.mineralPrice() && player.gas() > upgrade.gasPrice()) {
                UnitType buildingType = upgrade.whatUpgrades();
                if (buildingCount(buildingType) > 0) {
                    getUnits(buildingType).get(0).upgrade(upgrade);
                }
            }
        }
    }

    private void trainUnits() {
        if (unitCount(UnitType.Protoss_Probe) < this.build.getMaxWorkers()) {
            Unit building = getAvailabeTrainingBuilding(UnitType.Protoss_Probe);
            if (building != null) {
                building.train(UnitType.Protoss_Probe);
            }
        }

        UnitType[] wantedUnits = new UnitType[]{UnitType.Protoss_Observer, UnitType.Protoss_High_Templar, UnitType.Protoss_Dark_Templar, UnitType.Protoss_Dragoon, UnitType.Protoss_Zealot};
        if (player.minerals() < 125) {
            return;
        }
        for (UnitType type : wantedUnits) {
            if (canBuild(type)) {
                if (type == UnitType.Protoss_Observer && unitCount(UnitType.Protoss_Observer) >= 2) {
                    continue;
                }
                if (type == UnitType.Protoss_Dark_Templar && unitCount(UnitType.Protoss_Dark_Templar) >= 2) {
                    continue;
                }
                if (type == UnitType.Protoss_High_Templar && unitCount(UnitType.Protoss_High_Templar) >= 2) {
                    continue;
                }
                Unit building = getAvailabeTrainingBuilding(type);
                if (building != null) {
                    building.train(type);
                }
            }
        }
    }

    private long unitCount(UnitType type) {
        return player.getUnits().stream().filter(u -> u.getType() == type && u.isCompleted()).count();
    }

    private Unit getAvailabeTrainingBuilding(UnitType type) {
        UnitType building = type.whatBuilds().first;
        for (Unit u : player.getUnits()) {
            if (u.getType() == building && u.isTraining() == false && u.isCompleted()) {
                return u;
            }
        }
        return null;
    }

    private void buildStuff() {

        // Build stuff
        if (nextBuilding == null && buildingQueue.size() > 0) {
            nextBuilding = buildingQueue.get(0);
            buildingQueue.remove(0);
        }

        Probe probe = getWorker();
        long constructionPylons = player.getUnits().stream().filter(u -> u.getType() == UnitType.Protoss_Pylon && u.isConstructing()).count();
        int pc = player.supplyTotal() < 50 ? 1 : 2;
        int supplyOffset = player.supplyTotal() < 50 ? 4 : 10;
        int probesConstructingPylons = this.numberOfProbesBuilding(UnitType.Protoss_Pylon);
        if (player.supplyUsed() >= (player.supplyTotal() - supplyOffset) && constructionPylons < pc && probesConstructingPylons == 0 && canBuild(UnitType.Protoss_Pylon)) {
            TilePosition pos = positionFinder.getBuildingPosition(UnitType.Protoss_Pylon, probe.getUnit());
            if (pos != null) {
                issueBuildingCommand(probe, UnitType.Protoss_Pylon, pos);
            } else {
                System.out.println("Can not place pylon!");
            }
        }

        if (nextBuilding != null) {
            if (buildingCount(UnitType.Protoss_Pylon) > 0 && numberOfProbesBuilding(nextBuilding) == 0) {
                if (canBuild(nextBuilding)) {
                    TilePosition pos = positionFinder.getBuildingPosition(nextBuilding, probe.getUnit());
                    if (pos == null && constructionPylons == 0) {
                        pos = positionFinder.getBuildingPosition(UnitType.Protoss_Pylon, probe.getUnit());
                        if (pos != null) {
                            issueBuildingCommand(probe, UnitType.Protoss_Pylon, pos);
                        } else {
                            System.out.println("Can not place pylon!");
                        }
                    } else {
                        issueBuildingCommand(probe, nextBuilding, pos);
                        nextBuilding = null;
                    }
                }
            }

        }
    }

    private int numberOfProbesBuilding(UnitType buildingType) {
        return (int) this.probes.stream().filter(p -> p.getBuildingType() == buildingType).count();
    }

    private boolean canBuild(UnitType type) {
        if (player.minerals() < type.mineralPrice() || player.gas() < type.gasPrice()) {
            return false;
        }
        if (type.isBuilding()) {
            Set<UnitType> required = type.requiredUnits().keySet();
            for (UnitType r : required) {
                if (r.isBuilding() && buildingCount(r) == 0) {
                    return false;
                }
            }
            return true;
        }

        UnitType buildingType = type.whatBuilds().first;
        return buildingCount(buildingType) > 0;
    }

    private Probe getWorker() {
        List<Probe> idleWorkers = this.getProbes(Probe.STATE.IDLE);
        if (idleWorkers.size() > 0) {
            return idleWorkers.get(0);
        }
        List<Probe> miningWorkers = this.getProbes(Probe.STATE.MINING);
        if (miningWorkers.size() > 0) {
            return miningWorkers.get(0);
        }
        return null;
    }

    private long buildingCount(UnitType type) {
        return game.getAllUnits().stream().filter(u -> u.getType() == type && u.isCompleted()).count();
    }

    private void mineWithIdleProbes() {
        List<Probe> probes = this.getProbes(Probe.STATE.IDLE);
        List<Probe> miningProbes = this.getProbes(Probe.STATE.MINING);
        List<Unit> assimilators = this.getUnits(UnitType.Protoss_Assimilator);
        for (Unit a : assimilators) {
            int c = 0;
            for (Probe p : getProbes(Probe.STATE.MINING_GAS)) {
                Unit b = p.getAssimilator();
                if (b != null && b.getID() == a.getID()) {
                    c++;
                }
            }
            if (c < 3) {
                miningProbes.get(0).mineGas(a);
            }
        }

        if (probes.size() == 0) {
            return;
        }
        List<Unit> minerals = game.neutral().getUnits().stream().filter(u -> u.getType().isMineralField()).collect(Collectors.toList());
        for (Probe probe : probes) {
            probe.mine(minerals);
        }
    }

    private List<Unit> getUnits(UnitType type) {
        return player.getUnits().stream().filter(u -> u.getType() == type && u.isCompleted()).collect(Collectors.toList());
    }

    private List<Probe> getProbes(Probe.STATE state) {
        if (state == null) {
            return this.probes;
        }
        return this.probes.stream().filter(p -> p.getState() == state && p.getUnit().isCompleted()).collect(Collectors.toList());
    }

    void recruit(Unit unit) {
        Probe probe = new Probe(unit);
        this.probes.add(probe);
    }

    void onUnitCreate(Unit unit) {
        // System.out.println(unit.getType());
        for (Probe probe : probes) {
            if (unit.getType() == probe.getBuildingType()) {
                probe.didCompleteBuilding();
                return;
            }
        }

    }

}
