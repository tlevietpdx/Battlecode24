package Sprint2;

import java.util.ArrayList;

import battlecode.common.*;
import java.util.Random;

public class AttackDuck {

    static final Random rng = new Random(2609);
    
    private static int closestLoc(MapLocation myLoc, ArrayList<MapLocation> targetLocs) throws GameActionException {
        // should move to the closet ally spawn location
        int minSpawnDst = Integer.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < targetLocs.size(); i++){
            if (myLoc.distanceSquaredTo(targetLocs.get(i)) < minSpawnDst){
                idx = i;
                minSpawnDst = myLoc.distanceSquaredTo(targetLocs.get(i));
            }
        }
        return idx;
    }

    //return whether retreat is executed
    public static boolean retreatStrategy(RobotController rc) throws GameActionException {
        if (rc.canPickupFlag(rc.getLocation())){
            rc.pickupFlag(rc.getLocation());
            // rc.setIndicatorString("Holding a flag!");
        }
        // If we are holding an enemy flag, singularly focus on moving towards
        // an ally spawn zone to capture it! We use the check roundNum >= SETUP_ROUNDS
        // to make sure setup phase has ended.
        MapLocation[] rawSpawnLocs = rc.getAllySpawnLocations();
        ArrayList<MapLocation> spawnLocs = new ArrayList<>();
        for (MapLocation raw : rawSpawnLocs) spawnLocs.add(raw);
        if (rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS) {
            MapLocation closestBase = spawnLocs.get(closestLoc(rc.getLocation(), spawnLocs));
            Direction _ = Navigator.march2Target(rc, closestBase);
            return true;
        }
        return false;
    }

    public static void attackStrategy(RobotController rc) throws GameActionException {
        // scout for enemy robots and explore world
        // Sensing methods can be passed in a radius of -1 to automatically 
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        Direction stepped = null;
        for (RobotInfo enemy : enemyRobots) {
            // (chase enemy that has flag 
            if (enemy.hasFlag()) {
                stepped = Navigator.march2Target(rc, enemy.getLocation());
                if (rc.canAttack(enemy.getLocation())) rc.attack(enemy.getLocation());
                break;
            }
            //or 80% of chasing random enemy) and attack them
            else if (rng.nextInt(5) != 0) {
                stepped = Navigator.march2Target(rc, enemy.getLocation());
                if (rc.canAttack(enemy.getLocation())) rc.attack(enemy.getLocation());
                break;
            }
        }

        // scout for enemy flag
        //find enemy flag location (including broadcast)
        ArrayList<MapLocation> flagLocs = new ArrayList<>();
        FlagInfo[] enemyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        for (FlagInfo flag : enemyFlags) flagLocs.add(flag.getLocation());
        // does not detect enemy flag
        if (flagLocs.size() == 0) {
            // search for nearby flag
            MapLocation[] broadcastLocs = rc.senseBroadcastFlagLocations();
            for (MapLocation flagLoc : broadcastLocs) flagLocs.add(flagLoc);

            // half force will look for the flag and half would scout randomly
            if (flagLocs.size() != 0 && rng.nextInt(5) != 0){
                MapLocation closestFlag = flagLocs.get(closestLoc(rc.getLocation(), flagLocs));
                stepped = Navigator.march2Target(rc, closestFlag);
                if (rc.canPickupFlag(closestFlag)) rc.pickupFlag(closestFlag);   
            }
        }

        if (stepped == null) {
            stepped = scoutRandom(rc);
        }
    }

    private static Direction scoutRandom(RobotController rc) throws GameActionException {
        //get random target position on map
        MapLocation randTarget = new MapLocation(rng.nextInt(GameConstants.MAP_MAX_WIDTH), rng.nextInt(GameConstants.MAP_MAX_HEIGHT));
        return Navigator.march2Target(rc, randTarget);
    }
}
