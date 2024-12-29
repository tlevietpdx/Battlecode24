package Sprint3;

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
        int fwIdx = -1;

        // Find index of the forward direction
        for (int i = 0; i < directions.length; i++) {
            if (directions[i] == fw) {
                fwIdx = i;
                break;
            }
        }

        if (fwIdx == -1) // If forward direction is not found, return empty list
            return prioritizedDirections;

        int offset = 0;

        while (prioritizedDirections.size() < 4 && rotateCode != 0 && offset < 4) {
            // Ensure offset is always within the bounds
            int forwardIndex = (fwIdx + offset) % directions.length;
            int backwardIndex = (fwIdx + directions.length - offset) % directions.length;

            // Go all the way left
            if (rotateCode == 1 && moveable(rc, directions[forwardIndex])) {
                prioritizedDirections.add(directions[forwardIndex]);
            }

            // Go all the way right
            if (rotateCode == 2 && moveable(rc, directions[backwardIndex])) {
                prioritizedDirections.add(directions[backwardIndex]);
            }

            offset += 1;
        }

        // If only the center direction is chosen (rotateCode = 0)
        if (rotateCode == 0) {
            if (moveable(rc, directions[fwIdx])) {
                prioritizedDirections.add(directions[fwIdx]);
            }
            if (moveable(rc, directions[(fwIdx + 1) % directions.length])) {
                prioritizedDirections.add(directions[(fwIdx + 1) % directions.length]);
            }
            if (moveable(rc, directions[(fwIdx + directions.length - 1) % directions.length])) {
                prioritizedDirections.add(directions[(fwIdx + directions.length - 1) % directions.length]);
            }
        }

        return prioritizedDirections;
    }

    public static Direction march2Target(RobotController rc, MapLocation target) throws GameActionException {
        if (rc.getLocation() == null || target == null)
            return null;
        Direction dir = rc.getLocation().directionTo(target);
        ArrayList<Direction> prioritizedDirs = getPrioritizedDirection(rc, dir, rotCenter);
        for (Direction pDir : prioritizedDirs) {
            if (walkable(rc, pDir))
                return pDir;
        }

        if (stuckLoc != null && rc.getLocation() == stuckLoc) {
            // opposite direction can sometimes yield null :(
            try {
                Direction op_dir = lastMoveable.opposite();
                if (walkable(rc, op_dir))
                    return dir;
                else {
                    // reverse rotation compared to the 1st time
                    if (!lastClockwise)
                        prioritizedDirs = getPrioritizedDirection(rc, dir, rotRight);
                    else
                        prioritizedDirs = getPrioritizedDirection(rc, dir, rotLeft);
                    for (Direction pDir : prioritizedDirs) {
                        if (walkable(rc, pDir)) {
                            lastMoveable = pDir;
                            lastClockwise = !lastClockwise;
                            return pDir;
                        }
                    }
                }
            } catch (Exception e) {
                return null;
            }
        }
        // 1st time || got stuck some where new -> take random dir
        else {
            stuckLoc = rc.getLocation();
            boolean clockwise = (rc.getID() % 2 == 0);
            if (clockwise && !rc.hasFlag())
                prioritizedDirs = getPrioritizedDirection(rc, dir, rotRight);
            else
                prioritizedDirs = getPrioritizedDirection(rc, dir, rotLeft);
            for (Direction pDir : prioritizedDirs) {
                if (walkable(rc, pDir)) {
                    lastMoveable = pDir;
                    lastClockwise = clockwise;
                    return pDir;
                }
            }
        }

        return null;
    }
}