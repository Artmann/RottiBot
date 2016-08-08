package RottiBot.Units;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

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

    public Probe(Unit unit) {
        this.unit = unit;
        this.setState(STATE.IDLE);
    }

    public boolean build(UnitType building, TilePosition position){
        this.state = STATE.BUILDING;
        return this.unit.build(building, position);
    }

    public boolean moveTo(TilePosition position) {
        return this.unit.move(position.toPosition());
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
}
