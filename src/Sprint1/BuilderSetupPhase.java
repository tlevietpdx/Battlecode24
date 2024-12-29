// Taylor Hughes
// Logic for Setup Phase of Builder Duck
package Sprint1;
import battlecode.common.*;

public class BuilderSetupPhase {

    public static void runSetupPhase(RobotController myRobotDuck) throws GameActionException {
        MapLocation myPosition = myRobotDuck.getLocation();
        MapLocation [] nearbyPositions = myRobotDuck.getAllLocationsWithinRadiusSquared(myPosition,3);
        // if no crumbs nearby it just gives the direction CENTER aka nowhere
        Direction crumbDir = guideTowardNearbyCrumbs(myRobotDuck,nearbyPositions,myPosition);
        Direction waterHoleDir = guideTowardWaterHole(myRobotDuck,nearbyPositions,myPosition);
        if(crumbDir == Direction.CENTER){
            crumbDir = randomDirectionGuide(myRobotDuck);
            if(myRobotDuck.canFill(myPosition.add(waterHoleDir))){
                myRobotDuck.fill(myPosition.add(waterHoleDir));
            }
            else{
                moveRobotInDirection(myRobotDuck, crumbDir);
            }
        }
        else{
            MapLocation movedDuck = moveRobotInDirection(myRobotDuck, crumbDir);
        }
    }

    public static void runMainPhase(RobotController myRobotDuck) throws GameActionException {
        buildTrapInDirection(myRobotDuck,null,null);
        moveRobotInDirection(myRobotDuck, null); // random
    }

    public static MapLocation buildTrapInDirection(RobotController myRobotDuck, Direction buildToward, TrapType chosenTrap) throws GameActionException{
        try{
            MapLocation failedMapLoc = new MapLocation(-1,-1);
            if(null == buildToward) buildToward = randomDirectionGuide(myRobotDuck);
            if(chosenTrap == null) chosenTrap = randomTrapTypeGuide(myRobotDuck);    
            if(myRobotDuck.canBuild(chosenTrap,myRobotDuck.getLocation().add(buildToward))){
                MapLocation theTrapPlacementLoc = myRobotDuck.getLocation().add(buildToward);
                myRobotDuck.build(chosenTrap, theTrapPlacementLoc);
                return theTrapPlacementLoc;
            }
            return failedMapLoc;
        }
        catch(GameActionException e){
            System.out.println("GameActionException");
            e.printStackTrace();
        }
        catch(Exception e){
            System.out.println("Exception");
            e.printStackTrace();
        }
        return myRobotDuck.getLocation();
    }

    public static Boolean digWaterHole (RobotController myRobotDuck, MapLocation toDig) throws GameActionException {
        if(myRobotDuck.canDig(toDig)){
            myRobotDuck.dig(toDig);
            return true;
        }
        return false;
    }

    public static Boolean fillWaterHole (RobotController myRobotDuck, MapLocation toFill) throws GameActionException{
        if(myRobotDuck.canFill(toFill)){
            myRobotDuck.fill(toFill);
            return true;
        }
        return false;
    }

    public static TrapType randomTrapTypeGuide(RobotController myRobotDuck) throws GameActionException{
        int randNumber = RobotPlayer.randNum.nextInt() % 2;
        if(0 == randNumber){
            return TrapType.EXPLOSIVE;
        }
        else if(1 == randNumber){
            return TrapType.STUN;
        }
        else return TrapType.WATER;
    }

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
        // return randomDirectionGuide(myRobotDuck);
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
            goToward = randomDirectionGuide(myRobotDuck);
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