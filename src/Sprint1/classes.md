# Builder Robot Classes
## Table of Contents
__Classes__
### [BuilderMainPhase](#BuilderMainPhase)
### [BuilderSetupPhase](#BuilderSetupPhase)

## BuilderSetupPhase <a id="BuilderSetupPhase">
```java
/* 
Builds a trap in a location in the direction from the ducks current location
rc - required value
buildToward - NULL (results in random Direction being selected)
chosenTrap - NULL (results in randome TrapType being build)
return: 1 - success     0 - failure
*/
public static int buildTrapInDirection(RobotController rc, Direction buildToward, TrapType chosenTrap);
```
```java
public static Direction guideTowardCrumbs(RobotController rc, MapLocation [] nearbyPositions, MapLocation myPosition);
```
```java
/*
Moves the the robot from current location, to the location in the direction chosen
rc - required value
moveToward - NULL (results in random Direction being selected)
return: 1 - success     0 - failure
*/
public static int moveRobotInDirection(RobotController rc, Direction moveToward);
```
```java
/*
Using a random number generator, a random Direction is selected
rc - required value
return: Enum Direction
*/
public static Direction randomDirectionGuide(RobotController rc);
```
```java
/*
Using a random number generator, a random TrapType is selected
rc - required value
return: Enum TrapType
*/
public static TrapType randomTrapTypeGuide(RobotController rc);
```
```java
/*
This is the main function used to call this class.
This class is used to run a builder duck logic for the setup phase of battle.
*/
public static void runSetupPhase(RobotController rc);
```