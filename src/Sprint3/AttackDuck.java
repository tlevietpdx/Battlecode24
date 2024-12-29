package Sprint3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import battlecode.common.*;
import java.util.Random;

public class AttackDuck {

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

    private static final int START_SHARED = 32;
    private static final int END_SHARED = 47;
    private static final int MAX_ENTRIES = 9;

    private static int closestLoc(MapLocation myLoc, ArrayList<MapLocation> targetLocs) throws GameActionException {
        // should move to the closet ally spawn location
        int minSpawnDst = Integer.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < targetLocs.size(); i++) {
            if (myLoc.distanceSquaredTo(targetLocs.get(i)) < minSpawnDst) {
                idx = i;
                minSpawnDst = myLoc.distanceSquaredTo(targetLocs.get(i));
            }
        }
        return idx;
    }

    // return whether retreat is executed
    public static boolean retreatStrategy(RobotController rc) throws GameActionException {
        if (rc.canPickupFlag(rc.getLocation())) {
            rc.pickupFlag(rc.getLocation());
            if (rc.hasFlag()) {
                int capturedFlag = rc.readSharedArray(2);
                capturedFlag += 1;
                rc.writeSharedArray(2, capturedFlag);
                rc.writeSharedArray(3,1);
            }
        }

        MapLocation[] rawSpawnLocs = rc.getAllySpawnLocations();
        ArrayList<MapLocation> spawnLocs = new ArrayList<>();
        for (MapLocation raw : rawSpawnLocs)
            spawnLocs.add(raw);

        // If retreat conditions are met
        if (rc.getRoundNum() >= GameConstants.SETUP_ROUNDS) {
            if (rc.hasFlag()) {
                MapLocation chosenBase = spawnLocs.get(rc.getID() % spawnLocs.size());
                Direction dir = Navigator.march2Target(rc, chosenBase);

                // Fallback to naive random scouting if no valid direction is found
                if (dir == null) {
                    dir = naiveScoutRandom(rc);
                }

                return true; // Indicate retreat action
            }
        }

        // Update retreat status to false since we're not retreating
        return false;
    }


    public static void attackStrategy(RobotController rc) throws GameActionException {
        try {
            GlobalUpgradeManager upgradeManager = new GlobalUpgradeManager(rc);
            upgradeManager.updateWoundedCount();
            upgradeManager.resetWoundedCount();

            Direction stepped = null;

            // scout for enemy robots and explore world
            // Sensing methods can be passed in a radius of -1 to automatically
            // use the largest possible value.
            // attack enemy with the lowest health
            RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            stepped = senseEnemy(rc, enemyRobots);
            if (stepped != null)
                return;

            // scout for enemy flag
            // find enemy flag location (including broadcast)
            stepped = getEnemyFlags(rc);
            if (stepped != null)
                return;

            RobotInfo[] allyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
            stepped = senseAlly(rc, allyRobots);
            if (stepped != null)
                return;

            if (stepped == null) {
                stepped = scoutRandom(rc);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static Direction getEnemyFlags(RobotController rc) throws GameActionException {
        if (!rc.isSpawned())
            return null;
        ArrayList<MapLocation> flagLocs = new ArrayList<>();
        FlagInfo[] enemyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        Direction stepped = null;
        for (FlagInfo flag : enemyFlags)
            flagLocs.add(flag.getLocation());
        // does not detect enemy flag
        if (flagLocs.size() == 0) {
            // search for nearby flag
            MapLocation[] broadcastLocs = rc.senseBroadcastFlagLocations();
            for (MapLocation flagLoc : broadcastLocs)
                flagLocs.add(flagLoc);

            // half force will look for the flag and half would scout randomly
            if (flagLocs.size() != 0 && rc.getID() % 2 == 1) {
                MapLocation closestFlag = flagLocs.get(closestLoc(rc.getLocation(), flagLocs));
                stepped = Navigator.march2Target(rc, closestFlag);
                if (rc.canPickupFlag(closestFlag)) {
                    rc.pickupFlag(closestFlag);
                    if (rc.hasFlag()) {
                        int capturedFlag = rc.readSharedArray(2);
                        capturedFlag += 1;
                        rc.writeSharedArray(2, capturedFlag);
                    }
                }
            }
        }
        return stepped;
    }

    public static MapLocation canBuildTrap(RobotController rc, MapLocation enemyLocation) throws GameActionException {
        if (rc.getLocation() == null)
            return null;
        Direction toEnemy = rc.getLocation().directionTo(enemyLocation);
    
        // Define directions to attempt placing traps, up to 45 degrees to either side of `toEnemy`
        Direction[] possibleDirections = {
            toEnemy,
            toEnemy.rotateLeft(), toEnemy.rotateRight(),
            toEnemy.rotateLeft().rotateLeft(), toEnemy.rotateRight().rotateRight()
        };
    
        // Loop through directions to try to lay traps along the path to the enemy
        for (Direction dir : possibleDirections) {
            // Calculate potential trap location in this direction
            MapLocation trapLocation = rc.getLocation().add(dir);
    
            // Check if it's valid and possible to lay a trap at this location
            if (rc.canBuild(TrapType.EXPLOSIVE, trapLocation)) {
                return trapLocation;
            }
        }
        return null;
    }
        

    public static Direction senseEnemy(RobotController rc, RobotInfo[] enemyRobots)
            throws GameActionException {

        // Sort enemies based on the priority: hasFlag -> highestAtk -> highestBuild ->
        // lowest health
        Arrays.sort(enemyRobots, (a, b) -> {
            if (a.hasFlag() && !b.hasFlag())
                return -1;
            if (!a.hasFlag() && b.hasFlag())
                return 1;
            if (a.attackLevel != b.attackLevel)
                return Integer.compare(b.attackLevel, a.attackLevel); // highest attack first
            if (a.buildLevel != b.buildLevel)
                return Integer.compare(b.buildLevel, a.buildLevel); // highest build first
            return Integer.compare(a.health, b.health); // lowest health last
        });

        Direction stepped = null;

        // Attack the highest-priority enemy
        for (RobotInfo enemy : enemyRobots) {
            // Move toward and attack the prioritized enemy
            if (rc.getID() % 2 == 0)
                stepped = Explorer.march2Target(rc, enemy.getLocation());
            else
                stepped = Navigator.march2Target(rc, enemy.getLocation());
            
            // If can lay traps, do it
            if (enemyRobots.length >= 2 && rc.getCrumbs() >= 1000) {
                MapLocation trapLoc = canBuildTrap(rc, enemy.getLocation());
                if (trapLoc != null) {
                    if (rng.nextBoolean())
                        rc.build(TrapType.EXPLOSIVE, trapLoc);
                    else
                        rc.build(TrapType.STUN, trapLoc);
                    break;
                }
            }

            // If can attack, do it
            if (rc.canAttack(enemy.getLocation())) {
                rc.attack(enemy.getLocation());

                // Only increment counter when reaching level 4
                if (rc.getLevel(SkillType.ATTACK) >= 4) {
                    int myID = rc.getID(); // Get unique robot ID

                    // Use index 4 to store the last counted robot's ID
                    int lastCountedID = rc.readSharedArray(4);

                    // Only increment if this robot hasn't been counted (different ID)
                    if (lastCountedID != myID) {
                        int attackerCount = rc.readSharedArray(0);
                        attackerCount += 1;
                        rc.writeSharedArray(0, attackerCount);
                        rc.writeSharedArray(4, myID); // Store this robot's ID
                    }
                }
                break;
            }
        }
        return stepped;
    }

    public static Direction senseAlly(RobotController rc, RobotInfo[] allyRobots)
            throws GameActionException {
        Direction stepped = null;
        for (RobotInfo ally : allyRobots) {
            if (ally.hasFlag()) {
                stepped = Explorer.march2Target(rc, ally.getLocation());
                break;
            }
        }
        return stepped;
    }

    public static List<MapLocation> findProjectedEnemyLocations(List<MapLocation> ourSpawnZones, int mapWidth, int mapHeight) {
        List<MapLocation> enemySpawnLocations = new ArrayList<>();
        
        for (MapLocation ourSpawn : ourSpawnZones) {
            
            // Reflect across the main diagonal (top-left to bottom-right)
            MapLocation diagonalReflection = new MapLocation(ourSpawn.y, ourSpawn.x);
            if (isValidLocation(diagonalReflection, mapWidth, mapHeight) && !enemySpawnLocations.contains(diagonalReflection)) {
                enemySpawnLocations.add(diagonalReflection);
            }

            // Reflect across the vertical axis
            MapLocation verticalReflection = new MapLocation(mapWidth - ourSpawn.x - 1, ourSpawn.y);
            if (isValidLocation(verticalReflection, mapWidth, mapHeight) && !enemySpawnLocations.contains(verticalReflection)) {
                enemySpawnLocations.add(verticalReflection);
            }
    
            // Reflect across the anti-diagonal (top-right to bottom-left)
            MapLocation antiDiagonalReflection = new MapLocation(mapWidth - ourSpawn.y - 1, mapHeight - ourSpawn.x - 1);
            if (isValidLocation(antiDiagonalReflection, mapWidth, mapHeight) && !enemySpawnLocations.contains(antiDiagonalReflection)) {
                enemySpawnLocations.add(antiDiagonalReflection);
            }

            // Reflect across the horizontal axis
            MapLocation horizontalReflection = new MapLocation(ourSpawn.x, mapHeight - ourSpawn.y - 1);
            if (isValidLocation(horizontalReflection, mapWidth, mapHeight) && !enemySpawnLocations.contains(horizontalReflection)) {
                enemySpawnLocations.add(horizontalReflection);
            }
        }
        return enemySpawnLocations;
    }
    
    
    // Helper function to check if a location is within the map boundaries
    public static boolean isValidLocation(MapLocation loc, int mapWidth, int mapHeight) {
        return loc.x >= 2 && loc.x < mapWidth - 2 && loc.y >= 2 && loc.y < mapHeight - 2;
    }

    public static Direction naiveScoutRandom(RobotController rc) throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        if (Navigator.walkable(rc, dir))
            return dir;
        return null;
    }

    public static Direction scoutRandom(RobotController rc) throws GameActionException {
        int REFRESH_ROUND = (int) Math.sqrt((rc.getMapHeight() * rc.getMapHeight() + (rc.getMapWidth() * rc.getMapWidth())))/3;
        int robotOffset = (rc.getID() + 7207) % MAX_ENTRIES;
        int roundOffset = rc.getRoundNum() / REFRESH_ROUND;
    
        // Calculate the index in the shared array specific to this robot
        int xIndex = START_SHARED + 2 * robotOffset;
        int yIndex = START_SHARED + 2 * robotOffset + 1;
    
        // Read target location from shared array at robot's specific offset
        int x = rc.readSharedArray(xIndex);
        int y = rc.readSharedArray(yIndex);
    
        MapLocation target;
    
        // If the shared array entry is uninitialized (both x and y are 0), calculate a new target
        if (x == 0 && y == 0) {
            // Generate the projected enemy locations for the ally spawn zones
            List<MapLocation> projectedEnemyLocations = findProjectedEnemyLocations(
                new ArrayList<>(Arrays.asList(rc.getAllySpawnLocations())), 
                rc.getMapWidth(), rc.getMapHeight()
            );
    
            // Select a target from projected locations based on round offset
            if (!projectedEnemyLocations.isEmpty()) {
                target = projectedEnemyLocations.get((robotOffset + roundOffset) % projectedEnemyLocations.size());
            } else {
                // If no projected locations, fallback to a random location on the map
                target = new MapLocation(rng.nextInt(rc.getMapWidth()), rng.nextInt(rc.getMapHeight()));
            }
    
            // Write the calculated target to the shared array
            rc.writeSharedArray(xIndex, target.x);
            rc.writeSharedArray(yIndex, target.y);
    
        } else {
            // Use the existing location from shared array as target if it is already set
            target = new MapLocation(x, y);
        }
    
        // Move toward the determined target location
        Direction dir = Explorer.march2Target(rc, target);
        return dir;
    }
}
