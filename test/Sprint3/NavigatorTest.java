package Sprint3;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import battlecode.common.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;


public class NavigatorTest {

    private RobotController rc;

    @Before
    public void setUp() {
        rc = mock(RobotController.class);
    }

    @Test
    public void testMarch2Target_Success() throws GameActionException {
        // Mock the RobotController location and target
        MapLocation start = new MapLocation(0, 0);
        MapLocation target = new MapLocation(1, 0);

        when(rc.getLocation()).thenReturn(start);
        when(rc.canMove(Direction.EAST)).thenReturn(true);

        // Call march2Target and verify behavior
        Direction result = Navigator.march2Target(rc, target);
        assertEquals(Direction.EAST, result);

        verify(rc).move(Direction.EAST);
    }

    @Test
    public void testMarch2Target_Stuck() throws GameActionException {
        // Simulate being stuck at a location
        MapLocation stuckLoc = new MapLocation(0, 0);
        when(rc.getLocation()).thenReturn(stuckLoc);

        // Simulate no walkable directions
        when(rc.canMove(any(Direction.class))).thenReturn(false);

        // Call march2Target and verify it returns null when stuck
        Direction result = Navigator.march2Target(rc, stuckLoc);
        assertNull(result);
    }

    @Test
    public void testMarch2Target_RandomFallback() throws GameActionException {
        // Mock starting at (0,0) with a target at (5,5)
        MapLocation start = new MapLocation(0, 0);
        MapLocation target = new MapLocation(5, 5);
        when(rc.getLocation()).thenReturn(start);

        // Simulate all prioritized directions as blocked, fallback should use random
        when(rc.canMove(any(Direction.class))).thenReturn(false);
        when(rc.canMove(Direction.NORTH)).thenReturn(true);

        // Call march2Target
        Direction result = Navigator.march2Target(rc, target);

        // Verify random fallback
        assertEquals(Direction.NORTH, result);
    }
}
