// Taylor Hughes
// Actions that a builder duck is capable of doing.
package Sprint4;
import battlecode.common.*;

/*
~ ~ ~ FUNCTIONS ~ ~ ~

    private static MapLocation[] getSingleSpawnZoneLocations(MapLocation[] allSpawnZones, int origin_x, int origin_y){
    private static MapLocation getSpawnThreeOrigin(MapLocation[] spawnZones, int padding){
    private static MapLocation getSpawnTwoOrigin(MapLocation[] allSpawnZones, int padding){
    private static void setPerimeterAroundSpawnZone(MapLocation[] perimeterLocs, int perimStartingX, int perimStartingY, int stretch, int zone_number){
    
public static MapLocation[] getAllySpawnLocationsPaddedPerimeter(RobotController myRobotDuck, int padding){


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
    public static MapLocation[] spawnZoneOne = new MapLocation[9];
    public static MapLocation[] spawnZoneTwo = new MapLocation[9];
    public static MapLocation[] spawnZoneThree = new MapLocation[9];

    public static MapLocation[] getSingleSpawnZoneLocations(MapLocation[] allSpawnZones, int origin_x, int origin_y){
        MapLocation[] singleSpawnZone = new MapLocation[9];
        for(int indx = 0; indx < 3; indx++){
            for(int indy = 0; indy < 3; indy++){
                singleSpawnZone[indy * 3 + indx] = new MapLocation(origin_x + indx, origin_y + indy);
            }
        }
        return singleSpawnZone;
    }

    public static MapLocation getSpawnThreeOrigin(MapLocation[] spawnZones, int padding){
        int perimStartingX = 0;
        int perimStartingY = 0;
        boolean spawnOneExists = false; 
        boolean spawnTwoExists = false; 
        for(int indx = 3; indx < spawnZoneOne.length + spawnZoneTwo.length + 1; indx++){
            // does spawnZone exist in either spawnZoneOne or spawnZone Two
            for(int indx_one = 0; indx_one < spawnZoneOne.length; indx_one++){
                // if one is equal then it exists
                if(spawnZones[indx].x == spawnZoneOne[indx_one].x && spawnZones[indx].y == spawnZoneOne[indx_one].y){
                    spawnOneExists = true;
                }
                if(spawnZones[indx].x == spawnZoneTwo[indx_one].x && spawnZones[indx].y == spawnZoneTwo[indx_one].y){
                    spawnTwoExists = true;
                }
            }
            if(spawnOneExists || spawnTwoExists){
                spawnOneExists = false;
                spawnTwoExists = false;
            }
            else{
                perimStartingX = spawnZones[indx].x - padding - 1;
                perimStartingY = spawnZones[indx].y - padding - 1;
                indx = spawnZoneTwo.length + spawnZoneOne.length;
            }
        }
        return new MapLocation(perimStartingX, perimStartingY);
    }

    public static MapLocation getSpawnTwoOrigin(MapLocation[] allSpawnZones, int padding){
        int perimStartingX = 0;
        int perimStartingY = 0;
        for(int indx = 0; indx < spawnZoneOne.length; indx++){
            if(allSpawnZones[indx].x != spawnZoneOne[indx].x && allSpawnZones[indx].y != spawnZoneOne[indx].y){
                perimStartingX = allSpawnZones[indx].x - padding - 1;
                perimStartingY = allSpawnZones[indx].y - padding - 1;
                indx = spawnZoneOne.length;
            }
            else{
                perimStartingX = allSpawnZones[indx + 1].x - padding - 1;
                perimStartingY = allSpawnZones[indx + 1].y - padding - 1;
            }
        }
        return new MapLocation(perimStartingX,perimStartingY);
    }

    public static void setPerimeterAroundSpawnZone(MapLocation[] perimeterLocs, int perimStartingX, int perimStartingY, int stretch, int zone_number){
        for(int indx = 0; indx < stretch; indx++){
            // bottom      (0,0) -> (3,0) indx 0,1,2,3
            perimeterLocs[indx + ((zone_number * 4 - 4) * stretch)] = new MapLocation(perimStartingX + indx, perimStartingY);
            // rightside   (4,0) -> (4,3) indx 4,5,6,7
            perimeterLocs[indx + ((zone_number * 4 - 3) * stretch)] = new MapLocation(perimStartingX + stretch, perimStartingY + indx);
            // top         (4,4) -> (1,4) indx 9,10,11,12
            perimeterLocs[indx + ((zone_number * 4 - 2) * stretch)] = new MapLocation(perimStartingX + stretch - indx, perimStartingY + stretch);
            // leftside    (0,4) -> (0,1) indx 13,14,15,16
            perimeterLocs[indx + ((zone_number * 4 - 1) * stretch)] = new MapLocation(perimStartingX, perimStartingY + stretch - indx);
        }
    }

    public static MapLocation[] getAllySpawnLocationsPaddedPerimeter(RobotController myRobotDuck, int padding){
        int allPerimeterLength = (padding * 2 + 4) * 12;
        MapLocation[] spawnZones = myRobotDuck.getAllySpawnLocations();
        MapLocation[] perimeterLocs = new MapLocation[allPerimeterLength];
        MapLocation spawnTwoOrigin;
        MapLocation spawnThreeOrigin;
        int perimStartingX = spawnZones[0].x - padding - 1;
        int perimStartingY = spawnZones[0].y - padding - 1;
        int stretch = 4 + (padding * 2);

        // Find Spawn Zone One and set spawn zone ONE perimeter
        spawnZoneOne = getSingleSpawnZoneLocations(spawnZones,spawnZones[0].x,spawnZones[0].y);
        setPerimeterAroundSpawnZone(perimeterLocs,perimStartingX,perimStartingY, stretch, 1);

        // Find Spawn Zone Two and set spawn zone TWO perimeter
        spawnTwoOrigin = getSpawnTwoOrigin(spawnZones, padding);
        spawnZoneTwo = getSingleSpawnZoneLocations(spawnZones,spawnTwoOrigin.x + 1 + padding, spawnTwoOrigin.y + 1 + padding);
        setPerimeterAroundSpawnZone(perimeterLocs, spawnTwoOrigin.x, spawnTwoOrigin.y, stretch, 2);

        // Find Spawn Zone Three and set spawn zone THREE perimeter
        spawnThreeOrigin = getSpawnThreeOrigin(spawnZones, padding);
        spawnZoneThree = getSingleSpawnZoneLocations(spawnZones, spawnThreeOrigin.x + padding + 1, spawnThreeOrigin.y + padding + 1);
        setPerimeterAroundSpawnZone(perimeterLocs, spawnThreeOrigin.x, spawnThreeOrigin.y, stretch, 3);
        
        return perimeterLocs;
    }
    
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