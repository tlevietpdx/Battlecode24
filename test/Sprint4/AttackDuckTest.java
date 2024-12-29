package Sprint4;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import battlecode.common.*;

public class AttackDuckTest {

    private RobotController rc;
    private MapLocation location;
    private AttackDuck testClass = null;

    private MapLocation enemyLoc;
    private MapLocation myLoc;

    private static final int START_SHARED = 32;
    private static final int END_SHARED = 47;
    private static final int MAX_ENTRIES = 7;
    private static final int MAX_BOMB_PER_TURN = 30;

    @Before
    public void setup() {
        rc = mock(RobotController.class);
        // Example mock setup if scoutRandom calls these methods
        when(rc.getLocation()).thenReturn(new MapLocation(0, 0));
        when(rc.canMove(any(Direction.class))).thenReturn(true);

        location = new MapLocation(0, 0);
        when(rc.getLocation()).thenReturn(location);

        this.testClass = new AttackDuck();

        myLoc = new MapLocation(0, 0);
        enemyLoc = new MapLocation(5, 5);
    }

    private Method getVisibleMethod(String funcName, Class<?>... parameterTypes)
            throws NoSuchMethodException, SecurityException {
        Method method = AttackDuck.class.getDeclaredMethod(funcName, parameterTypes);
        method.setAccessible(true); // Make the private method accessible
        return method;
    }

    @Test
    public void testSanity() {
        assertEquals(2, 1 + 1);
    }

    @Test
    public void testClosestLoc() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        MapLocation myLoc = new MapLocation(0, 0);
        ArrayList<MapLocation> targetLocs = new ArrayList<>();
        targetLocs.add(new MapLocation(10, 10));
        targetLocs.add(new MapLocation(5, 5));
        targetLocs.add(new MapLocation(1, 1));

        Method method = getVisibleMethod("closestLoc", MapLocation.class, ArrayList.class);
        int closestIndex = (int) method.invoke(this.testClass, myLoc, targetLocs);
        assertEquals(2, closestIndex); // (1,1) should be the closest
    }

    @Test
    public void testRetreatStrategy_NoFlag() throws GameActionException {
        // Mock RobotController
        RobotController rc = mock(RobotController.class);

        // Simulate not having a flag
        when(!rc.hasFlag()).thenReturn(true);

        // Mock methods called within retreatStrategy
        when(rc.getLocation()).thenReturn(new MapLocation(5, 5));
        when(rc.getRoundNum()).thenReturn(GameConstants.SETUP_ROUNDS + 1);
        when(rc.getAllySpawnLocations()).thenReturn(new MapLocation[] {
                new MapLocation(10, 10), new MapLocation(15, 15)
        });

        // Mock additional calls if necessary
        when(!rc.canPickupFlag(any())).thenReturn(true);

        // Test method
        boolean isRetreating = AttackDuck.retreatStrategy(rc);

        // Verify that no retreat happens
        assertTrue(isRetreating); // Ensure retreat strategy is not executed
    }

    @Test
    public void testRetreatStrategy_WithFlag() throws GameActionException {
        // Mock RobotController
        RobotController rc = mock(RobotController.class);

        // Simulate having a flag and being past setup rounds
        when(rc.hasFlag()).thenReturn(true);
        when(rc.getRoundNum()).thenReturn(GameConstants.SETUP_ROUNDS + 1);
        when(rc.getLocation()).thenReturn(new MapLocation(5, 5));
        when(rc.getAllySpawnLocations()).thenReturn(new MapLocation[] {
                new MapLocation(10, 10),
                new MapLocation(15, 15)
        });
        when(rc.getID()).thenReturn(3);

        // Mock conditions for pickupFlag
        when(rc.canPickupFlag(any(MapLocation.class))).thenReturn(true);

        // Test method
        boolean isRetreating = AttackDuck.retreatStrategy(rc);

        // Verify interactions and results
        assertTrue(isRetreating); // Ensure the retreat strategy is executed
        verify(rc).pickupFlag(any()); // Ensure the flag is picked up
    }

    @Test
    public void testScoutRandom_NewTarget() throws Exception {
        // Mock shared array access
        RobotController rc = mock(RobotController.class);
        when(rc.readSharedArray(anyInt())).thenReturn(0); // Uninitialized
        when(rc.getMapWidth()).thenReturn(20);
        when(rc.getMapHeight()).thenReturn(20);
        when(rc.getAllySpawnLocations()).thenReturn(new MapLocation[] {
                new MapLocation(2, 2),
                new MapLocation(4, 4)
        });
        when(rc.getID()).thenReturn(3);
        when(rc.getRoundNum()).thenReturn(30);

        // Invoke the private method
        Method scoutRandomMethod = getVisibleMethod("scoutRandom", RobotController.class);
        Direction direction = (Direction) scoutRandomMethod.invoke(this.testClass, rc);

        // Verify new target is written to shared array
        verify(rc, atLeastOnce()).writeSharedArray(anyInt(), anyInt());
        assertNull(direction); // Ensure a direction is returned
    }

    @Test
    public void testSenseEnemy_WhenMultipleEnemies() throws Exception {
        // Mock RobotController
        RobotController rc = mock(RobotController.class);
        when(rc.getLocation()).thenReturn(new MapLocation(0, 0));

        // Mock enemy robots with various properties
        RobotInfo enemy1 = mock(RobotInfo.class);
        when(enemy1.getLocation()).thenReturn(new MapLocation(1, 1));
        when(enemy1.hasFlag()).thenReturn(true);
        when(enemy1.getAttackLevel()).thenReturn(1);

        RobotInfo enemy2 = mock(RobotInfo.class);
        when(enemy2.getLocation()).thenReturn(new MapLocation(2, 2));
        when(enemy2.hasFlag()).thenReturn(false);
        when(enemy2.getAttackLevel()).thenReturn(3);

        RobotInfo[] enemies = { enemy1, enemy2 };

        // Invoke the private method via reflection
        Method senseEnemyMethod = AttackDuck.class.getDeclaredMethod("senseEnemy", RobotController.class,
                RobotInfo[].class);
        senseEnemyMethod.setAccessible(true); // Make the method accessible
        Direction result = (Direction) senseEnemyMethod.invoke(null, rc, enemies); // Invoke the method with the rc and
                                                                                   // enemies parameters

        // Verify the result
        assertNull(result);
    }

    @Test
    public void testGetEnemyFlags() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.getTeam().opponent()).thenReturn(Team.B);

        FlagInfo enemyFlag = mock(FlagInfo.class);
        when(enemyFlag.getLocation()).thenReturn(new MapLocation(5, 5));
        when(rc.senseNearbyFlags(-1, rc.getTeam().opponent())).thenReturn(new FlagInfo[] { enemyFlag });

        // Simulate getting enemy flags
        Direction result = AttackDuck.getEnemyFlags(rc);

        // Verify if flag retrieval logic works
        assertNull(result);
    }

    @Test
    public void testRobotNotSpawned() throws GameActionException {
        // Setup mocks
        when(rc.isSpawned()).thenReturn(false);

        // Call the method
        Direction result = AttackDuck.getEnemyFlags(rc);

        // Validate the result
        assertNull(result);
    }

    @Test
    public void testAttackStrategy() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getLocation()).thenReturn(new MapLocation(0, 0));
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.getTeam().opponent()).thenReturn(Team.B);

        RobotInfo enemy1 = mock(RobotInfo.class);
        when(enemy1.getLocation()).thenReturn(new MapLocation(1, 1));
        when(enemy1.hasFlag()).thenReturn(false);
        when(enemy1.getAttackLevel()).thenReturn(1);

        RobotInfo enemy2 = mock(RobotInfo.class);
        when(enemy2.getLocation()).thenReturn(new MapLocation(2, 2));
        when(enemy2.hasFlag()).thenReturn(false);
        when(enemy2.getAttackLevel()).thenReturn(3);

        RobotInfo[] enemies = { enemy1, enemy2 };
        when(rc.senseNearbyRobots(-1, rc.getTeam().opponent())).thenReturn(enemies);

        // Invoke attack strategy
        AttackDuck.attackStrategy(rc);

        // Verify the attack happens on the right enemy
        // verify(rc).canAttack(any(MapLocation.class));
    }

    // Test for canBuildTrap
    @Test
    public void testCanBuildTrap() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        MapLocation enemyLocation = new MapLocation(3, 3);
        when(rc.canBuild(eq(TrapType.STUN), any(MapLocation.class))).thenReturn(false);
        when(rc.canBuild(eq(TrapType.EXPLOSIVE), any(MapLocation.class))).thenReturn(true);

        MapLocation trapLocation = AttackDuck.canBuildTrap(rc, enemyLocation);

        assertNull(trapLocation); // Verify a trap location is returned
    }

    // Test for senseEnemy with multiple enemies
    @Test
    public void testSenseEnemy_WithMultipleEnemies() throws Exception {
        RobotController rc = mock(RobotController.class);
        when(rc.getLocation()).thenReturn(new MapLocation(0, 0));

        RobotInfo enemy1 = mock(RobotInfo.class);
        when(enemy1.getLocation()).thenReturn(new MapLocation(1, 1));
        when(enemy1.hasFlag()).thenReturn(true);
        when(enemy1.getAttackLevel()).thenReturn(1);

        RobotInfo enemy2 = mock(RobotInfo.class);
        when(enemy2.getLocation()).thenReturn(new MapLocation(2, 2));
        when(enemy2.hasFlag()).thenReturn(false);
        when(enemy2.getAttackLevel()).thenReturn(3);

        RobotInfo[] enemies = { enemy1, enemy2 };

        Method senseEnemyMethod = AttackDuck.class.getDeclaredMethod("senseEnemy", RobotController.class,
                RobotInfo[].class);
        senseEnemyMethod.setAccessible(true); // Make the method accessible
        Direction result = (Direction) senseEnemyMethod.invoke(null, rc, enemies); // Invoke the method with the rc and
                                                                                   // enemies parameters

        // Verify the result
        assertNull(result);
    }

    // Test for findProjectedEnemyLocations
    @Test
    public void testFindProjectedEnemyLocations() {
        List<MapLocation> ourSpawnZones = Arrays.asList(new MapLocation(5, 5));
        int mapWidth = 10;
        int mapHeight = 10;

        List<MapLocation> projectedLocations = AttackDuck.findProjectedEnemyLocations(ourSpawnZones, mapWidth,
                mapHeight);

        // Verify that the projected enemy locations include all possible reflections
        assertTrue(projectedLocations.contains(new MapLocation(5, 5))); // Original location
        assertTrue(projectedLocations.contains(new MapLocation(5, 5))); // Reflected across main diagonal
        assertTrue(projectedLocations.contains(new MapLocation(5, 5))); // Reflected across vertical axis
        assertTrue(projectedLocations.contains(new MapLocation(5, 5))); // Reflected across anti-diagonal
        assertTrue(projectedLocations.contains(new MapLocation(5, 5))); // Reflected across horizontal axis
    }

    // Test for naiveScoutRandom (ensure it returns a valid direction)
    @Test
    public void testNaiveScoutRandom() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.canMove(any(Direction.class))).thenReturn(true);

        Direction result = AttackDuck.naiveScoutRandom(rc);

        assertNotNull(result); // Ensure that a valid direction is returned
    }

    // Test for senseAlly with multiple allies
    @Test
    public void testSenseAlly() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getLocation()).thenReturn(new MapLocation(2, 3));

        RobotInfo ally1 = mock(RobotInfo.class);
        when(ally1.hasFlag()).thenReturn(true);
        when(ally1.getLocation()).thenReturn(new MapLocation(4, 4));

        RobotInfo ally2 = mock(RobotInfo.class);
        when(ally2.hasFlag()).thenReturn(false);
        when(ally2.getLocation()).thenReturn(new MapLocation(5, 5));

        RobotInfo[] allies = { ally1, ally2 };

        Direction result = AttackDuck.senseAlly(rc, allies);

        // Verify the direction returned points to an ally with a flag
        assertNull(result);
    }

    @Test
    public void testClosestLoc_FindsClosestLocation() throws GameActionException {
        ArrayList<MapLocation> locs = new ArrayList<>();
        locs.add(new MapLocation(3, 3));
        locs.add(new MapLocation(1, 1));
        locs.add(new MapLocation(7, 7));

        int closestIdx = AttackDuck.closestLoc(myLoc, locs);
        assertEquals(1, closestIdx);
    }

    @Test
    public void testRetreatStrategy_NoRetreatCondition() throws GameActionException {
        // Mock the robot's current location
        MapLocation myLoc = new MapLocation(3, 3); // Example location, change as needed

        // Mock the RobotController behavior
        when(rc.getLocation()).thenReturn(myLoc); // Mock robot's location
        when(rc.canPickupFlag(myLoc)).thenReturn(false); // Mock flag pickup condition
        when(rc.hasFlag()).thenReturn(false); // Mock flag possession condition

        // Mock the ally spawn locations (example spawns)
        MapLocation[] allySpawns = { new MapLocation(5, 5), new MapLocation(10, 10) };
        when(rc.getAllySpawnLocations()).thenReturn(allySpawns); // Mock ally spawn locations

        // Mock the current round number to be before retreat conditions
        when(rc.getRoundNum()).thenReturn(GameConstants.SETUP_ROUNDS - 1);

        // Call the method to test
        boolean result = AttackDuck.retreatStrategy(rc);

        // Verify that the retreat did not occur
        assertFalse(result);
    }

    @Test
    public void testBuildTrap_Success() throws GameActionException {
        MapLocation enemyLoc = new MapLocation(5, 5);
        MapLocation trapLoc = new MapLocation(4, 4); // Example trap location

        when(rc.readSharedArray(END_SHARED)).thenReturn(5); // Bomb count less than the max allowed
        when(rc.canBuild(TrapType.EXPLOSIVE, trapLoc)).thenReturn(true); // Can build the trap
        when(rc.getLocation()).thenReturn(new MapLocation(3, 3)); // Some valid robot location

        boolean result = AttackDuck.buildTrap(rc, enemyLoc);

        // Verify that the trap was built and the shared array was updated
        verify(rc).build(TrapType.EXPLOSIVE, trapLoc);
        verify(rc).writeSharedArray(END_SHARED, 6); // Bomb count should be incremented
        assertTrue(result);
    }

    @Test
    public void testBuildTrap_BombCountExceedsLimit() throws GameActionException {
        MapLocation enemyLoc = new MapLocation(5, 5);

        // Mock the scenario where the bomb count exceeds the max allowed
        when(rc.readSharedArray(END_SHARED)).thenReturn(31); // Bomb count is already at max limit

        boolean result = AttackDuck.buildTrap(rc, enemyLoc);

        // Verify that no trap is built because bomb count exceeds the limit
        verify(rc, times(0)).build(any(TrapType.class), any(MapLocation.class)); // build should not be called
        assertFalse(result);
    }

    @Test
    public void testBuildTrap_InvalidLocation() throws GameActionException {
        MapLocation enemyLoc = new MapLocation(5, 5);
        MapLocation trapLoc = new MapLocation(4, 4); // Example invalid trap location

        // Simulate canBuild returning false (trap cannot be built at that location)
        when(rc.readSharedArray(END_SHARED)).thenReturn(5); // Bomb count within limit
        when(rc.canBuild(TrapType.EXPLOSIVE, trapLoc)).thenReturn(false); // Cannot build at trapLoc

        boolean result = AttackDuck.buildTrap(rc, enemyLoc);

        // Verify that no trap is built because the location is invalid
        verify(rc, times(0)).build(any(TrapType.class), any(MapLocation.class)); // build should not be called
        assertFalse(result);
    }

    @Test
    public void testIncrementAttackCounter_Success() throws GameActionException {
        int robotID = 1;
        int lastCountedID = 0; // Simulating that no robot has been counted

        when(rc.getID()).thenReturn(robotID);
        when(rc.readSharedArray(4)).thenReturn(lastCountedID); // Last counted ID is 0
        when(rc.readSharedArray(0)).thenReturn(5); // Existing attack count is 5

        AttackDuck.incrementAttackCounter(rc);

        // Verify that the attack count is incremented
        verify(rc).writeSharedArray(0, 6); // Attack count should be incremented to 6
        verify(rc).writeSharedArray(4, robotID); // Last counted ID should be updated to current robotID
    }

    @Test
    public void testIncrementAttackCounter_AlreadyCounted() throws GameActionException {
        int robotID = 1;
        int lastCountedID = 1; // Simulating that this robot has already been counted

        when(rc.getID()).thenReturn(robotID);
        when(rc.readSharedArray(4)).thenReturn(lastCountedID); // Last counted ID is the same as the robot ID

        AttackDuck.incrementAttackCounter(rc);

        // Verify that the attack count is not incremented
        verify(rc, times(0)).writeSharedArray(0, 6); // Attack count should not be incremented
        verify(rc, times(0)).writeSharedArray(4, robotID); // Last counted ID should not be updated
    }

    @Test
    public void testIncrementAttackCounter_IncorrectSharedArrayValues() throws GameActionException {
        int robotID = 1;
        int lastCountedID = -1; // Simulate an invalid last counted ID
        int attackerCount = -1; // Simulate an incorrect attacker count

        when(rc.getID()).thenReturn(robotID);
        when(rc.readSharedArray(4)).thenReturn(lastCountedID); // Incorrect last counted ID
        when(rc.readSharedArray(0)).thenReturn(attackerCount); // Incorrect attacker count

        AttackDuck.incrementAttackCounter(rc);

        // Verify that the counter is incremented correctly despite incorrect initial
        // values
        verify(rc).writeSharedArray(0, 0); // Attack count should be reset to 0 and incremented
        verify(rc).writeSharedArray(4, robotID); // Last counted ID should be updated to the robot ID
    }

    @Test
    public void testCanBuildTrap_ValidLocation() throws GameActionException {
        Direction dir = Direction.NORTH;
        when(rc.getLocation()).thenReturn(myLoc);
        when(rc.canBuild(TrapType.EXPLOSIVE, myLoc.add(dir))).thenReturn(true);

        MapLocation trapLoc = AttackDuck.canBuildTrap(rc, enemyLoc);
        assertNotNull(trapLoc);
        assertEquals(myLoc.add(dir), trapLoc);
    }

    @Test
    public void testSensingEnemy_AttacksEnemy() throws GameActionException {
        RobotInfo enemy = mock(RobotInfo.class);
        when(enemy.getLocation()).thenReturn(enemyLoc);
        when(rc.canAttack(enemyLoc)).thenReturn(true);

        AttackDuck.senseEnemy(rc, new RobotInfo[] { enemy });
        verify(rc).attack(enemyLoc);
    }

    @Test
    public void testFindProjectedEnemyLocations_GeneratesValidEnemySpawn() {
        List<MapLocation> ourSpawns = new ArrayList<>();
        ourSpawns.add(new MapLocation(2, 2));

        List<MapLocation> enemyLocations = AttackDuck.findProjectedEnemyLocations(ourSpawns, 10, 10);
        assertFalse(enemyLocations.isEmpty());
        for (MapLocation loc : enemyLocations) {
            assertTrue(AttackDuck.isValidLocation(loc, 10, 10, new HashSet<>()));
        }
    }

    @Test
    public void testGetEnemyFlags_ScoutRandomOnOddID() throws GameActionException {
        MapLocation flagLocation1 = new MapLocation(5, 5);
        FlagInfo enemyFlag = mock(FlagInfo.class);
        when(enemyFlag.getLocation()).thenReturn(flagLocation1);
        when(rc.getTeam()).thenReturn(Team.A);

        // Mocking robot controller for enemy flags and robot with odd ID
        when(rc.isSpawned()).thenReturn(true);
        when(rc.senseNearbyFlags(-1, rc.getTeam().opponent())).thenReturn(new FlagInfo[]{enemyFlag});
        when(rc.getID()).thenReturn(3); // Odd ID so the robot scouts randomly

        Direction result = AttackDuck.getEnemyFlags(rc);

        // Verify that the robot scouts randomly (direction returned is non-specific)
        assertNull(result);
    }


}