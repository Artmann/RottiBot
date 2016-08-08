package RottiBot.Commands;

import RottiBot.Units.Probe;
import bwapi.TilePosition;
import bwapi.UnitType;

public class BuildCommand {
    public enum STATE { PENDING, DONE, FAILED }
    private Probe probe;
    private UnitType building;
    private TilePosition position;
    private STATE state = STATE.PENDING;

    public BuildCommand(Probe probe, UnitType building, TilePosition position) {
        this.probe = probe;
        this.building = building;
        this.position = position;
        probe.setState(Probe.STATE.BUILDING);
    }

    public void update() {
        if (this.state == STATE.DONE) {
            return;
        }
        int distance = this.probe.getUnit().getDistance(position.toPosition());
        if (distance >= TilePosition.SIZE_IN_PIXELS * 3) {
            this.probe.moveTo(position);
        } else {
            this.probe.build(building, position);
        }
    }

    public synchronized void didPlaceStructure() {
        probe.setState(Probe.STATE.IDLE);
        this.state = STATE.DONE;
    }

    public void didComplete() {}

    public UnitType getBuilding() {
        return building;
    }

    public TilePosition getPosition() {
        return position;
    }
}
