// Taylor Hughes
// Class of various Scouting measures involving movement, etc.
package Sprint4;
import battlecode.common.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

/*
~ ~ ~ Variables ~ ~ ~
enum TileType { UNKNOWN, SPAWNZONE, DAM, WALL, WATER }

@descript Used to plot a map of locations.
public static TileType map_percepts[3600]

~ ~ ~ Function ~ ~ ~ 
@descript Plots a single location in a 60 x 60 "map" at location (x,y) coordinate with TileType. The map has coordinate (0,0) at bottom left of screen
public static int cartograph_location(RobotController rc, int x_coordinate, int y_coordinate, TileType detected)
    @return int element_index number of plot within map_percepts[array]

@descript Help a duck move in the direction of nearby crumbs if any detected nearby.
public static Direction guideTowardNearbyCrumbs(RobotController myRobotDuck, MapLocation [] nearbyPositions, MapLocation myPosition)
    @returns the direction toward crumbs nearby -or- random direction ALWAYS SUCCESSFUL

@descript Moves the robot in the direction specifically (randomly if 'null') specified.
public static MapLocation moveRobotInDirection(RobotController myRobotDuck, Direction goToward)
    @return failed - MapLocation(-1,-1)
    @return succeeded - MapLocation(current location of duck after move)

    @ WARNING START - NOT YET IMPLEMENTED CORRECTLY
    @descript Used to document a mapping of TileTypes visible to robot within environment.
    public static int percept_surrounding(RobotController rc)
    @ WARNING END - NOT YET IMPLEMENTED CORRECTLY

@descript Used to help get a direction randomly
public static Direction randomDirectionGuide(RobotController myRobotDuck)
    @return Cardinal direction - always successful
*/

public class ScoutMoves {
    
    public static Direction guideTowardNearbyCrumbs(RobotController myRobotDuck, MapLocation [] nearbyPositions, MapLocation myPosition) throws GameActionException{
        for(int indx = 0; indx < nearbyPositions.length; indx++){
            MapInfo tileInfo = myRobotDuck.senseMapInfo(nearbyPositions[indx]);
            if(tileInfo.getCrumbs() > 0){
                Direction pathToCrumbs = myPosition.directionTo(nearbyPositions[indx]);
                if(myRobotDuck.canMove(pathToCrumbs)){
                    return pathToCrumbs;
                }
            }
        }
        return Direction.CENTER;
        // return ScoutMoves.randomDirectionGuide(myRobotDuck);
    }

    public static Direction guideTowardWaterHole(RobotController myRobotDuck, MapLocation [] nearbyPositions, MapLocation myPosition) throws GameActionException{
        for(int indx = 0; indx < nearbyPositions.length; indx++){
            MapInfo tileInfo = myRobotDuck.senseMapInfo(nearbyPositions[indx]);
            if(tileInfo.isWater()){
                Direction pathToWater = myPosition.directionTo(nearbyPositions[indx]);
                    return pathToWater;
            }
        }
        return Direction.CENTER;
    }

    public static MapLocation moveRobotInDirection(RobotController myRobotDuck, Direction goToward) throws GameActionException{
        if(null == goToward){
            goToward = ScoutMoves.randomDirectionGuide(myRobotDuck);
        }
        MapLocation badLocation = new MapLocation(-1,-1);
        if(myRobotDuck.canMove(goToward)){
                myRobotDuck.move(goToward);
                return myRobotDuck.getLocation();
                //return badLocation;
        }
        
        return badLocation;
    }

    // Uses randNum in RobotPlayer.java to obtain random Direction to go
    public static Direction randomDirectionGuide(RobotController myRobotDuck) throws GameActionException{
        Direction randDir = Direction.allDirections()[RobotPlayer.randNum.nextInt(8)];
        return randDir;
    }
    
}
