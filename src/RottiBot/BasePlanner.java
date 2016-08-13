package RottiBot;


import bwapi.*;
import bwapi.Color;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class BasePlanner {
    private Game game;
    private TilePosition startPosition;
    private List<Chokepoint> chokepoints;
    private ArrayList<Pair<UnitType, TilePosition>> list;
    private ArrayList<UnitType> buildings;
    private ArrayList<TilePosition> positions;

    BasePlanner(Game game, TilePosition startPosition, List<Chokepoint> chokepoints, ArrayList<UnitType> buildings) {
        this.game = game;
        this.startPosition = startPosition;
        this.chokepoints = chokepoints;
        this.buildings = buildings;

        BaseLocation baseLocation = BWTA.getNearestBaseLocation(startPosition);
        Polygon polygon = baseLocation.getRegion().getPolygon();
        int max = baseLocation.getRegion().getMaxDistance();
        positions = new ArrayList<>();
        for (int i = -max; i < max; i++) {
            for (int j = -max; j < max; j++) {
                int x = startPosition.getX() + i;
                int y = startPosition.getY() + j;
                TilePosition pos = new TilePosition(x, y);
                if (polygon.isInside(pos.toPosition())) {
                    positions.add(pos);
                }
            }
        }

        list = new ArrayList<>();
        list.add(new Pair<>(UnitType.Protoss_Gateway, startPosition));
    }



    ArrayList<Pair<UnitType, TilePosition>> getPlan() {
        return this.list;
    }

    void draw() {
        for (Pair<UnitType, TilePosition> p : list) {
            UnitType type = p.first;
            TilePosition pos = p.second;
            TilePosition pos2 = new TilePosition(pos.getX() + type.tileWidth(), pos.getY() + type.tileHeight());
            Color c = (type == UnitType.Protoss_Pylon) ? Color.Blue : Color.Red;
            game.drawBoxMap(pos.toPosition(), pos2.toPosition(), c);
        }
        for (TilePosition pos : positions) {
            Position p = pos.toPosition();
            game.drawBoxMap(p.getX(), p.getY(), p.getX() + TilePosition.SIZE_IN_PIXELS, p.getY() + TilePosition.SIZE_IN_PIXELS, Color.Yellow);
        }
    }
}
