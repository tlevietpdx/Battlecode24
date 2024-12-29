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
    private int currentRound;
    private boolean canBuyAttack;
    private boolean canBuyCapturing;
    private boolean canBuyHealing;

    public GlobalUpgradeManager(RobotController rc) {
        this.rc = rc;
        this.woundedHealthThreshold = MAX_HEALTH - rc.getHealAmount();
        refreshCachedValues();
    }

    // Call this at the start of each round
    private void refreshCachedValues() {
        currentRound = rc.getRoundNum();
        canBuyAttack = rc.canBuyGlobal(GlobalUpgrade.ATTACK);
        canBuyCapturing = rc.canBuyGlobal(GlobalUpgrade.CAPTURING);
        canBuyHealing = rc.canBuyGlobal(GlobalUpgrade.HEALING);
    }

    public void tryPurchaseUpgrades() throws GameActionException {
        refreshCachedValues();

        // Quick exit if no upgrades available
        if (!canBuyAttack && !canBuyCapturing && !canBuyHealing) {
            return;
        }

        // Late game handling
        if (currentRound >= LATE_GAME_ROUND) {
            tryBuyRemainingUpgrades();
            return;
        }

        // Early game specific handling
        if (currentRound == EARLY_GAME_THRESHOLD) {
            tryBuyEarlyGameUpgrade();
            return;
        }

        // Mid game handling
        tryBuyMidGameUpgrades();
    }

    private void tryBuyEarlyGameUpgrade() throws GameActionException {
        if (canBuyAttack && rc.readSharedArray(SHARED_ARRAY_ATTACKER_COUNT) >= MIN_ATTACKERS_FOR_ATTACK_UPGRADE) {
            buyUpgrade(GlobalUpgrade.ATTACK);
        }
    }

    private void tryBuyMidGameUpgrades() throws GameActionException {
        // Cache shared array reads
        int woundedCount = rc.readSharedArray(SHARED_ARRAY_WOUNDED_COUNT);

        if (canBuyHealing && woundedCount >= MIN_WOUNDED_FOR_HEALING) {
            buyUpgrade(GlobalUpgrade.HEALING);
            return;
        }

        if (canBuyCapturing) {
            int flagsCaptured = rc.readSharedArray(SHARED_ARRAY_FLAGS_CAPTURED);
            boolean isRetreating = rc.readSharedArray(SHARED_ARRAY_IS_RETREATING) == 1;
            if (flagsCaptured > 0 || isRetreating) {
                buyUpgrade(GlobalUpgrade.CAPTURING);
                return;
            }
        }

        if (canBuyAttack && rc.readSharedArray(SHARED_ARRAY_ATTACKER_COUNT) >= MIN_ATTACKERS_FOR_ATTACK_UPGRADE) {
            buyUpgrade(GlobalUpgrade.ATTACK);
        }
    }

    private void tryBuyRemainingUpgrades() throws GameActionException {
        if (canBuyAttack) {
            buyUpgrade(GlobalUpgrade.ATTACK);
        } else if (canBuyCapturing) {
            buyUpgrade(GlobalUpgrade.CAPTURING);
        } else if (canBuyHealing) {
            buyUpgrade(GlobalUpgrade.HEALING);
        }
    }

    private void buyUpgrade(GlobalUpgrade upgrade) throws GameActionException {
        rc.buyGlobal(upgrade);
    }

    public void updateWoundedCount() {
        if (!rc.isSpawned()) return;

        try {
            // Get all allies in one API call
            RobotInfo[] nearbyAllies = rc.senseNearbyRobots(-1, rc.getTeam());
            if (nearbyAllies == null) return;

            int woundedCount = 0;
            for (RobotInfo ally : nearbyAllies) {
                if (ally.getHealth() < woundedHealthThreshold) {
                    woundedCount++;
                }
            }
            rc.writeSharedArray(SHARED_ARRAY_WOUNDED_COUNT, woundedCount);
        } catch (GameActionException e) {
            System.out.println("Error updating wounded count: " + e.getMessage());
        }
    }

    public void resetWoundedCount() {
        if (currentRound % WOUNDED_RESET_INTERVAL == 0) {
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