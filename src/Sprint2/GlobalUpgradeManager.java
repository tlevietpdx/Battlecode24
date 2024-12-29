package Sprint2;

import battlecode.common.*;

public class GlobalUpgradeManager {
    public static void tryPurchaseUpgrades(RobotController rc, int battleRound) throws GameActionException {
        if (battleRound % 600 == 0) {
            GlobalUpgrade[] upgrades = {GlobalUpgrade.ATTACK, GlobalUpgrade.CAPTURING, GlobalUpgrade.HEALING};
            for (GlobalUpgrade upgrade : upgrades) {
                if (rc.canBuyGlobal(upgrade)) {
                    rc.buyGlobal(upgrade);
                }
            }
        }
    }
}

