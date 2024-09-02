import becker.robots.City;
import becker.robots.Direction;
import becker.robots.Thing;
import becker.robots.Wall;

import java.util.Random;

/**
 * @author anarb
 * @description The Main class for the Picker robot, handles creating the environment/setting
 * @date Aug 23, 2024
 */
public class Main {

    /**
     * Main method for the program
     *
     * @param args - I have no idea what this does
     */
    public static void main(String[] args) {

        City myCity = new City();
        Random rand = new Random();
        ArbattiPickerRobot karel;

        final int MULTIPLIER = 5;

        int horizontalWall = 10;
        int verticalWall = 10;
        //int doorSpawn = 9;
        int wallSpawnX = 0, wallSpawnY = 0, storageDistance = 2;


        int robotX = rand.nextInt(wallSpawnX, wallSpawnX + horizontalWall);
        int robotY = rand.nextInt(wallSpawnY, wallSpawnY + verticalWall);

        int doorSpawn = rand.nextInt(wallSpawnX, wallSpawnX + horizontalWall);
        int thingSpawnCount = rand.nextInt(horizontalWall * MULTIPLIER-1, horizontalWall * MULTIPLIER);
        //int thingSpawnCount = horizontalWall * MULTIPLIER;

        myCity.showThingCounts(true);
        karel = new ArbattiPickerRobot(myCity, robotY, robotX, Direction.WEST);

        // Builds the horizontal walls
        for (int i = 0; i < horizontalWall; i++) {
            new Wall(myCity, wallSpawnY, wallSpawnX + i, Direction.NORTH);

            // Puts the bottom walls
            if (i != doorSpawn) {
                new Wall(myCity, wallSpawnY + verticalWall - 1, wallSpawnX + i, Direction.SOUTH);
            }
        }

        // Builds the vertical walls
        for (int i = 0; i < verticalWall; i++) {
            new Wall(myCity, i + wallSpawnY, wallSpawnX, Direction.WEST);
            new Wall(myCity, i + wallSpawnY, wallSpawnX + horizontalWall - 1, Direction.EAST);
        }

        // Builds the leftmost wall of the storage area
        new Wall(myCity, wallSpawnY + verticalWall + storageDistance, wallSpawnX, Direction.WEST);

        // Builds the horizontal part of the storage area
        for (int i = 0; i < horizontalWall; i++) {
            new Wall(myCity, wallSpawnY + verticalWall + storageDistance, wallSpawnX + i, Direction.SOUTH);
        }

        // Spawns the things
        for (int i = 0; i < thingSpawnCount; i++) {
            int thingX = rand.nextInt(wallSpawnX, wallSpawnX + horizontalWall);
            int thingY = rand.nextInt(wallSpawnY, wallSpawnY + verticalWall);
            new Thing(myCity, thingY, thingX);
        }

        karel.collectChairs();
    }

}