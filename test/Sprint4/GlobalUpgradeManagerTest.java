package Sprint4;

import battlecode.common.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class GlobalUpgradeManagerTest {
    private RobotController rc;
    private Team allyTeam;
    private GlobalUpgradeManager manager;

    @Before
    public void setUp() {
        rc = mock(RobotController.class);
        allyTeam = Team.A;
        when(rc.getTeam()).thenReturn(allyTeam);
        when(rc.getHealAmount()).thenReturn(80);
        // Setup default values for cached checks
        when(rc.canBuyGlobal(any(GlobalUpgrade.class))).thenReturn(false);
        when(rc.getRoundNum()).thenReturn(0);
        manager = new GlobalUpgradeManager(rc);
    }

    @Test
    public void testEarlyGameUpgrade() throws GameActionException {
        // Setup
        when(rc.getRoundNum()).thenReturn(600); // EARLY_GAME_THRESHOLD
        when(rc.canBuyGlobal(GlobalUpgrade.ATTACK)).thenReturn(true);
        when(rc.readSharedArray(GlobalUpgradeManager.SHARED_ARRAY_ATTACKER_COUNT))
                .thenReturn(15);

        // Execute
        manager.tryPurchaseUpgrades();

        // Verify
        verify(rc).buyGlobal(GlobalUpgrade.ATTACK);
    }

    @Test
    public void testEarlyGameUpgradeInsufficientAttackers() throws GameActionException {
        // Setup
        when(rc.getRoundNum()).thenReturn(600);
        when(rc.canBuyGlobal(GlobalUpgrade.ATTACK)).thenReturn(true);
        when(rc.readSharedArray(GlobalUpgradeManager.SHARED_ARRAY_ATTACKER_COUNT))
                .thenReturn(14);

        // Execute
        manager.tryPurchaseUpgrades();

        // Verify
        verify(rc, never()).buyGlobal(any(GlobalUpgrade.class));
    }

    @Test
    public void testMidGameHealingUpgrade() throws GameActionException {
        // Setup
        when(rc.getRoundNum()).thenReturn(1000);
        when(rc.canBuyGlobal(GlobalUpgrade.HEALING)).thenReturn(true);
        when(rc.readSharedArray(GlobalUpgradeManager.SHARED_ARRAY_WOUNDED_COUNT))
                .thenReturn(7);

        // Execute
        manager.tryPurchaseUpgrades();

        // Verify
        verify(rc).buyGlobal(GlobalUpgrade.HEALING);
    }

    @Test
    public void testMidGameHealingUpgradeInsufficientWounded() throws GameActionException {
        // Setup
        when(rc.getRoundNum()).thenReturn(1000);
        when(rc.canBuyGlobal(GlobalUpgrade.HEALING)).thenReturn(true);
        when(rc.readSharedArray(GlobalUpgradeManager.SHARED_ARRAY_WOUNDED_COUNT))
                .thenReturn(6);

        // Execute
        manager.tryPurchaseUpgrades();

        // Verify
        verify(rc, never()).buyGlobal(any(GlobalUpgrade.class));
    }

    @Test
    public void testMidGameCapturingUpgrade() throws GameActionException {
        // Setup
        when(rc.getRoundNum()).thenReturn(1000);
        when(rc.canBuyGlobal(GlobalUpgrade.CAPTURING)).thenReturn(true);
        when(rc.readSharedArray(GlobalUpgradeManager.SHARED_ARRAY_FLAGS_CAPTURED))
                .thenReturn(1);

        // Execute
        manager.tryPurchaseUpgrades();

        // Verify
        verify(rc).buyGlobal(GlobalUpgrade.CAPTURING);
    }

    @Test
    public void testMidGameCapturingUpgradeWhenRetreating() throws GameActionException {
        // Setup
        when(rc.getRoundNum()).thenReturn(1000);
        when(rc.canBuyGlobal(GlobalUpgrade.CAPTURING)).thenReturn(true);
        when(rc.readSharedArray(GlobalUpgradeManager.SHARED_ARRAY_FLAGS_CAPTURED))
                .thenReturn(0);
        when(rc.readSharedArray(GlobalUpgradeManager.SHARED_ARRAY_IS_RETREATING))
                .thenReturn(1);

        // Execute
        manager.tryPurchaseUpgrades();

        // Verify
        verify(rc).buyGlobal(GlobalUpgrade.CAPTURING);
    }

    @Test
    public void testLateGameUpgrades() throws GameActionException {
        // Setup
        when(rc.getRoundNum()).thenReturn(1800);
        when(rc.canBuyGlobal(GlobalUpgrade.ATTACK)).thenReturn(true);
        when(rc.canBuyGlobal(GlobalUpgrade.CAPTURING)).thenReturn(true);
        when(rc.canBuyGlobal(GlobalUpgrade.HEALING)).thenReturn(true);

        // Execute
        manager.tryPurchaseUpgrades();

        // Verify priority order
        verify(rc).buyGlobal(GlobalUpgrade.ATTACK);
        verify(rc, never()).buyGlobal(GlobalUpgrade.CAPTURING);
        verify(rc, never()).buyGlobal(GlobalUpgrade.HEALING);
    }

    @Test
    public void testUpdateWoundedCount() throws GameActionException {
        // Setup
        when(rc.isSpawned()).thenReturn(true);
        MapLocation location = new MapLocation(0, 0);
        RobotInfo[] allies = {
                new RobotInfo(1, allyTeam, 75, location, false, 1, 1, 1),
                new RobotInfo(2, allyTeam, 1000, location, false, 1, 1, 1),
                new RobotInfo(3, allyTeam, 50, location, false, 1, 1, 1)
        };

        when(rc.senseNearbyRobots(anyInt(), eq(allyTeam))).thenReturn(allies);

        // Execute
        manager.updateWoundedCount();

        // Verify
        verify(rc).writeSharedArray(GlobalUpgradeManager.SHARED_ARRAY_WOUNDED_COUNT, 2);
    }

    @Test
    public void testUpdateWoundedCountWithNullAllies() throws GameActionException {
        // Setup
        when(rc.isSpawned()).thenReturn(true);
        when(rc.senseNearbyRobots(anyInt(), eq(allyTeam))).thenReturn(null);

        // Execute
        manager.updateWoundedCount();

        // Verify - we expect a write of 0 when no allies are found
        verify(rc).writeSharedArray(GlobalUpgradeManager.SHARED_ARRAY_WOUNDED_COUNT, 0);
    }

    @Test
    public void testResetWoundedCount() throws GameActionException {
        // Setup
        when(rc.getRoundNum()).thenReturn(50);

        // Execute
        manager.resetWoundedCount();

        // Verify
        verify(rc).writeSharedArray(GlobalUpgradeManager.SHARED_ARRAY_WOUNDED_COUNT, 0);
    }

    @Test
    public void testUpdateHealThreshold() throws GameActionException {
        // Setup
        when(rc.getHealAmount()).thenReturn(80);
        when(rc.isSpawned()).thenReturn(true);
        MapLocation location = new MapLocation(0, 0);
        RobotInfo[] allies = {
                new RobotInfo(1, allyTeam, 860, location, false, 1, 1, 1),
                new RobotInfo(2, allyTeam, 851, location, false, 1, 1, 1),
                new RobotInfo(3, allyTeam, 849, location, false, 1, 1, 1)
        };

        when(rc.senseNearbyRobots(anyInt(), eq(allyTeam))).thenReturn(allies);

        // Execute
        manager.updateHealThreshold();
        manager.updateWoundedCount();

        // Verify
        verify(rc).writeSharedArray(GlobalUpgradeManager.SHARED_ARRAY_WOUNDED_COUNT, 3);
    }

    @Test
    public void testNoAvailableUpgrades() throws GameActionException {
        // Setup
        when(rc.getRoundNum()).thenReturn(1000);
        when(rc.canBuyGlobal(any(GlobalUpgrade.class))).thenReturn(false);

        // Execute
        manager.tryPurchaseUpgrades();

        // Verify
        verify(rc, never()).buyGlobal(any(GlobalUpgrade.class));
    }

    @Test
    public void testLateGameUpgradesOnlyCapturingAvailable() throws GameActionException {
        // Setup
        when(rc.getRoundNum()).thenReturn(1800);
        when(rc.canBuyGlobal(GlobalUpgrade.ATTACK)).thenReturn(false);
        when(rc.canBuyGlobal(GlobalUpgrade.CAPTURING)).thenReturn(true);
        when(rc.canBuyGlobal(GlobalUpgrade.HEALING)).thenReturn(false);

        // Execute
        manager.tryPurchaseUpgrades();

        // Verify
        verify(rc).buyGlobal(GlobalUpgrade.CAPTURING);
    }

    @Test
    public void testGameActionExceptionInUpdateWoundedCount() throws GameActionException {
        // Setup
        when(rc.isSpawned()).thenReturn(true);
        when(rc.senseNearbyRobots(anyInt(), eq(allyTeam)))
                .thenThrow(new GameActionException(GameActionExceptionType.CANT_DO_THAT, "Test exception"));

        // Execute
        manager.updateWoundedCount();

        // Verify no exceptions are thrown and method handles the error gracefully
        verify(rc, never()).writeSharedArray(anyInt(), anyInt());
    }

    @Test
    public void testGameActionExceptionInResetWoundedCount() throws GameActionException {
        // Setup
        when(rc.getRoundNum()).thenReturn(50);
        doThrow(new GameActionException(GameActionExceptionType.CANT_DO_THAT, "Test exception"))
                .when(rc).writeSharedArray(eq(GlobalUpgradeManager.SHARED_ARRAY_WOUNDED_COUNT), eq(0));

        // Execute
        manager.resetWoundedCount();

        // Verify method called the write attempt despite the exception
        verify(rc).writeSharedArray(GlobalUpgradeManager.SHARED_ARRAY_WOUNDED_COUNT, 0);
    }
}