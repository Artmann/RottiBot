package RottiBot;

import bwapi.*;
import bwta.BWTA;
import bwta.Chokepoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class BuildingPositionFinder {
    TilePosition startPosition;
    Game game;
    Player player;
    ArrayList<TilePosition> pylonPositions = new ArrayList<>();
    List<Chokepoint> chokepoints;

    BuildingPositionFinder(TilePosition startPosition, Game game, Player player, List<Chokepoint> chokepoints) {
        this.startPosition = startPosition;
        this.game = game;
        this.player = player;
        this.chokepoints = chokepoints;
        findInitialPylonPositions();
    }

    private void findInitialPylonPositions() {
        int mx = 10;
        int my = 10;
        int x = 0;
        int y = 0;
        int dx = 0;
        int dy = -1;
        for (int i = 0; i < Math.pow(Math.max(mx, my), 2); i++) {
            if (-mx / 2 < x && x <= mx / 2 && -my / 2 < y && y < my / 2) {
                TilePosition pos = new TilePosition(startPosition.getX() + x, startPosition.getY() + y);
                this.pylonPositions.add(pos);
            }
            if (x == y || (x < 0 && x == -y) || (x > 0 && x == 1 - y)) {
                int t = dx;
                dx = -dy;
                dy = t;
            }
            x += dx;
            y += dy;
        }
    }

    private TilePosition getPylonPosition(Unit unit) {
        List<Unit> pylons = player.getUnits().stream().filter(u -> u.getType() == UnitType.Protoss_Pylon).collect(Collectors.toList());
        if (pylons.size() == 0) {
            int x = startPosition.getX();
            int y = startPosition.getY();
            for (int i = -12; i < 12; i += 2) {
                for (int j = -12; j < 12; j += 2) {
                    if (i > 5 && j > 5) {
                        TilePosition pos = new TilePosition(x + i, y + j);
                        if (game.canBuildHere(pos, UnitType.Protoss_Pylon, unit, false)) {
                            return pos;
                        }
                    }
                }
            }
        }
        double baseAngle = angle(this.startPosition, this.chokepoints.get(0).getCenter().toTilePosition());
        System.out.println(baseAngle);
        for (int p = pylons.size() - 1; p >= 0; p--) {
            for (int i = -6; i <= 6; i++) {
                for (int j = -6; j <= 6; j += 6) {
                    Unit pylon = pylons.get(p);
                    for (int d = 0; i < 360; d += 10) {
                        for (int r = 6; r > 0; r--) {
                            TilePosition pos = positionOnCircle(pylon.getTilePosition(), baseAngle + d, 6);
                          //  System.out.println("CIRCLE:" + pos);
                            if (game.canBuildHere(pos, UnitType.Protoss_Pylon, unit, false)) {
                                return pos;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private double angle(TilePosition a, TilePosition b) {
        int dx = a.getX() - b.getX();
        int dy = a.getY() - b.getY();
        double radians = Math.atan2(dy, dx);
        return Math.toDegrees(radians);
    }

    private TilePosition positionOnCircle(TilePosition center, double degrees, int radius) {
        double angle = Math.toRadians(degrees);
        double x = radius * Math.cos(angle) + center.getX();
        double y = radius * Math.sin(angle) + center.getY();
        return new TilePosition((int)Math.round(x), (int)Math.round(y));
    }

    TilePosition getBuildingPosition(UnitType building, Unit unit) {
        if (building == UnitType.Protoss_Pylon) {
            return this.getPylonPosition(unit);
        }
        if (building == UnitType.Protoss_Assimilator) {
            return this.getAssimilatorPosition(unit);
        }
        if (building == UnitType.Protoss_Nexus) {
            return this.getNexusPosition(unit);
        }

        List<Unit> pylons = player.getUnits().stream().filter(u -> u.getType() == UnitType.Protoss_Pylon).collect(Collectors.toList());
        for (Unit pylon : pylons) {
            TilePosition pos = pylon.getTilePosition();
            for (int i = -6; i <= 6; i += 2) {
                for (int j = -6; j <= 6; j += 2) {
                    int nx = pos.getX() + i;
                    int ny = pos.getY() + j;
                    TilePosition newPos = new TilePosition(nx, ny);
                    if (game.canBuildHere(newPos, building)) {
                        return newPos;
                    }
                }
            }
        }
        return null;
    }

    private TilePosition getNexusPosition(Unit unit) {
        List<TilePosition> locations = BWTA.getBaseLocations().stream().map(b -> b.getTilePosition()).collect(Collectors.toList());
        TilePosition up = unit.getTilePosition();
        Collections.sort(locations, (TilePosition a, TilePosition b) -> Double.compare(BWTA.getGroundDistance(a, up), BWTA.getGroundDistance(b, up)));
        int nexusCount = (int) player.getUnits().stream().filter(u -> u.getType() == UnitType.Protoss_Nexus).count();
        return locations.get(nexusCount);
    }

    private TilePosition getAssimilatorPosition(Unit unit) {
        List<Unit> geysers = game.getNeutralUnits().stream().filter(u -> u.getType() == UnitType.Resource_Vespene_Geyser).collect(Collectors.toList());
        Unit c = null;
        int dist =  0;
        for (Unit u : geysers) {
            int d = unit.getDistance(u.getPosition());
            if (d < dist || dist == 0) {
                dist = d;
                c = u;
            }
        }
        return c.getTilePosition();
    }

}
