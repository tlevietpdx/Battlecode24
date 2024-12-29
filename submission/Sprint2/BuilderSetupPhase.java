// Taylor Hughes
// Logic for Setup Phase of Builder Duck
package Sprint2;
import battlecode.common.*;

public class BuilderSetupPhase {

    public static void runSetupPhase(RobotController myRobotDuck) throws GameActionException {
        MapLocation myPosition = myRobotDuck.getLocation();
        MapLocation [] nearbyPositions = myRobotDuck.getAllLocationsWithinRadiusSquared(myPosition,3);
        // if no crumbs nearby it just gives the direction CENTER aka nowhere
        Direction crumbDir = ScoutMoves.guideTowardNearbyCrumbs(myRobotDuck,nearbyPositions,myPosition);
        Direction waterHoleDir = ScoutMoves.guideTowardWaterHole(myRobotDuck,nearbyPositions,myPosition);
        if(crumbDir == Direction.CENTER){
            crumbDir = ScoutMoves.randomDirectionGuide(myRobotDuck);
            if(myRobotDuck.canFill(myPosition.add(waterHoleDir))){
                myRobotDuck.fill(myPosition.add(waterHoleDir));
            }
            else{
                ScoutMoves.moveRobotInDirection(myRobotDuck, crumbDir);
            }
        }
        else{
            MapLocation movedDuck = ScoutMoves.moveRobotInDirection(myRobotDuck, crumbDir);
        }
    }

    public static void runMainPhase(RobotController myRobotDuck) throws GameActionException {
        BuilderDuck.buildTrapInDirection(myRobotDuck,null,null);
        ScoutMoves.moveRobotInDirection(myRobotDuck, null); // random
    }

}