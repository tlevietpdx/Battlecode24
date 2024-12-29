// Taylor Hughes
// Logic for Setup Phase of Builder Duck
package testbot;
import battlecode.common.*;

public class BuilderSetupPhase {

    public static void runSetupPhase(RobotController myRobotDuck) throws GameActionException {
        MapLocation myPosition = myRobotDuck.getLocation();
        // take crumbs
        MapLocation potentialMovement;
        MapLocation [] nearbyPositions = myRobotDuck.getAllLocationsWithinRadiusSquared(myPosition,3);
        // determine where there are crumbs
        Direction crumbDir = guideTowardCrumbs(myRobotDuck,nearbyPositions,myPosition);
        myRobotDuck.setIndicatorString("Crumbs " + crumbDir);
        int movedDuck = moveRobotInDirection(myRobotDuck, crumbDir);
        if(0 == movedDuck){
            potentialMovement = myPosition.add(crumbDir);
            if(myRobotDuck.senseMapInfo(potentialMovement).isWater()){
                if(myRobotDuck.canFill(potentialMovement)){
                    myRobotDuck.fill(potentialMovement);
                    myRobotDuck.setIndicatorString("Filled in Some Water");
                    myRobotDuck.move(crumbDir);
                }
            }
        }

    }

    public static void runMainPhase(RobotController myRobotDuck) throws GameActionException {
        buildTrapInDirection(myRobotDuck,null,null);
        moveRobotInDirection(myRobotDuck, null); // random
    }

    // evaluates the radius, and tells duck which direcion to move towards
    public static Direction guideTowardCrumbs(RobotController myRobotDuck, MapLocation [] nearbyPositions, MapLocation myPosition) throws GameActionException{
        for(int indx = 0; indx < nearbyPositions.length; indx++){
            MapInfo tileInfo = myRobotDuck.senseMapInfo(nearbyPositions[indx]);
            if(tileInfo.getCrumbs() > 0){
                Direction pathToCrumbs = myPosition.directionTo(nearbyPositions[indx]);
                if(myRobotDuck.canMove(pathToCrumbs)){
                    return pathToCrumbs;
                }
            }
        }

        return randomDirectionGuide(myRobotDuck);
    }

    public static Direction randomDirectionGuide(RobotController myRobotDuck){
        Direction randDir = Direction.allDirections()[RobotPlayer.randNum.nextInt(8)];
        return randDir;
    }

    public static TrapType randomTrapTypeGuide(RobotController myRobotDuck){
        int randNumber = RobotPlayer.randNum.nextInt() % 3;
        if(0 == randNumber){
            return TrapType.EXPLOSIVE;
        }
        else if(1 == randNumber){
            return TrapType.STUN;
        }
        else if(2 == randNumber){
            return TrapType.WATER;
        }
        else return TrapType.NONE;
    }

    // return 1 - Success       return 0 - Failure
    // if Direction goToward is null -> random directino is given
    public static int moveRobotInDirection(RobotController myRobotDuck, Direction goToward)throws GameActionException{
        if(null == goToward){
            goToward = randomDirectionGuide(myRobotDuck);
        }
        if(myRobotDuck.canMove(goToward)){
                myRobotDuck.move(goToward);
                return 1;
        }
        return 0;
    }

    // return 1 - success       return 0 - Failure
    // if Direction goToward is null -> random directino is generated
    // if TrapType chosenTrap is null -> random TrapType is generated
    public static int buildTrapInDirection(RobotController myRobotDuck, Direction buildToward, TrapType chosenTrap) throws GameActionException{
        if(null == buildToward) buildToward = randomDirectionGuide(myRobotDuck);
        if(chosenTrap == null) chosenTrap = randomTrapTypeGuide(myRobotDuck);    
        if(myRobotDuck.canBuild(chosenTrap,myRobotDuck.getLocation().add(buildToward))){
            myRobotDuck.build(chosenTrap, myRobotDuck.getLocation().add(buildToward));
            return 1;
        }
        return 0;
    }

}
