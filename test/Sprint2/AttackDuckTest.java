package Sprint2;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import battlecode.common.*;

public class AttackDuckTest {

    private RobotController rc;
    private MapLocation location;
    private AttackDuck testClass = null;

    @Before
    public void setup() {
        rc = mock(RobotController.class);
        location = new MapLocation(0, 0);
        when(rc.getLocation()).thenReturn(location);
        when(rc.canMove(any(Direction.class))).thenReturn(true);
        when(rc.canPickupFlag(any(MapLocation.class))).thenReturn(true);
        when(rc.getTeam()).thenReturn(Team.A);
        
        this.testClass = new AttackDuck();
    }

    private Method getVisibleMethod(String funcName, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        Method method = AttackDuck.class.getDeclaredMethod(funcName, parameterTypes);
        method.setAccessible(true); // Make the private method accessible
        return method;
    }

    @Test
    public void testSanity() {
        assertEquals(2, 1 + 1);
    }

    @Test
    public void testClosestLoc() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        MapLocation myLoc = new MapLocation(0, 0);
        ArrayList<MapLocation> targetLocs = new ArrayList<>();
        targetLocs.add(new MapLocation(10, 10));
        targetLocs.add(new MapLocation(5, 5));
        targetLocs.add(new MapLocation(1, 1));

        Method method = getVisibleMethod("closestLoc", MapLocation.class, ArrayList.class);
        int closestIndex = (int) method.invoke(this.testClass, myLoc, targetLocs);
        assertEquals(2, closestIndex);  // (1,1) should be the closest
    }

    @Test
    public void testScoutRandom() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = getVisibleMethod("scoutRandom", RobotController.class);
        Direction result = (Direction) method.invoke(this.testClass, rc);
        assertNotNull(result);
    }

    @Test
    public void testRetreatStrategy() throws GameActionException {
        // Setup the mock conditions for retreat strategy
        when(rc.hasFlag()).thenReturn(true);
        when(rc.getRoundNum()).thenReturn(GameConstants.SETUP_ROUNDS + 1);
        
        MapLocation[] allySpawns = { new MapLocation(2, 2), new MapLocation(3, 3) };
        when(rc.getAllySpawnLocations()).thenReturn(allySpawns);

        boolean result = AttackDuck.retreatStrategy(rc);
        assertTrue(result);
    }

    // @Test
    // public void testSenseCrumbs() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, GameActionException {
    //     Method method = getVisibleMethod("senseCrumbs", RobotController.class, Direction.class);
    //     MapLocation crumbLoc = new MapLocation(1, 1);
    //     MapLocation[] locationsWithCrumbs = { crumbLoc };
        
    //     when(rc.getAllLocationsWithinRadiusSquared(any(MapLocation.class), any(Integer.class)))
    //             .thenReturn(locationsWithCrumbs);
        
    //     MapInfo mapInfoWithCrumbs = mock(MapInfo.class);
    //     when(mapInfoWithCrumbs.getCrumbs()).thenReturn(5);
    //     when(rc.senseMapInfo(crumbLoc)).thenReturn(mapInfoWithCrumbs);
        
    //     Direction result = (Direction) method.invoke(this.testClass, rc, null);
    //     assertNotNull(result);
    // }

}
