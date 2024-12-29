package Sprint3;

import battlecode.common.*;

public class GlobalUpgradeManager {
    private static final int MIN_ATTACKERS_FOR_ATTACK_UPGRADE = 15;
    private static final int MIN_WOUNDED_FOR_HEALING = 7;
    private static final int LATE_GAME_ROUND = 1800;
    private static final int EARLY_GAME_THRESHOLD = 600;
    private static final int WOUNDED_RESET_INTERVAL = 50;
    private static final int MAX_HEALTH = 1000;

    // Shared array indices
    public static final int SHARED_ARRAY_ATTACKER_COUNT = 0;
    public static final int SHARED_ARRAY_WOUNDED_COUNT = 1;
    public static final int SHARED_ARRAY_FLAGS_CAPTURED = 2;
    public static final int SHARED_ARRAY_IS_RETREATING = 3;

    private final RobotController rc;
    private int woundedHealthThreshold;
    private GameState gameState;
    private UpgradeAvailability upgradeAvailability;

    public GlobalUpgradeManager(RobotController rc) {
        this.rc = rc;
        this.woundedHealthThreshold = MAX_HEALTH - rc.getHealAmount();
        this.upgradeAvailability = new UpgradeAvailability();
        refreshCachedValues();
    }

    private static class UpgradeAvailability {
        boolean attack;
        boolean capturing;
        boolean healing;

        boolean hasAnyAvailable() {
            return attack || capturing || healing;
        }
    }

    private enum GameState {
        EARLY_GAME,
        MID_GAME,
        LATE_GAME
    }

    private void refreshCachedValues() {
        updateGameState();
        updateUpgradeAvailability();
    }

    private void updateGameState() {
        int currentRound = rc.getRoundNum();
        if (currentRound >= LATE_GAME_ROUND) {
            gameState = GameState.LATE_GAME;
        } else if (currentRound == EARLY_GAME_THRESHOLD) {
            gameState = GameState.EARLY_GAME;
        } else {
            gameState = GameState.MID_GAME;
        }
    }

    private void updateUpgradeAvailability() {
        upgradeAvailability.attack = rc.canBuyGlobal(GlobalUpgrade.ATTACK);
        upgradeAvailability.capturing = rc.canBuyGlobal(GlobalUpgrade.CAPTURING);
        upgradeAvailability.healing = rc.canBuyGlobal(GlobalUpgrade.HEALING);
    }

    public void tryPurchaseUpgrades() throws GameActionException {
        refreshCachedValues();

        if (!upgradeAvailability.hasAnyAvailable()) {
            return;
        }

        switch (gameState) {
            case LATE_GAME:
                purchaseLateGameUpgrades();
                break;
            case EARLY_GAME:
                purchaseEarlyGameUpgrades();
                break;
            case MID_GAME:
                purchaseMidGameUpgrades();
                break;
        }
    }

    private void purchaseEarlyGameUpgrades() throws GameActionException {
        if (upgradeAvailability.attack && hasEnoughAttackers()) {
            buyUpgrade(GlobalUpgrade.ATTACK);
        }
    }

    private void purchaseMidGameUpgrades() throws GameActionException {
        if (shouldBuyHealing()) {
            buyUpgrade(GlobalUpgrade.HEALING);
        } else if (shouldBuyCapturing()) {
            buyUpgrade(GlobalUpgrade.CAPTURING);
        } else if (shouldBuyAttack()) {
            buyUpgrade(GlobalUpgrade.ATTACK);
        }
    }

    private void purchaseLateGameUpgrades() throws GameActionException {
        if (upgradeAvailability.attack) {
            buyUpgrade(GlobalUpgrade.ATTACK);
        } else if (upgradeAvailability.capturing) {
            buyUpgrade(GlobalUpgrade.CAPTURING);
        } else if (upgradeAvailability.healing) {
            buyUpgrade(GlobalUpgrade.HEALING);
        }
    }

    private boolean hasEnoughAttackers() throws GameActionException {
        return rc.readSharedArray(SHARED_ARRAY_ATTACKER_COUNT) >= MIN_ATTACKERS_FOR_ATTACK_UPGRADE;
    }

    private boolean shouldBuyHealing() throws GameActionException {
        return upgradeAvailability.healing &&
                rc.readSharedArray(SHARED_ARRAY_WOUNDED_COUNT) >= MIN_WOUNDED_FOR_HEALING;
    }

    private boolean shouldBuyCapturing() throws GameActionException {
        return upgradeAvailability.capturing &&
                (rc.readSharedArray(SHARED_ARRAY_FLAGS_CAPTURED) > 0 ||
                        rc.readSharedArray(SHARED_ARRAY_IS_RETREATING) == 1);
    }

    private boolean shouldBuyAttack() throws GameActionException {
        return upgradeAvailability.attack && hasEnoughAttackers();
    }

    private void buyUpgrade(GlobalUpgrade upgrade) throws GameActionException {
        rc.buyGlobal(upgrade);
    }

    public void updateWoundedCount() {
        if (!rc.isSpawned()) return;

        try {
            int woundedCount = countWoundedAllies();
            rc.writeSharedArray(SHARED_ARRAY_WOUNDED_COUNT, woundedCount);
        } catch (GameActionException e) {
            System.out.println("Error updating wounded count: " + e.getMessage());
        }
    }

    private int countWoundedAllies() throws GameActionException {
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(-1, rc.getTeam());
        if (nearbyAllies == null) return 0;

        int woundedCount = 0;
        for (RobotInfo ally : nearbyAllies) {
            if (ally.getHealth() < woundedHealthThreshold) {
                woundedCount++;
            }
        }
        return woundedCount;
    }

    public void resetWoundedCount() {
        if (rc.getRoundNum() % WOUNDED_RESET_INTERVAL == 0) {
            try {
                rc.writeSharedArray(SHARED_ARRAY_WOUNDED_COUNT, 0);
            } catch (GameActionException e) {
                System.out.println("Error resetting wounded count: " + e.getMessage());
            }
        }
    }

    public void updateHealThreshold() {
        this.woundedHealthThreshold = MAX_HEALTH - rc.getHealAmount();
    }
}