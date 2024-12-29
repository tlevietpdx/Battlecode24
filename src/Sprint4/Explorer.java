package Sprint4;

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

    public static boolean walkable(RobotController rc, Direction dir) {
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

    public static boolean moveable(RobotController rc, Direction dir) {
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

        while (fwIdx != -1 && offset <= 4 && prioritizedDirections.size() < 7)
        {
            if (offset != 0 && moveable(rc, directions[(fwIdx + directions.length - offset) % directions.length]))
                prioritizedDirections.add(directions[(fwIdx + directions.length - offset) % directions.length]); 
                
            if (moveable(rc, directions[(fwIdx + offset) % directions.length]))
                prioritizedDirections.add(directions[(fwIdx + 1) % directions.length]);

            offset += 1;
        }
        
        Collections.shuffle(prioritizedDirections);
        return prioritizedDirections;
    }

    public static Direction march2Target(RobotController rc, MapLocation target) throws GameActionException {
        if (rc.getLocation() == null || target == null) {
            return null;
        }
    
        Direction initialDir = rc.getLocation().directionTo(target);
        ArrayList<Direction> prioritizedDirs = getPrioritizedDirection(rc, initialDir);
    
        // Try moving in the prioritized directions
        for (Direction dir : prioritizedDirs) {
            if (walkable(rc, dir)) {
                return dir;
            }
        }
    
        // Handle being stuck at the same location
        if (isStuck(rc)) {
            Direction unstuckDir = tryOppositeOrRotate(rc);
            if (unstuckDir != null) {
                return unstuckDir;
            }
        } else {
            // Mark current location as stuck
            stuckLoc = rc.getLocation();
        }
    
        // Try random directions if stuck
        return tryRandomRotation(rc, initialDir);
    }
    
    // Helper method to check if the robot is stuck
    private static boolean isStuck(RobotController rc) {
        return stuckLoc != null && rc.getLocation().equals(stuckLoc);
    }
    
    // Helper method to try opposite direction or rotate to find a moveable direction
    private static Direction tryOppositeOrRotate(RobotController rc) throws GameActionException {
        try {
            Direction oppositeDir = lastMoveable.opposite();
            if (walkable(rc, oppositeDir)) {
                return oppositeDir;
            } else {
                return rotateAndCheck(rc, lastMoveable);
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    // Helper method to rotate and check for walkable directions
    private static Direction rotateAndCheck(RobotController rc, Direction dir) throws GameActionException {
        Direction rotatedDir = lastClockwise ? dir.rotateRight() : dir.rotateLeft();
        if (walkable(rc, rotatedDir)) {
            lastMoveable = rotatedDir;
            lastClockwise = !lastClockwise;
            return rotatedDir;
        }
        return null;
    }
    
    // Helper method to try random rotations if stuck
    private static Direction tryRandomRotation(RobotController rc, Direction initialDir) throws GameActionException {
        boolean clockwise = rng.nextBoolean();
        for (int i = 0; i < 9; ++i) {
            initialDir = clockwise ? initialDir.rotateLeft() : initialDir.rotateRight();
            if (walkable(rc, initialDir)) {
                lastMoveable = initialDir;
                lastClockwise = clockwise;
                return initialDir;
            }
        }
        return null;
    }
    
}