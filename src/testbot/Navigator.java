package testbot;

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

    private static MapLocation stuckLoc = null;
    private static Direction lastMoveable = null;
    private static boolean lastClockwise = false;

    private static boolean walkable(RobotController rc, Direction dir) {
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
        } catch (GameActionException e) {
            // e.printStackTrace(); // Handle the exception (e.g., log it)
        }
        return false;
    }

    // TODO: improvement ideas, within 30 moves & same appointed target, if the
    // robot is circling around, stop
    // and choose another chosen target to move to, during that, if a different
    // target is appointed, try it
    // otherwise, keep chosen target, if chosen target does not work in 30 moves,
    // switch to another chosen.
    // public static Direction march2Target(RobotController rc, MapLocation target) throws GameActionException {
    //     NavigatorData data = navHashMap.get(rc.getID());
    //     if (data == null) {
    //         Direction dir = naiveMarch2Target(rc, target);
    //         navHashMap.put(rc.getID(), new NavigatorData(target, dir));
    //         System.out.println(String.valueOf(rc.getRoundNum()) + ":(init) heading to " + String.valueOf(target));
    //         return dir;
    //     }
    //     if (!data.circlingCheck())
    //     {
    //         Direction dir = naiveMarch2Target(rc, target);
    //         data.updateStep(target, rc.getLocation());
    //         navHashMap.replace(rc.getID(), data);
    //         System.out.println(String.valueOf(rc.getRoundNum()) + ":(init) heading to " + String.valueOf(target) + String.valueOf(data.getStepRecs()));
    //         return dir;
    //     }
    //     else {
    //         // is circling around
    //         MapLocation localTarget = null;
    //         if (data.getMyTarget() == null) {
    //             MapLocation randTarget = new MapLocation(rng.nextInt(GameConstants.MAP_MAX_WIDTH), rng.nextInt(GameConstants.MAP_MAX_HEIGHT));
    //             data.setMyTarget(randTarget);
    //             localTarget = randTarget;
    //         }
    //         else { 
    //             localTarget = data.getMyTarget();
    //         }
    //         Direction dir = naiveMarch2Target(rc, localTarget);
    //         // if we can step, update record & data in the array
    //         if (walkable(rc, dir)) {
    //             data.updateStep(localTarget, rc.getLocation());
    //             navHashMap.replace(rc.getID(), data);
    //             return dir;
    //         }
    //         System.out.println(String.valueOf(rc.getRoundNum()) + ":(init) heading to " + String.valueOf(target) + String.valueOf(data.getStepRecs()));
    //     }
    //     return null;
    // }

    public static ArrayList<Direction> getPrioritizedDirection(Direction fw) {
        ArrayList<Direction> prioritizedDirections = new ArrayList<>();
        int fwIdx = -1;
        for (int i = 0; i < directions.length; i++) {
            if (directions[i] == fw) {
                fwIdx = i;
                break;
            }
        }

        if (fwIdx != -1) {
            prioritizedDirections.add(directions[fwIdx]);
            prioritizedDirections.add(directions[(fwIdx + 1) % directions.length]);
            prioritizedDirections.add(directions[(fwIdx + directions.length - 1) % directions.length]);
            prioritizedDirections.add(directions[(fwIdx + 2) % directions.length]);
            prioritizedDirections.add(directions[(fwIdx + directions.length - 2) % directions.length]);
            prioritizedDirections.add(directions[(fwIdx + 3) % directions.length]);
            prioritizedDirections.add(directions[(fwIdx + directions.length - 3) % directions.length]);
        }

        Collections.shuffle(prioritizedDirections);
        return prioritizedDirections;
    }

    public static Direction march2Target(RobotController rc, MapLocation target) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(target);
        ArrayList<Direction> prioritizedDirs = getPrioritizedDirection(dir);
        for (Direction pDir: prioritizedDirs) {
            if (walkable(rc, pDir))
                return pDir;
        }
        // stuck by a wall and unable to move
        // if stuck at the same location
        if (stuckLoc != null && rc.getLocation() == stuckLoc) {
            Direction op_dir = lastMoveable;
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

// package testbot;

// import java.util.ArrayList;
// import java.util.Random;

// import battlecode.common.*;

// public class Navigator {

//     static final Direction[] directions = {
//             Direction.NORTH,
//             Direction.NORTHEAST,
//             Direction.EAST,
//             Direction.SOUTHEAST,
//             Direction.SOUTH,
//             Direction.SOUTHWEST,
//             Direction.WEST,
//             Direction.NORTHWEST,
//     };

//     static final Random rng = new Random(2609);

//     private static boolean walkable(RobotController rc, Direction dir) {
//         try {
//             if (rc.canMove(dir)) {
//                 rc.move(dir);
//                 return true;
//             } else if (rc.canFill(rc.getLocation().add(dir))) {
//                 rc.fill(rc.getLocation().add(dir));
//                 return true;
//             }
//         } catch (GameActionException e) {
//             // e.printStackTrace(); // Handle the exception (e.g., log it)
//         }
//         return false;
//     }

//     public static ArrayList<Direction> getPrioritizedDirection(Direction fw) {
//         ArrayList<Direction> prioritizedDirections = new ArrayList<>();
//         int fwIdx = -1;
//         for (int i = 0; i < directions.length; i++) {
//             if (directions[i] == fw) {
//                 fwIdx = i;
//                 break;
//             }
//         }

//         if (fwIdx != -1) {
//             prioritizedDirections.add(directions[fwIdx]);
//             prioritizedDirections.add(directions[(fwIdx + 1) % directions.length]);
//             prioritizedDirections.add(directions[(fwIdx + directions.length - 1) % directions.length]);
//             prioritizedDirections.add(directions[(fwIdx + 2) % directions.length]);
//             prioritizedDirections.add(directions[(fwIdx + directions.length - 2) % directions.length]);
//         }

//         return prioritizedDirections;
//     }

//     public static Direction march2Target(RobotController rc, MapLocation target) throws GameActionException {
//         Direction dir = rc.getLocation().directionTo(target);
//         ArrayList<Direction> orderedDir = getPrioritizedDirection(dir);
//         for (Direction direction : orderedDir) {
//             if (walkable(rc, direction))
//                 return direction;
//         }
//         return null;
//     }
// }