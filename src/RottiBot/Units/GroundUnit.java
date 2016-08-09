package RottiBot.Units;

import bwapi.Position;
import bwapi.Unit;

public class GroundUnit {
    public enum STATE { IDLE, RUN, FIGHT, ATTACK_MOVE };
    private Unit unit;
    private Unit target;
    private Position destination;
    private boolean inCombat = false;
    private boolean inBase = true;
    private STATE state = STATE.IDLE;

    public GroundUnit(Unit unit) {
        this.unit = unit;
    }

    public void attackMove(Position position) {
        if (position == destination) {
            return;
        }
        if (this.unit.canAttackMove()) {
            this.unit.attack(position);
        } else {
            this.unit.move(position);
        }
        this.destination = position;
    }


    public void setTarget(Unit target) {
        this.target = target;
    }

    public void setDestination(Position destination) {
        this.destination = destination;
    }

    public Unit getUnit() {
        return unit;
    }

    public Unit getTarget() {
        return target;
    }

    public Position getDestination() {
        return destination;
    }

    public boolean isInCombat() {
        return inCombat;
    }

    public void setInCombat(boolean inCombat) {
        this.inCombat = inCombat;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public boolean isInBase() {
        return inBase;
    }

    public void setInBase(boolean inBase) {
        this.inBase = inBase;
    }

    public STATE getState() {
        return state;
    }

    public void setState(STATE state) {
        this.state = state;
    }
}

