package Sprint3;

import battlecode.common.*;
import java.util.ArrayList;

public class HealerDuck {
    //Pick an ally to heal
    //Priorities: has flag -> lowest health -> farthest distance
    //Only heals those it would not over heal
    //returns null if no allies are found
    public static RobotInfo pickHealTarget(RobotController rc) throws GameActionException {
        RobotInfo [] allies = rc.senseNearbyRobots(4, rc.getTeam());
        HealPriorityQueue queued_allies = new HealPriorityQueue(rc);
        int max_to_heal = 1000 - rc.getHealAmount();

        for(RobotInfo ally : allies) {
            if(ally.getHealth() <= max_to_heal)
                queued_allies.add(ally);
        }
        return queued_allies.getTopPriority();
    }

    //Attempt to heal nearby injured allies.
    //Do nothing if no injured allies in sight
    //Return target's location, or null if none was picked
    public static MapLocation runHealerStep(RobotController rc) throws GameActionException {
        //Don't try to run this function if we can't heal
        if(!rc.isActionReady() || rc.getLocation() == null)
            return null;
        RobotInfo target = pickHealTarget(rc);
        if(target == null)
            return null;
        MapLocation target_loc = target.getLocation();
        if(rc.canHeal(target_loc)) {
            rc.heal(target_loc);
        }
        return target_loc;
    }
}
