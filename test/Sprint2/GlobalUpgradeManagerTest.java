package Sprint2;

import battlecode.common.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class GlobalUpgradeManagerTest {

    @Mock
    private RobotController rc;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testTryPurchaseUpgrades_WhenRoundIs600_ShouldAttemptAllUpgrades() throws GameActionException {
        // Arrange
        when(rc.canBuyGlobal(any(GlobalUpgrade.class))).thenReturn(true);

        // Act
        GlobalUpgradeManager.tryPurchaseUpgrades(rc, 600);

        // Assert
        verify(rc).canBuyGlobal(GlobalUpgrade.ATTACK);
        verify(rc).canBuyGlobal(GlobalUpgrade.CAPTURING);
        verify(rc).canBuyGlobal(GlobalUpgrade.HEALING);
        verify(rc).buyGlobal(GlobalUpgrade.ATTACK);
        verify(rc).buyGlobal(GlobalUpgrade.CAPTURING);
        verify(rc).buyGlobal(GlobalUpgrade.HEALING);
    }

    @Test
    public void testTryPurchaseUpgrades_WhenRoundIsNot600_ShouldNotAttemptUpgrades() throws GameActionException {
        // Act
        GlobalUpgradeManager.tryPurchaseUpgrades(rc, 601);

        // Assert
        verify(rc, never()).canBuyGlobal(any(GlobalUpgrade.class));
        verify(rc, never()).buyGlobal(any(GlobalUpgrade.class));
    }

    @Test
    public void testTryPurchaseUpgrades_WhenCannotBuyUpgrade_ShouldNotPurchase() throws GameActionException {
        // Arrange
        when(rc.canBuyGlobal(any(GlobalUpgrade.class))).thenReturn(false);

        // Act
        GlobalUpgradeManager.tryPurchaseUpgrades(rc, 600);

        // Assert
        verify(rc, times(3)).canBuyGlobal(any(GlobalUpgrade.class));
        verify(rc, never()).buyGlobal(any(GlobalUpgrade.class));
    }

    @Test
    public void testTryPurchaseUpgrades_WhenSomeUpgradesAvailable_ShouldPurchaseSelectively() throws GameActionException {
        // Arrange
        when(rc.canBuyGlobal(GlobalUpgrade.ATTACK)).thenReturn(true);
        when(rc.canBuyGlobal(GlobalUpgrade.CAPTURING)).thenReturn(false);
        when(rc.canBuyGlobal(GlobalUpgrade.HEALING)).thenReturn(true);

        // Act
        GlobalUpgradeManager.tryPurchaseUpgrades(rc, 600);

        // Assert
        verify(rc).buyGlobal(GlobalUpgrade.ATTACK);
        verify(rc, never()).buyGlobal(GlobalUpgrade.CAPTURING);
        verify(rc).buyGlobal(GlobalUpgrade.HEALING);
    }

    @Test
    public void testTryPurchaseUpgrades_WhenRoundIs1200_ShouldAttemptUpgrades() throws GameActionException {
        // Arrange
        when(rc.canBuyGlobal(any(GlobalUpgrade.class))).thenReturn(true);

        // Act
        GlobalUpgradeManager.tryPurchaseUpgrades(rc, 1200);

        // Assert
        verify(rc, times(3)).canBuyGlobal(any(GlobalUpgrade.class));
        verify(rc, times(3)).buyGlobal(any(GlobalUpgrade.class));
    }
}