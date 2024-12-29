// Taylor Hughes
// Logic for MainPhase of Builder Duck
package testbot;
import battlecode.common.*;

public class BuilderMainPhase {

    public static void runMainPhase (RobotController myRobotDuck) throws GameActionException{
        Direction goToward = randomDirectionGuide(myRobotDuck);
        if(myRobotDuck.canMove(goToward)){
            myRobotDuck.move(goToward);
        }
    }

    public static Direction randomDirectionGuide(RobotController myRobotDuck){
        Direction randDir = Direction.allDirections()[RobotPlayer.randNum.nextInt(8)];
        return randDir;
    }
}

