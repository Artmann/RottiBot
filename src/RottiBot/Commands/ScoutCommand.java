package RottiBot.Commands;

import RottiBot.Units.Probe;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;

import java.util.List;

public class ScoutCommand {
    private Probe probe;
    private List<TilePosition> locations;
    private Position nextPosition = null;

    public ScoutCommand(Probe probe, List<TilePosition> locations) {
        this.probe = probe;
        this.locations = locations;
        probe.setState(Probe.STATE.SCOUTING);
        nextLocation();
    }

    private void nextLocation() {
        if (locations.size() > 0) {
            nextPosition = locations.get(0).toPosition();
            locations.remove(0);
        }
    }

    public void update() {
        Unit unit = probe.getUnit();
        if (unit.getDistance(nextPosition) > TilePosition.SIZE_IN_PIXELS * 2.5) {
            unit.move(nextPosition);
        } else {
            nextLocation();
        }
    }
}
