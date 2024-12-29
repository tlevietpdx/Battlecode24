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

public class ScoutMovesTest {

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
}
