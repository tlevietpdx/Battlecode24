package Sprint4;

import java.util.Random;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Collections;

public class Navigator {

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

    private static int rotCenter = 0;
    private static int rotLeft = 1;
    private static int rotRight = 2;

    private static MapLocation stuckLoc = null;
    private static Direction lastMoveable = null;
    private static boolean lastClockwise = false;

    public static boolean walkable(RobotController rc, Direction dir) {
        if (dir == null)
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
        if (dir == null)
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

    public static ArrayList<Direction> getPrioritizedDirection(RobotController rc, Direction fw, int rotateCode) {
        ArrayList<Direction> prioritizedDirections = new ArrayList<>();
        int fwIdx = getDirectionIndex(fw);
    
        if (fwIdx == -1) { // If forward direction is not found, return empty list
            return prioritizedDirections;
        }
    
        // Prioritize center direction (rotateCode = 0)
        if (rotateCode == 0) {
            addMoveableDirections(rc, prioritizedDirections, fwIdx, 1);
            return prioritizedDirections;
        }
    
        // Rotate either left (rotateCode = 1) or right (rotateCode = 2)
        for (int offset = 0; offset < 4 && prioritizedDirections.size() < 4; offset++) {
            int rotatedIdx = rotateCode == 1 
                ? (fwIdx + offset) % directions.length      // Rotate left
                : (fwIdx + directions.length - offset) % directions.length; // Rotate right
    
            addMoveableDirection(rc, prioritizedDirections, rotatedIdx);
        }
    
        return prioritizedDirections;
    }
    
    // Helper method to get the index of a direction
    private static int getDirectionIndex(Direction dir) {
        for (int i = 0; i < directions.length; i++) {
            if (directions[i] == dir) {
                return i;
            }
        }
        return -1;
    }
    
    // Helper method to add moveable directions in both directions from fwIdx
    private static void addMoveableDirections(RobotController rc, ArrayList<Direction> prioritizedDirections, int fwIdx, int range) {
        addMoveableDirection(rc, prioritizedDirections, fwIdx); // Forward
        addMoveableDirection(rc, prioritizedDirections, (fwIdx + directions.length - 1) % directions.length); // Left
        addMoveableDirection(rc, prioritizedDirections, (fwIdx + 1) % directions.length); // Right
    }
    
    // Helper method to check if a direction is moveable and add it
    private static void addMoveableDirection(RobotController rc, ArrayList<Direction> prioritizedDirections, int index) {
        if (moveable(rc, directions[index])) {
            prioritizedDirections.add(directions[index]);
        }
    }
    

    public static Direction march2Target(RobotController rc, MapLocation target) throws GameActionException {
        if (rc.getLocation() == null || target == null) {
            return null;
        }
    
        Direction initialDir = rc.getLocation().directionTo(target);
        ArrayList<Direction> prioritizedDirs = getPrioritizedDirection(rc, initialDir, rotCenter);
    
        // Try to move in prioritized directions
        Direction moveDir = findWalkableDirection(rc, prioritizedDirs);
        if (moveDir != null) {
            return moveDir;
        }
    
        // Handle case when stuck
        if (isStuck(rc)) {
            moveDir = handleStuckSituation(rc, initialDir);
        } else {
            moveDir = handleNewStuckSituation(rc, initialDir);
        }
    
        return moveDir;
    }
    
    // Helper method to check if the robot is stuck at the same location
    private static boolean isStuck(RobotController rc) {
        return stuckLoc != null && rc.getLocation().equals(stuckLoc);
    }
    
    // Helper method to find a walkable direction from a list of prioritized directions
    private static Direction findWalkableDirection(RobotController rc, ArrayList<Direction> prioritizedDirs) throws GameActionException {
        for (Direction dir : prioritizedDirs) {
            if (walkable(rc, dir)) {
                return dir;
            }
        }
        return null;
    }
    
    // Helper method to handle movement when stuck
    private static Direction handleStuckSituation(RobotController rc, Direction dir) throws GameActionException {
        try {
            Direction oppositeDir = lastMoveable.opposite();
            if (walkable(rc, oppositeDir)) {
                return oppositeDir;
            }
    
            ArrayList<Direction> prioritizedDirs = rotatePrioritizedDirections(rc, dir);
            Direction moveDir = findWalkableDirection(rc, prioritizedDirs);
            if (moveDir != null) {
                lastMoveable = moveDir;
                lastClockwise = !lastClockwise;
            }
            return moveDir;
        } catch (Exception e) {
            return null;
        }
    }
    
    // Helper method to handle new stuck situation by trying random directions
    private static Direction handleNewStuckSituation(RobotController rc, Direction dir) throws GameActionException {
        stuckLoc = rc.getLocation();
        boolean clockwise = (rc.getID() % 2 == 0 && !rc.hasFlag());
        ArrayList<Direction> prioritizedDirs = getPrioritizedDirection(rc, dir, clockwise ? rotLeft : rotRight);
    
        Direction moveDir = findWalkableDirection(rc, prioritizedDirs);
        if (moveDir != null) {
            lastMoveable = moveDir;
            lastClockwise = clockwise;
        }
        return moveDir;
    }
    
    // Helper method to rotate prioritized directions based on lastClockwise value
    private static ArrayList<Direction> rotatePrioritizedDirections(RobotController rc, Direction dir) throws GameActionException {
        return lastClockwise ? getPrioritizedDirection(rc, dir, rotRight) : getPrioritizedDirection(rc, dir, rotLeft);
    }
    
}