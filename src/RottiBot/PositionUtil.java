package RottiBot;

import bwapi.Position;

public class PositionUtil {
    public static Position translate(Position pos, int dx, int dy) {
        return new Position(pos.getX() + dx, pos.getY() + dy);
    }
}
