package Sprint4;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import battlecode.common.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

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

    @Test
    public void testMarch2Target_PrioritizedDirections() throws GameActionException {
        // Mock a target to the northeast
        MapLocation start = new MapLocation(0, 0);
        MapLocation target = new MapLocation(1, 1);
        when(rc.getLocation()).thenReturn(start);

        // Simulate movement options
        when(rc.canMove(Direction.NORTHEAST)).thenReturn(true);
        when(rc.canMove(Direction.NORTH)).thenReturn(true);
        when(rc.canMove(Direction.EAST)).thenReturn(true);

        // Call march2Target and check if it chooses the best direction
        Direction result = Navigator.march2Target(rc, target);
        assertEquals(Direction.NORTHEAST, result);
    }

    @Test
    public void testMarch2Target_PreferFillWhenBlocked() throws GameActionException {
        // Mock a starting location and target
        MapLocation start = new MapLocation(0, 0);
        MapLocation target = new MapLocation(1, 0);

        // Set up RobotController to be unable to move east but able to fill
        when(rc.getLocation()).thenReturn(start);
        when(rc.canMove(Direction.EAST)).thenReturn(false);
        when(rc.canFill(new MapLocation(1, 0))).thenReturn(true);

        // Call march2Target and ensure it fills the location when blocked
        Direction result = Navigator.march2Target(rc, target);
        assertEquals(Direction.EAST, result);

        verify(rc).fill(new MapLocation(1, 0));
    }

    // @Test
    // public void testMarch2Target_FallbackToOtherDirection() throws GameActionException {
    //     // Mock a starting location and target
    //     MapLocation start = new MapLocation(0, 0);
    //     MapLocation target = new MapLocation(1, 1);

    //     when(rc.getLocation()).thenReturn(start);

    //     // Simulate inability to move in the correct direction but able to move west
    //     when(rc.canMove(Direction.NORTHEAST)).thenReturn(false);
    //     when(rc.canMove(Direction.WEST)).thenReturn(true);

    //     // Call march2Target and verify it falls back to a valid direction
    //     Direction result = Navigator.march2Target(rc, target);
    //     assertEquals(Direction.WEST, result);
    // }

    @Test
    public void testMarch2Target_AllDirectionsBlocked() throws GameActionException {
        // Mock the starting location and target
        MapLocation start = new MapLocation(0, 0);
        MapLocation target = new MapLocation(1, 0);

        when(rc.getLocation()).thenReturn(start);

        // Simulate all directions being blocked
        when(rc.canMove(any(Direction.class))).thenReturn(false);

        // Call march2Target and verify it returns null when all directions are blocked
        Direction result = Navigator.march2Target(rc, target);
        assertNull(result);
    }
}
