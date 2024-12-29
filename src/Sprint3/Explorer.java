package Sprint3;

import java.util.Random;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Collections;

public class Explorer {

    static final Random rng = new Random(2609);

    static final Direction[] directions = {
                    Direction.NORTH,
                    Direction.NORTHEAST,
                    Direction.EAST,
                    Direction.SOUTHEAST,
                    Direction.SOUTH,
                    Direction.SOUTHWEST,
                    Direction.WEST,
                    Direction.NORTHWEST,
            };

    private static MapLocation stuckLoc = null;
    private static Direction lastMoveable = null;
    private static boolean lastClockwise = false;

    private static boolean walkable(RobotController rc, Direction dir) {
        if (dir == null || rc.getLocation() == null)
            return false;
        try {
            if (rc.canMove(dir)) {
                rc.move(dir);
                return true;
            } else if (rc.canFill(rc.getLocation().add(dir))) {
                rc.fill(rc.getLocation().add(dir));
                return true;
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Handle the exception (e.g., log it)
        }
        return false;
    }

    private static boolean moveable(RobotController rc, Direction dir) {
        if (dir == null || rc.getLocation() == null)
            return false;
        try {
            if (rc.canMove(dir)) {
                return true;
            } else if (rc.canFill(rc.getLocation().add(dir))) {
                return true;
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Handle the exception (e.g., log it)
        }
        return false;
    }

    public static ArrayList<Direction> getPrioritizedDirection(RobotController rc, Direction fw) {
        ArrayList<Direction> prioritizedDirections = new ArrayList<>();
        int fwIdx = -1;
        for (int i = 0; i < directions.length; i++) {
            if (directions[i] == fw) {
                fwIdx = i;
                break;
            }
        }

        int offset = 0;

        while (fwIdx != -1 && offset <= 4 && prioritizedDirections.size() < 5)
        {
            if (moveable(rc, directions[(fwIdx + offset) % directions.length]))
                prioritizedDirections.add(directions[(fwIdx + 1) % directions.length]);

            if (offset != 0 && moveable(rc, directions[(fwIdx + directions.length - offset) % directions.length]))
                prioritizedDirections.add(directions[(fwIdx + directions.length - offset) % directions.length]); 
                
            offset += 1;
        }
        
        Collections.shuffle(prioritizedDirections);
        return prioritizedDirections;
    }

    public static Direction march2Target(RobotController rc, MapLocation target) throws GameActionException {
        if (rc.getLocation() == null)
            return null;
        Direction dir = rc.getLocation().directionTo(target);
        ArrayList<Direction> prioritizedDirs = getPrioritizedDirection(rc, dir);
        for (Direction pDir: prioritizedDirs) {
            if (walkable(rc, pDir))
                return pDir;
        }
        // stuck by a wall and unable to move
        // if stuck at the same location
        if (stuckLoc != null && rc.getLocation() == stuckLoc) {
            // opposite direction can sometimes yield null :(
            try {
                Direction op_dir = lastMoveable.opposite();
                if (walkable(rc, op_dir))
                return dir;
                else {
                    // reverse rotation compared to the 1st time
                    if (!lastClockwise)
                        dir = dir.rotateRight();
                    else
                        dir = dir.rotateLeft();
                    if (walkable(rc, dir)) {
                        lastMoveable = dir;
                        lastClockwise = !lastClockwise;
                        return dir;
                    }
                }
            } catch (Exception e) {
                return null;
            }
        }
        // got stuck some where new -> take random dir
        else {
            stuckLoc = rc.getLocation();
            boolean clockwise = rng.nextBoolean();
            for (int i = 0; i < 9; ++i) {
                if (clockwise)
                    dir = dir.rotateRight();
                else
                    dir = dir.rotateLeft();
                if (walkable(rc, dir)) {
                    lastMoveable = dir;
                    lastClockwise = clockwise;
                    return dir;
                }
            }
        }
        return null;
    }
}