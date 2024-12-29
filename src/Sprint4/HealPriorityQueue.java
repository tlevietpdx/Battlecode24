package Sprint4;

import battlecode.common.*;
import java.util.ArrayList;

//List to manage possible heal targets and retrieve them in the proper order:
// Has flag -> lowest health -> farthest distance

//A helper class used by HealerDuck.pickHealTarget
public class HealPriorityQueue {
    //One individual priority queue
    // HealPriorityQueue uses two of these: one for ducks with the flag, and one for those without

    private static class Queue {
        private final ArrayList<RobotInfo> contents;
        Queue() {
            contents = new ArrayList<>();
        }

        boolean isNotEmpty() { return !contents.isEmpty(); }


        //Add to this queue by lowest health first
        //Used by HealPriorityQueue.add

        private void add(RobotInfo to_add) {
            int size = contents.size();
            for(int i = 0; i < size; i++) {
                if(contents.get(i).getHealth() >= to_add.getHealth()) {
                    contents.add(i, to_add);
                    return;
                }
            }
            contents.add(size, to_add);
        }

        //Search a list of RobotInfos sorted by lowest health first
        //return the farthest robot from the given position that is tied for lowest health

        //Used by HealPriorityQueue.get

        RobotInfo getPrioritized(MapLocation pos) {
            int farthest_dist = pos.distanceSquaredTo(contents.get(0).getLocation());
            int ret = 0;
            int min_health = contents.get(0).getHealth();
            for(int i = 1; i < contents.size(); i++) {
                if(contents.get(i).getHealth() > min_health) break;
                int dist_to_i = pos.distanceSquaredTo(contents.get(i).getLocation());
                if(dist_to_i > farthest_dist) {
                    ret = i;
                    farthest_dist = dist_to_i;
                }
            }
            return contents.get(ret);
        }
    }

    private final Queue no_flag; //Potential heal targets not carrying the flag
    private final Queue flag_carriers; //Potential heal targets carrying the flag
    private final RobotController rc;

    //Constructor
    HealPriorityQueue(RobotController robc) {
        no_flag = new Queue();
        flag_carriers = new Queue();
        rc = robc;
    }

    //Adds the given robot to the correct list in sorted order
    public void add(RobotInfo to_add) {
        assert(to_add != null);
        if(to_add.hasFlag()) {
            flag_carriers.add(to_add);
        }
        else {
            no_flag.add(to_add);
        }
    }

    //Get the highest priority ally in the queue
    public RobotInfo getTopPriority() {
        if(flag_carriers.isNotEmpty())
            return flag_carriers.getPrioritized(rc.getLocation());
        if(no_flag.isNotEmpty())
            return no_flag.getPrioritized(rc.getLocation());
        return null;
    }
}
