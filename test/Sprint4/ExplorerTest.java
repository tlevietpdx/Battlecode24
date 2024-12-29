package Sprint4;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import battlecode.common.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

public class ExplorerTest {

    private RobotController rc;

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

    @Before
    public void setUp() {
        rc = mock(RobotController.class);
    }

    @Test
    public void testMoveable_Success() throws GameActionException {
        // Mock the RobotController's ability to move in a direction
        when(rc.canMove(Direction.NORTH)).thenReturn(true);
        when(rc.getLocation()).thenReturn(new MapLocation(0, 0));

        // Call the moveable method and verify it returns true
        boolean result = Explorer.moveable(rc, Direction.NORTH);
        assertTrue(result);

        verify(rc).canMove(Direction.NORTH);
    }

    @Test
    public void testWalkable_Success() throws GameActionException {
        // Mock the RobotController's ability to move in a direction
        MapLocation location = new MapLocation(0, 0);
        when(rc.getLocation()).thenReturn(location);
        when(rc.canMove(Direction.EAST)).thenReturn(true);

        // Call walkable and verify the robot moves east
        boolean result = Explorer.walkable(rc, Direction.EAST);
        assertTrue(result);

        verify(rc).move(Direction.EAST);
    }

    @Test
    public void testGetPrioritizedDirection_Success() throws GameActionException {
        // Mock directions around the robot
        when(rc.canMove(Direction.NORTH)).thenReturn(true);
        when(rc.canMove(Direction.NORTHEAST)).thenReturn(true);
        when(rc.getLocation()).thenReturn(new MapLocation(0, 0));


        // Call getPrioritizedDirection
        ArrayList<Direction> directions = Explorer.getPrioritizedDirection(rc, Direction.NORTH);

        // Ensure the result contains valid directions
        assertNotNull(directions);
        assertTrue(directions.contains(Direction.NORTH) || directions.contains(Direction.NORTHEAST));
    }

    @Test
    public void testMarch2Target_Success() throws GameActionException {
        // Mock the RobotController location and target
        MapLocation start = new MapLocation(0, 0);
        MapLocation target = new MapLocation(1, 0);

        when(rc.getLocation()).thenReturn(start);
        when(rc.canMove(Direction.EAST)).thenReturn(true);

        // Call march2Target and verify behavior
        Direction result = Explorer.march2Target(rc, target);
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
        Direction result = Explorer.march2Target(rc, stuckLoc);
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
        Direction result = Explorer.march2Target(rc, target);

        // Verify random fallback
        assertEquals(Direction.NORTH, result);
    }
}
