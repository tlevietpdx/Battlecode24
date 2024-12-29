package Sprint3;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import battlecode.common.*;

public class BuilderDuckTest {

    private RobotController rc;
    private MapLocation location;
    private BuilderDuck testClass = null;
    private MapLocation goodDig,badDig,goodFill,badFill,goodBuild,badBuild;
    private Direction goodToward, badToward;

    @Before
    public void setup() {
        rc = mock(RobotController.class);

        // Example mock setup if BuilderDuck calls these methods
        when(rc.getLocation()).thenReturn(new MapLocation(1,1));
        when(rc.canMove(any(Direction.class))).thenReturn(true);
        when(rc.canDig(any(MapLocation.class))).thenReturn(true);
        when(rc.canFill(any(MapLocation.class))).thenReturn(true);

        location = new MapLocation(0, 0);
        goodDig = new MapLocation(1,1);
        badDig = new MapLocation(0,0);
        goodFill = new MapLocation(1,1);
        badFill = new MapLocation(0,0);
        goodBuild = new MapLocation(1,1);
        badBuild = new MapLocation(-1,-1);
        goodToward = Direction.NORTHEAST;
        badToward = Direction.NORTH;
        when(rc.getLocation()).thenReturn(location);
        when(rc.canDig(goodDig)).thenReturn(true);
        when(rc.canFill(goodFill)).thenReturn(true);
        when(rc.canDig(badDig)).thenReturn(false);
        when(rc.canFill(badFill)).thenReturn(false);
        when(rc.canBuild(TrapType.WATER, goodBuild)).thenReturn(true);
        when(rc.canBuild(TrapType.WATER, badBuild)).thenReturn(false);

        this.testClass = new BuilderDuck();
    }

    private Method getVisibleMethod(String funcName, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException{
        Method method = AttackDuck.class.getDeclaredMethod(funcName, parameterTypes);
        method.setAccessible(true); // Make the private method accessible
        return method;
    }

    @Test
    public void testSanity() {
        assertEquals(2, 1 + 1);
    }

    @Test
    public void testDigWaterHole() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = BuilderDuck.class.getDeclaredMethod("digWaterHole",RobotController.class,MapLocation.class);
        Boolean goodresult = (Boolean) method.invoke(this.testClass,rc, goodDig);
        Boolean badresult = (Boolean) method.invoke(this.testClass,rc,badDig);
        assertTrue(goodresult);
        assertFalse(badresult);
    }

    @Test
    public void testFillWaterHole() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = BuilderDuck.class.getDeclaredMethod("fillWaterHole",RobotController.class,MapLocation.class);
        Boolean goodresult = (Boolean) method.invoke(this.testClass,rc, goodFill);
        Boolean badresult = (Boolean) method.invoke(this.testClass,rc, badFill);
        assertTrue(goodresult);
        assertFalse(badresult);
    } 

    @Test
    public void testBuildTrapInDirection() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        TrapType goodTrap = TrapType.WATER;
        Method method = BuilderDuck.class.getDeclaredMethod("buildTrapInDirection",RobotController.class,Direction.class,TrapType.class);
        MapLocation goodMapLoc = (MapLocation) method.invoke(this.testClass,rc,goodToward,goodTrap);
        MapLocation badMapLoc = (MapLocation) method.invoke(this.testClass,rc,badToward,goodTrap);
        assertEquals(goodMapLoc, goodBuild);
        assertEquals(badMapLoc, badBuild);
    }
}