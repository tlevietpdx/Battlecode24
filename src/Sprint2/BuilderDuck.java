// Taylor Hughes
// Actions that a builder duck is capable of doing.
package Sprint2;
import battlecode.common.*;

/*
~ ~ ~ FUNCTIONS ~ ~ ~

@descript Builds a specified (random if 'null') trap in a specified (random if 'null') direction.
public static MapLocation buildTrapInDirection(RobotController rc, Direction buildToward, TrapType chosenTrap)
    @return MapLocation(-1,-1) -> Failed
    @return MapLocation(location where trap was built)

@descript Digs a hole at the specified location if legally able to dig there.
public static Boolean digWaterHole(RobotController rc, MapLocation toDig)
    @return false -> failed to dig the hole
    @return true -> succeeded to dig the hole

@descript Fills a hole at the specific location if legally able to dig there.
public static Boolean fillWaterHole(RobotController rc, MapLocation toFill)
    @return false -> failed to fill the hole
    @return true -> succeeded to fill the hole

@descript Used to recieve a random Trap Type.
public static TrapType randomTrapTypeGuide(RobotController rc)
    @return TrapType -> either EXPLOSIVE, WATER, STUN, or NONE -> Always Successful
 
*/

public class BuilderDuck {
    
    public static MapLocation buildTrapInDirection(RobotController myRobotDuck, Direction buildToward, TrapType chosenTrap) throws GameActionException{
        try{
            MapLocation failedMapLoc = new MapLocation(-1,-1);
            if(null == buildToward) buildToward = ScoutMoves.randomDirectionGuide(myRobotDuck);
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
}