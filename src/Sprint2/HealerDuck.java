package Sprint2;

import battlecode.common.*;
import java.util.ArrayList;

public class HealerDuck {
    //Add duck info to the given list, sorted by lowest health first
    //Return the index of the new element
    private static int addSortedByMissingHealth(ArrayList<RobotInfo> list, RobotInfo to_add) {
            for(int i = 0; i < list.size(); i++) {
                if(list.get(i).getHealth() >= to_add.getHealth()) {
                   list.add(i, to_add);
                   return i;
                }
            }
            list.add(list.size(), to_add);
            return list.size();
    }

    //Search a list of RobotInfos sorted by lowest health first
    //return the farthest robot from the given position that is tied for lowest health
    private static RobotInfo getFarthestAtMinHealth(ArrayList<RobotInfo> list, MapLocation pos) {
        int farthest_dist = pos.distanceSquaredTo(list.get(0).getLocation());
        int ret = 0;
        int min_health = list.get(0).getHealth();
        for(int i = 1; i < list.size(); i++) {
            if(list.get(i).getHealth() > min_health) break;
            int dist_to_i = pos.distanceSquaredTo(list.get(i).getLocation());
            if(dist_to_i > farthest_dist) {
                ret = i;
                farthest_dist = dist_to_i;
            }
        }
        return list.get(ret);
    }

    //Pick an ally to heal
    //Priorities: has flag -> lowest health -> farthest distance
    //Only heals those it would not over heal
    //returns null if no allies are found
    public static RobotInfo pickHealTarget(RobotController rc) throws GameActionException {
        RobotInfo [] allies = rc.senseNearbyRobots(4, rc.getTeam());
        ArrayList<RobotInfo> allies_with_flag = new ArrayList<>();
        ArrayList<RobotInfo> low_health_allies = new ArrayList<>();
        int heal_amount = rc.getHealAmount();

        for(RobotInfo ally : allies) {
            if(ally.hasFlag() && ally.getHealth() <= 1000 - heal_amount) {
                addSortedByMissingHealth(allies_with_flag, ally);
            } else if (ally.getHealth() <= 1000 - heal_amount) {
                addSortedByMissingHealth(low_health_allies, ally);
            }
        }
        if(!allies_with_flag.isEmpty()) {
            return getFarthestAtMinHealth(allies_with_flag, rc.getLocation());
        }
        else if(!low_health_allies.isEmpty()) {
            return getFarthestAtMinHealth(low_health_allies, rc.getLocation());
        }

        return null;
    }

    //Attempt to move toward and heal damaged allies.
    //Do nothing if no injured allies in sight
    //Return target's location, or null if none was picked
    public static MapLocation runHealerStep(RobotController rc) throws GameActionException {
        RobotInfo target = pickHealTarget(rc);
        if(target == null)
            return null;
        MapLocation target_loc = target.getLocation();
        //Direction dir_to_target = rc.getLocation().directionTo(target_loc);
        //if(rc.canMove(dir_to_target)) rc.move(dir_to_target);
        if(rc.canHeal(target_loc)) {
            rc.heal(target_loc);
        }
        return target_loc;
    }
}
