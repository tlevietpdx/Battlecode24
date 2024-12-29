package Sprint4;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
//import org.mockito.MockedStatic;
//import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.ArrayList;

import battlecode.common.*;

public class HealerDuckTest {
    private RobotController rc;
    private HealerDuck testClass = null;
    private MapLocation location;
    private Clock clock;

    @Before
    public void setup() {
        rc = mock(RobotController.class);
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.getHealAmount()).thenReturn(80);
        when(rc.isActionReady()).thenReturn(true);

        location = new MapLocation(1, 1);
        when(rc.getLocation()).thenReturn(location);

        this.testClass = new HealerDuck();
    }

    private Method getVisibleMethod(String funcName, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException{
        Method method = HealerDuck.class.getDeclaredMethod(funcName, parameterTypes);
        method.setAccessible(true); // Make the private method accessible
        return method;
    }

    @Test
    public void testNoTargets() throws NoSuchMethodException, SecurityException, GameActionException, InvocationTargetException, IllegalAccessException {
        RobotInfo[] nearby_robots = {};
        when(rc.senseNearbyRobots(4, Team.A)).thenReturn(nearby_robots);
        Method method = getVisibleMethod("runHealerStep", RobotController.class);
        MapLocation result = (MapLocation) method.invoke(this.testClass, rc);
        assertNull(result);
    }

    @Test
    public void testNotActionable() throws NoSuchMethodException, SecurityException, GameActionException, InvocationTargetException, IllegalAccessException {
        //Mock a target with 1 hp at location (0, 0)
        MapLocation targetLoc = new MapLocation(0, 0);
        RobotInfo target1 = mock(RobotInfo.class);
        when(target1.getLocation()).thenReturn(targetLoc);
        when(target1.getHealth()).thenReturn(1);
        when(target1.hasFlag()).thenReturn(false);

        RobotInfo[] nearby_robots = {target1};
        when(rc.senseNearbyRobots(4, Team.A)).thenReturn(nearby_robots);
        //Make sure robot is not actionable
        when(rc.isActionReady()).thenReturn(false);
        Method method = getVisibleMethod("runHealerStep", RobotController.class);
        MapLocation result = (MapLocation) method.invoke(this.testClass, rc);
        assertNull(result);
    }

    @Test

    public void testOneTarget() throws NoSuchMethodException, SecurityException, GameActionException, InvocationTargetException, IllegalAccessException {
        //Mock a target with 1 hp at location (0, 0)
        MapLocation targetLoc = new MapLocation(0, 0);
        RobotInfo target1 = mock(RobotInfo.class);
        when(target1.getLocation()).thenReturn(targetLoc);
        when(target1.getHealth()).thenReturn(1);
        when(target1.hasFlag()).thenReturn(false);

        RobotInfo[] nearby_robots = {target1};
        when(rc.senseNearbyRobots(4, Team.A)).thenReturn(nearby_robots);
        Method method = getVisibleMethod("runHealerStep", RobotController.class);
        MapLocation result = (MapLocation) method.invoke(this.testClass, rc);
        assertEquals(targetLoc, result);
    }

    @Test
    public void testFullHealthTarget() throws NoSuchMethodException, SecurityException, GameActionException, InvocationTargetException, IllegalAccessException {
        //Mock a target with 1000 hp at location (0, 0)
        MapLocation targetLoc = new MapLocation(0, 0);
        RobotInfo target1 = mock(RobotInfo.class);
        when(target1.getLocation()).thenReturn(targetLoc);
        when(target1.getHealth()).thenReturn(1000);
        when(target1.hasFlag()).thenReturn(false);

        RobotInfo[] nearby_robots = {target1};
        when(rc.senseNearbyRobots(4, Team.A)).thenReturn(nearby_robots);
        Method method = getVisibleMethod("runHealerStep", RobotController.class);
        MapLocation result = (MapLocation) method.invoke(this.testClass, rc);
        assertNull(result);
    }

    @Test
    public void testPrioritizeLowHealth() throws NoSuchMethodException, SecurityException, GameActionException, InvocationTargetException, IllegalAccessException {
        //Mock a target with 500 hp at location (0, 0)
        MapLocation target1Loc = new MapLocation(0, 0);
        RobotInfo target1 = mock(RobotInfo.class);
        when(target1.getLocation()).thenReturn(target1Loc);
        when(target1.getHealth()).thenReturn(500);
        when(target1.hasFlag()).thenReturn(false);

        //Mock a target with 100 hp at location (2, 2)
        MapLocation target2Loc = new MapLocation(2, 2);
        RobotInfo target2 = mock(RobotInfo.class);
        when(target2.getLocation()).thenReturn(target2Loc);
        when(target2.getHealth()).thenReturn(100);
        when(target2.hasFlag()).thenReturn(false);

        RobotInfo[] nearby_robots = {target1, target2};
        when(rc.senseNearbyRobots(4, Team.A)).thenReturn(nearby_robots);
        Method method = getVisibleMethod("runHealerStep", RobotController.class);
        MapLocation result = (MapLocation) method.invoke(this.testClass, rc);
        assertEquals(target2Loc, result);
    }

    @Test
    public void testPrioritizeFlag() throws NoSuchMethodException, SecurityException, GameActionException, InvocationTargetException, IllegalAccessException {
        //Mock a target with 200 hp at location (0, 0) with no flag
        MapLocation target1Loc = new MapLocation(0, 0);
        RobotInfo target1 = mock(RobotInfo.class);
        when(target1.getLocation()).thenReturn(target1Loc);
        when(target1.getHealth()).thenReturn(200);
        when(target1.hasFlag()).thenReturn(false);

        //Mock a target with 300 hp at location (2, 2) with the flag
        MapLocation target2Loc = new MapLocation(2, 2);
        RobotInfo target2 = mock(RobotInfo.class);
        when(target2.getLocation()).thenReturn(target2Loc);
        when(target2.getHealth()).thenReturn(300);
        when(target2.hasFlag()).thenReturn(true);

        RobotInfo[] nearby_robots = {target1, target2};
        when(rc.senseNearbyRobots(4, Team.A)).thenReturn(nearby_robots);
        Method method = getVisibleMethod("runHealerStep", RobotController.class);
        MapLocation result = (MapLocation) method.invoke(this.testClass, rc);
        assertEquals(target2Loc, result);
    }

    @Test
    public void testPrioritizeFurthest() throws NoSuchMethodException, SecurityException, GameActionException, InvocationTargetException, IllegalAccessException {
        //Mock a target with 200 hp at location (0, 0) with no flag
        MapLocation target1Loc = new MapLocation(0, 0);
        RobotInfo target1 = mock(RobotInfo.class);
        when(target1.getLocation()).thenReturn(target1Loc);
        when(target1.getHealth()).thenReturn(200);
        when(target1.hasFlag()).thenReturn(false);

        //Mock a target with 200 hp at location (3, 2) with the flag
        MapLocation target2Loc = new MapLocation(3, 2);
        RobotInfo target2 = mock(RobotInfo.class);
        when(target2.getLocation()).thenReturn(target2Loc);
        when(target2.getHealth()).thenReturn(200);
        when(target2.hasFlag()).thenReturn(false);

        RobotInfo[] nearby_robots = {target1, target2};
        when(rc.senseNearbyRobots(4, Team.A)).thenReturn(nearby_robots);
        Method method = getVisibleMethod("runHealerStep", RobotController.class);
        MapLocation result = (MapLocation) method.invoke(this.testClass, rc);
        assertEquals(target2Loc, result);
    }
}
