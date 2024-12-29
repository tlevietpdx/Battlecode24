package Sprint4;

import battlecode.common.*;

/**
 * Manages the purchase and timing of global upgrades for the robot team.
 * Handles different upgrade strategies based on game state and team conditions.
 */
public class GlobalUpgradeManager {
    private static final int MIN_ATTACKERS_FOR_ATTACK_UPGRADE = 15;
    private static final int MIN_WOUNDED_FOR_HEALING = 7;
    private static final int LATE_GAME_ROUND = 1800;
    private static final int EARLY_GAME_THRESHOLD = 600;
    private static final int WOUNDED_RESET_INTERVAL = 50;
    private static final int MAX_HEALTH = 1000;

    // Shared array indices for team-wide communication
    public static final int SHARED_ARRAY_ATTACKER_COUNT = 0;
    public static final int SHARED_ARRAY_WOUNDED_COUNT = 1;
    public static final int SHARED_ARRAY_FLAGS_CAPTURED = 2;
    public static final int SHARED_ARRAY_IS_RETREATING = 3;

    private final RobotController rc;
    private int woundedHealthThreshold;
    private GameState gameState;
    private UpgradeAvailability upgradeAvailability;

    /**
     * Inner class to track which upgrades are currently available for purchase.
     * Provides a clean way to check if any upgrades are available.
     */
    private static class UpgradeAvailability {
        boolean attack;
        boolean capturing;
        boolean healing;

        boolean hasAnyAvailable() {
            return attack || capturing || healing;
        }
    }

    /**
     * Enum to represent different phases of the game.
     * Each phase has different upgrade priorities and strategies.
     */
    private enum GameState {
        EARLY_GAME,
        MID_GAME,
        LATE_GAME
    }

    /**
     * Constructs a new GlobalUpgradeManager with the given RobotController.
     * Initializes thresholds and caches initial values.
     *
     * @param rc The RobotController instance for this robot
     */
    public GlobalUpgradeManager(RobotController rc) {
        this.rc = rc;
        this.woundedHealthThreshold = MAX_HEALTH - rc.getHealAmount();
        this.upgradeAvailability = new UpgradeAvailability();
        refreshCachedValues();
    }

    /**
     * Updates all cached values to ensure decisions are made with current information.
     * Should be called at the start of each round.
     */
    private void refreshCachedValues() {
        updateGameState();
        updateUpgradeAvailability();
    }

    /**
     * Determines the current game state based on round number.
     * Updates the gameState field accordingly.
     */
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

    /**
     * Updates which upgrades are currently available for purchase.
     * Checks each upgrade type with the RobotController.
     */
    private void updateUpgradeAvailability() {
        upgradeAvailability.attack = rc.canBuyGlobal(GlobalUpgrade.ATTACK);
        upgradeAvailability.capturing = rc.canBuyGlobal(GlobalUpgrade.CAPTURING);
        upgradeAvailability.healing = rc.canBuyGlobal(GlobalUpgrade.HEALING);
    }

    /**
     * Main method to attempt purchasing upgrades based on current game state.
     * Handles different strategies for early, mid, and late game.
     *
     * @throws GameActionException if any robot controller calls fail
     */
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

    /**
     * Handles upgrade purchases during early game.
     * Focuses on attack upgrades if enough attackers are present.
     */
    private void purchaseEarlyGameUpgrades() throws GameActionException {
        if (upgradeAvailability.attack && hasEnoughAttackers()) {
            buyUpgrade(GlobalUpgrade.ATTACK);
        }
    }

    /**
     * Handles upgrade purchases during mid game.
     * Prioritizes healing when many wounded units exist,
     * then capturing when flags are involved,
     * and finally attack when sufficient attackers are present.
     */
    private void purchaseMidGameUpgrades() throws GameActionException {
        if (shouldBuyHealing()) {
            buyUpgrade(GlobalUpgrade.HEALING);
        } else if (shouldBuyCapturing()) {
            buyUpgrade(GlobalUpgrade.CAPTURING);
        } else if (shouldBuyAttack()) {
            buyUpgrade(GlobalUpgrade.ATTACK);
        }
    }

    /**
     * Handles upgrade purchases during late game.
     * Buys any remaining upgrades in order of priority:
     * Attack -> Capturing -> Healing
     */
    private void purchaseLateGameUpgrades() throws GameActionException {
        if (upgradeAvailability.attack) {
            buyUpgrade(GlobalUpgrade.ATTACK);
        } else if (upgradeAvailability.capturing) {
            buyUpgrade(GlobalUpgrade.CAPTURING);
        } else if (upgradeAvailability.healing) {
            buyUpgrade(GlobalUpgrade.HEALING);
        }
    }

    /**
     * Checks if there are enough attackers to justify an attack upgrade.
     */
    private boolean hasEnoughAttackers() throws GameActionException {
        return rc.readSharedArray(SHARED_ARRAY_ATTACKER_COUNT) >= MIN_ATTACKERS_FOR_ATTACK_UPGRADE;
    }

    /**
     * Determines if healing upgrade should be purchased based on wounded count.
     */
    private boolean shouldBuyHealing() throws GameActionException {
        return upgradeAvailability.healing &&
                rc.readSharedArray(SHARED_ARRAY_WOUNDED_COUNT) >= MIN_WOUNDED_FOR_HEALING;
    }

    /**
     * Determines if capturing upgrade should be purchased based on flag status
     * and retreat status.
     */
    private boolean shouldBuyCapturing() throws GameActionException {
        return upgradeAvailability.capturing &&
                (rc.readSharedArray(SHARED_ARRAY_FLAGS_CAPTURED) > 0 ||
                        rc.readSharedArray(SHARED_ARRAY_IS_RETREATING) == 1);
    }

    /**
     * Determines if attack upgrade should be purchased based on attacker count.
     */
    private boolean shouldBuyAttack() throws GameActionException {
        return upgradeAvailability.attack && hasEnoughAttackers();
    }

    /**
     * Executes the purchase of a global upgrade.
     */
    private void buyUpgrade(GlobalUpgrade upgrade) throws GameActionException {
        rc.buyGlobal(upgrade);
    }

    /**
     * Updates the count of wounded allies in the shared array.
     * Only counts allies below the wounded health threshold.
     */
    public void updateWoundedCount() {
        if (!rc.isSpawned()) return;

        try {
            int woundedCount = countWoundedAllies();
            rc.writeSharedArray(SHARED_ARRAY_WOUNDED_COUNT, woundedCount);
        } catch (GameActionException e) {
            System.out.println("Error updating wounded count: " + e.getMessage());
        }
    }

    /**
     * Counts the number of nearby wounded allies.
     * An ally is considered wounded if their health is below the threshold.
     */
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

    /**
     * Periodically resets the wounded count in the shared array.
     * Resets occur every WOUNDED_RESET_INTERVAL rounds.
     */
    public void resetWoundedCount() {
        if (rc.getRoundNum() % WOUNDED_RESET_INTERVAL == 0) {
            try {
                rc.writeSharedArray(SHARED_ARRAY_WOUNDED_COUNT, 0);
            } catch (GameActionException e) {
                System.out.println("Error resetting wounded count: " + e.getMessage());
            }
        }
    }

    /**
     * Updates the threshold for considering a unit wounded based on current heal amount.
     * A unit is considered wounded if its health is below MAX_HEALTH minus heal amount.
     */
    public void updateHealThreshold() {
        this.woundedHealthThreshold = MAX_HEALTH - rc.getHealAmount();
    }
}