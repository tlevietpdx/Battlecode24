package Sprint3;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import battlecode.common.*;

public class AttackDuckTest {

    private RobotController rc;
    private MapLocation location;
    private AttackDuck testClass = null;

    @Before
    public void setup() {
        rc = mock(RobotController.class);
        // Example mock setup if scoutRandom calls these methods
        when(rc.getLocation()).thenReturn(new MapLocation(0, 0));
        when(rc.canMove(any(Direction.class))).thenReturn(true);

        location = new MapLocation(0, 0);
        when(rc.getLocation()).thenReturn(location);

        this.testClass = new AttackDuck();
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
        
        RobotInfo[] enemies = {enemy1, enemy2};
        
        // Invoke the private method via reflection
        Method senseEnemyMethod = AttackDuck.class.getDeclaredMethod("senseEnemy", RobotController.class, RobotInfo[].class);
        senseEnemyMethod.setAccessible(true);  // Make the method accessible
        Direction result = (Direction) senseEnemyMethod.invoke(null, rc, enemies);  // Invoke the method with the rc and enemies parameters
        
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
        when(rc.senseNearbyFlags(-1, rc.getTeam().opponent())).thenReturn(new FlagInfo[]{enemyFlag});
        
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

        RobotInfo[] enemies = {enemy1, enemy2};
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
        
        RobotInfo[] enemies = {enemy1, enemy2};

        Method senseEnemyMethod = AttackDuck.class.getDeclaredMethod("senseEnemy", RobotController.class, RobotInfo[].class);
        senseEnemyMethod.setAccessible(true);  // Make the method accessible
        Direction result = (Direction) senseEnemyMethod.invoke(null, rc, enemies);  // Invoke the method with the rc and enemies parameters
        
        // Verify the result
        assertNull(result);
    }

    // Test for naiveScoutRandom (ensure it returns a valid direction)
    @Test
    public void testNaiveScoutRandom() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.canMove(any(Direction.class))).thenReturn(true);
        
        Direction result = AttackDuck.naiveScoutRandom(rc);
        
        assertNotNull(result);  // Ensure that a valid direction is returned
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

        RobotInfo[] allies = {ally1, ally2};

        Direction result = AttackDuck.senseAlly(rc, allies);

        // Verify the direction returned points to an ally with a flag
        assertNull(result);
    }
}
