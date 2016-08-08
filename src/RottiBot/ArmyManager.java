package RottiBot;

import RottiBot.Units.GroundUnit;
import bwapi.*;
import bwta.BWTA;
import bwta.Chokepoint;
import co.artmann.builds.Build;
import com.sun.org.apache.xpath.internal.operations.Or;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class ArmyManager {
    private Game game;
    private Player player;
    private Player enemyPlayer;

    private ArrayList<GroundUnit> units = new ArrayList<>();
    private ArrayList<Position> enemyBuildings = new ArrayList<>();

    private enum STATE { DEFFENSIVE, AGGRESIVE };
    private STATE state;
    private Position enemyBase = null;
    private List<Chokepoint> chokepoints;
    private Position rendezvousPoint = null;
    private Build build = null;

    ArmyManager(Game game, Player player, Player enemyPlayer, List<Chokepoint> chokepoints, Build build) {
        this.game = game;
        this.player = player;
        this.state = STATE.DEFFENSIVE;
        this.enemyPlayer = enemyPlayer;
        this.chokepoints = chokepoints;
        this.rendezvousPoint = chokepoints.get(1).getCenter();
        this.build = build;
    }

    void update() {
        int screenSize = 20 * TilePosition.SIZE_IN_PIXELS;
        for (Unit enemy : getVisibleEnemyUnits()) {
            for (GroundUnit gu : getUnits()) {
                boolean ic = false;
                boolean ib = false;
                if (gu.getUnit().getDistance(player.getStartLocation().toPosition()) < screenSize
                || gu.getUnit().getDistance(rendezvousPoint) < screenSize) {
                    ib = true;
                }
                if (gu.getUnit().getDistance(enemy) <= screenSize) {
                    ic = true;
                }
                gu.setInCombat(ic);
                gu.setInBase(ib);
            }
        }

        List<GroundUnit> combatGroundUnits = getUnits().stream().filter(unit -> unit.isInCombat()).collect(Collectors.toList());
        List<Unit> combatUnits = combatGroundUnits.stream().map(u -> u.getUnit()).collect(Collectors.toList());

        if (state == STATE.DEFFENSIVE && getVisibleEnemyUnits().size() > 0) {
            for (GroundUnit groundUnit : combatGroundUnits) {
                if (groundUnit.isInCombat()) {
                    groundUnit.setState(GroundUnit.STATE.FIGHT);
                }
            }
        }

        if (state == STATE.AGGRESIVE) {
            for (GroundUnit groundUnit : getUnits()) {
                if (groundUnit.isInBase()) {
                    if (groundUnit.isInCombat()) {
                        groundUnit.setState(GroundUnit.STATE.FIGHT);
                    } else {
                        groundUnit.setState(GroundUnit.STATE.ATTACK_MOVE);
                    }
                } else {
                    if (strength(combatUnits) > strength(getVisibleEnemyUnits())) {
                        groundUnit.setState(GroundUnit.STATE.FIGHT);
                    } else {
                        if (groundUnit.isInCombat()) {
                            groundUnit.setState(GroundUnit.STATE.RUN);
                        }
                    }
                }
            }
        }

        for (GroundUnit groundUnit : getUnits()) {
            Unit u = groundUnit.getUnit();
            if (groundUnit.getState() == GroundUnit.STATE.ATTACK_MOVE) {
                if (u.getOrder() != Order.AttackMove) {
                    u.attack(enemyBase);
                }
            }
            if (groundUnit.getState() == GroundUnit.STATE.FIGHT) {
                if (u.getOrder() == Order.Move || u.getOrder() == Order.PlayerGuard) {
                    if (getVisibleEnemyUnits().size() > 0) {
                        Unit e = getVisibleEnemyUnits().get(0);
                        u.attack(e.getPosition());
                    } else {
                        groundUnit.setState(this.state == STATE.DEFFENSIVE ? GroundUnit.STATE.IDLE : GroundUnit.STATE.ATTACK_MOVE);
                    }
                }
            }
            if (groundUnit.getState() == GroundUnit.STATE.RUN) {
                if (u.getOrder() != Order.Move) {
                    u.move(rendezvousPoint);
                }
            }
        }

//
//        if (this.state == STATE.AGGRESIVE) {
//            if (strength(combatUnits) > strength(getVisibleEnemyUnits())) {
//                if (enemyBase != null) {
//                    for (GroundUnit gu : getUnits()) {
//                        gu.attackMove(enemyBase);
//                    }
//                }
//            } else {
//                for (GroundUnit gu : combatGroundUnits) {
//                    gu.getUnit().move(rendezvousPoint);
//                }
//            }
//        }

        int attackAt = this.build.getAttackAt() > 0 ? this.build.getAttackAt() : 30;
        for (Unit u : player.getUnits()) {
            if (u.getType() == UnitType.Protoss_Gateway && u.isCompleted()) {
                u.setRallyPoint(rendezvousPoint);
            }
        }

        if (enemyBuildings.size() > 0 && enemyBase == null) {
            enemyBase = enemyBuildings.get(0);
        }
        List<Unit> enemies = getEnemyUnits();
        if (state == STATE.DEFFENSIVE) {
            if (strength() >= attackAt) {
                this.state = STATE.AGGRESIVE;
            }
        } else {

//            if (enemies.size() > 0) {
//                System.out.println("FOUND ENNEMIES");
//                for (Unit unit : units) {
//                    Unit target = getClosestEnemy(enemies, unit);
//                    unit.attack(target.getPosition());
//                    System.out.println(target.getType());
//                }
//                return;
//            }

//            if (enemyBase != null) {
//                for (GroundUnit unit : units) {
//                    unit.attackMove(enemyBase);
//                }
//            }
        }
    }

    public void draw() {
        game.drawTextScreen(10, 20 , "Mode: "+this.state);
        game.drawTextScreen(10, 40 , "Enemy Building Count: "+this.enemyBuildings.size());
        game.drawTextScreen(10, 60 , "Army Size: "+this.units.size());
        game.drawTextScreen(10, 80 , "Army Strength: "+this.strength());
        game.drawTextScreen(10, 100 , "Enemies: "+this.getVisibleEnemyUnits().size());
        game.drawTextScreen(10, 120 , "Enemy Strength: "+this.strength(getVisibleEnemyUnits()));

        if (enemyBase != null) {
            game.drawCircleMap(enemyBase, 20, Color.Red);
        }
        for (GroundUnit gu : units) {
            game.drawTextMap(gu.getUnit().getPosition(), gu.getState().toString() +"-"+ gu.getUnit().getOrder());
        }
    }

    private List<Unit> getEnemyUnits() {
        return enemyPlayer.getUnits();

    }

    private List<Unit> getVisibleEnemyUnits() {
        return getEnemyUnits().stream().filter(unit -> unit.getType().isBuilding() == false && unit.isVisible()).collect(Collectors.toList());
    }

    private Unit getClosestEnemy(List<Unit> enemies, Unit unit) {
        for (Unit enemy : enemies) {
            if (unit.getDistance(enemy.getPosition()) < TilePosition.SIZE_IN_PIXELS * 6) {
                return enemy;
            }
        }
        return enemies.get(0);
    }

    private int strength() {
        List<Unit> units = getUnits().stream().map(u -> u.getUnit()).collect(Collectors.toList());
        return this.strength(units);
    }

    private int strength(List<Unit> units) {
        int strength = 0;
        for (Unit unit : units) {
            UnitType type = unit.getType();
            if (type == UnitType.Protoss_Dragoon || type == UnitType.Protoss_Dark_Templar || type == UnitType.Terran_Siege_Tank_Tank_Mode || type == UnitType.Terran_Siege_Tank_Siege_Mode) {
                strength += 2;
            } else {
                strength += 1;
            }
        }
        return strength;
    }

    private List<GroundUnit> getUnits() {
        return this.units.stream().filter(u -> u.getUnit().isCompleted()).collect(Collectors.toList());
    }

    void recruit(Unit u) {
        GroundUnit gu = new GroundUnit(u);
        this.units.add(gu);
    }

    void unitDied(Unit unit) {
        GroundUnit groundUnit = null;
        for (GroundUnit gu : units) {
            if (gu.getUnit() == unit) {
                groundUnit = gu;
            }
        }
        if (groundUnit != null) {
            this.units.remove(groundUnit);
        }
    }

    void foundEnemyBuilding(Unit unit) {
        Position pos = unit.getPosition();
        boolean isNew = true;
        for (Position p : enemyBuildings) {
            if (p == pos) {
                isNew = false;
                break;
            }
        }
        if (isNew) {
            this.enemyBuildings.add(pos);
            System.out.println("FOUND ENEMY " + pos);
        }
    }


}
