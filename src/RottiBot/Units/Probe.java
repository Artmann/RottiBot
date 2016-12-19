package RottiBot.Units;

import RottiBot.PositionUtil;
import bwapi.*;

import javax.rmi.CORBA.Tie;
import java.util.Collections;
import java.util.List;

public class Probe {

    public enum STATE {
        IDLE, MINING, MINING_GAS, BUILDING, SCOUTING
    }
    private Unit unit;
    private STATE state;
    private Unit assimilator = null;

    private UnitType buildingType;
    private TilePosition buildingPosition;

    public Probe(Unit unit) {
        this.unit = unit;
        this.setState(STATE.IDLE);
    }

    public void tick() {
        if (this.state == STATE.BUILDING) {
            // If the probe is standing still we should move to the build position or build the building
            Order currentOrder = this.unit.getOrder();
            if (currentOrder == Order.PlayerGuard || currentOrder == Order.MoveToMinerals || currentOrder == Order.MiningMinerals) {
                Position pos = PositionUtil.translate(this.buildingPosition.toPosition(), 16, 16);
                if (this.unit.getDistance(pos) > TilePosition.SIZE_IN_PIXELS * 3) {
                    this.unit.move(pos);
                } else {
                    this.unit.build(this.buildingType, this.buildingPosition);
                }
            }
        }
    }

    public void build(UnitType building, TilePosition position){
        this.state = STATE.BUILDING;
        this.buildingType = building;
        this.buildingPosition = position;
    }

    public void didCompleteBuilding() {
        this.buildingType = null;
        this.buildingPosition = null;
        this.state = STATE.IDLE;
    }

    public void draw(Game game) {
        game.drawTextMap(this.unit.getPosition(), this.state.toString() + " - " + this.unit.getOrder());
        if (this.buildingPosition != null) {
            game.drawCircleMap(this.buildingPosition.toPosition(), TilePosition.SIZE_IN_PIXELS, Color.Cyan, true);
        }
    }

    public void mine(List<Unit> minerals) {
        this.state = STATE.MINING;
        // Find closest mineral
        Collections.sort(minerals, (Unit a, Unit b) -> Integer.compare(unit.getDistance(a.getPosition()),unit.getDistance(b.getPosition())));
        for (Unit mineral : minerals) {
            if (unit.canGather(mineral)) {
                unit.gather(mineral);
                break;
            }
        }
    }

    public void mineGas(Unit assimilator) {
        this.assimilator = assimilator;
        this.state = STATE.MINING_GAS;
        this.unit.gather(assimilator);
    }

    public Unit getAssimilator() {
        return this.assimilator;
    }

    public STATE getState() {
        return state;
    }

    public void setState(STATE state) {
        this.state = state;
    }

    public Unit getUnit() {
        return unit;
    }

    public UnitType getBuildingType() {
        return buildingType;
    }
}
