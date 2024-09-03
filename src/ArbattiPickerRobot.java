import becker.robots.City;
import becker.robots.Direction;
import becker.robots.IPredicate;
import becker.robots.RobotSE;

/**
 * @author anarb
 * @description Helper file for the picker robot, handles the robot's movement and "intelligence"
 * @date Aug 23, 2024
 */
public class ArbattiPickerRobot extends RobotSE {

    // This is a change for the better
    boolean moveUp = false, firstTime = true, finished = false;
    private int mode;
    private int doorX, doorY;
    private int storageX, storageY;
    private int offsetY = 0;

    /**
     * @param city      The City object where the sims will be placed
     * @param i         The y-location of a sim
     * @param i1        The x-location of a sim
     * @param direction The initial direction the sim (robot) faces
     * @description The constructor method from the parent class
     */
    public ArbattiPickerRobot(City city, int i, int i1, Direction direction) {
        super(city, i, i1, direction);
    }

    /**
     * @description The "main" method to get the robot to do the whole sequence
     */
    public void collectChairs() {

        this.findDoor();

        this.findStorage(this.mode);

        //.out.println("We are in this part");
        this.goToLocation(this.storageY, this.storageX, this.doorY, this.doorX);

        this.pickChairs(this.mode);
    }

    /**
     * @description The method to find the door
     */
    private void findDoor() {

        this.findEastWall();
        this.findSouthWall();
        this.locateDoor();
    }

    /**
     * @description Method to find the East wall of the gymnasium
     */
    private void findEastWall() {

        this.faceEast();
        this.frontMove();
    }

    /**
     * @description Method to find the South wall
     */
    private void findSouthWall() {

        this.turnRight();

        // Makes sure the robot stays within the cafeteria should the door be on the rightmost corner of the wall
        while (this.frontIsClear() && this.getIntersection().countSims(IPredicate.anyWall) >= 1) {
            this.move();
        }
    }

    /**
     * @description Method to locate the door and classify the door's location
     */
    private void locateDoor() {

        // Sees if the door is on the rightmost corner of the cafeteria
        if (this.frontIsClear() && this.isFacingSouth()) {
            this.mode = 1;
            this.doorY = this.getStreet() - 1;
        }
        else {
            this.turnRight();

            // Moves as long as the front is clear and the robot is touching a wall (hasn't found door)
            while (this.frontIsClear() && this.getIntersection().countSims(IPredicate.anyWall) != 0) {
                this.move();
            }

            // Checks to see if the robot is facing west, the front is clear, and it has found the door
            if (this.isFacingWest() && this.frontIsClear() && this.getIntersection().countSims(IPredicate.anyWall) == 0) {
                this.mode = 2;
                //this.doorY = this.getStreet();
                //this.doorX = this.getAvenue();
            }

            // Checks to see if the door has spawned on the left-most corner of the cafeteria
            else if (this.isFacingWest() && !this.frontIsClear() && this.getIntersection().countSims(IPredicate.anyWall) == 1) {
                this.mode = 3;
                //this.doorY = this.getStreet();
                //this.doorX = this.getAvenue();

            }
            this.doorY = this.getStreet();

        }
        this.doorX = this.getAvenue();

    }

    /**
     * @description Method to face North
     */
    private void faceNorth() {

        if (this.isFacingWest()) {
            this.turnRight();
        }
        else if (this.isFacingSouth()) {
            this.turnAround();
        }
        else if (this.isFacingEast()) {
            this.turnLeft();
        }
    }

    /**
     * @description Makes the robot face the East direction
     */
    private void faceEast() {

        if (this.isFacingNorth()) {
            this.turnRight();
        }
        else if (this.isFacingWest()) {
            this.turnAround();
        }
        else if (this.isFacingSouth()) {
            this.turnLeft();
        }
    }

    /**
     * @description Method to face South
     */
    private void faceSouth() {

        if (this.isFacingNorth()) {
            this.turnAround();
        }
        else if (this.isFacingWest()) {
            this.turnLeft();
        }
        else if (this.isFacingEast()) {
            this.turnRight();
        }
    }

    /**
     * @description Method to face West
     */
    private void faceWest() {

        if (this.isFacingNorth()) {
            this.turnLeft();
        }
        else if (this.isFacingEast()) {
            this.turnAround();
        }
        else if (this.isFacingSouth()) {
            this.turnRight();
        }
    }

    /**
     * @description Moves the robot forward as long as the way is clear
     */
    public void frontMove() {

        while (this.frontIsClear()) {
            this.move();
        }
    }

    /**
     * @description Method to safely pick something up
     */
    private void pickUpSafely() {

        boolean storageXUpdate = false;

        //Checks to make sure the robot can pick something and that it is not already holding anything
        if (this.canPickThing() && this.countThingsInBackpack() == 0) {
            this.pickThing();
        }

        // Checks to see if the robot is holding a chair
        if (this.countThingsInBackpack() == 1) {

            this.goToLocation(this.getStreet(), this.getAvenue(), this.doorY, this.doorX);
            this.goToLocation(this.getStreet(), this.getAvenue(), this.storageY, this.getAvenue());
            this.goToLocation(this.getStreet(), this.getAvenue(), this.storageY, this.storageX);

            this.putThing();

            // Checks if all the chairs have been stacked
            if (this.finished) {
                this.faceNorth();
                this.move();
            }

            // Checks if the 10 chairs have been stacked
            if (this.getIntersection().countSims(IPredicate.aThing) == 10) {
                storageXUpdate = true;
            }

            // Returns to the door
            if (!this.finished) {
                this.goToLocation(this.storageY, this.storageX, this.doorY, this.doorX);
            }

            // Checks if the 10 chairs have been stacked and the storage point needs to be incremented
            if (storageXUpdate) {
                this.storageX++;
            }

        }

    }

    /**
     * @description Method to see if the robot should go to the next street
     */
    private void advanceStreet() {

        // Checks if the robot has completed clearing the chairs
        if (!this.frontIsClear() && this.getIntersection().countSims(IPredicate.aThing) == 1 && this.offsetY != 0 && this.getIntersection().countSims(IPredicate.aWall) == 2) {
            this.finished = true;
            this.pickUpSafely();
        }

        // Checks to see if that was the last thing to pickup
        if (!this.frontIsClear() && this.getIntersection().countSims(IPredicate.aThing) == 1) {
            this.pickUpSafely();
            this.offsetY++;
            this.moveUp = true;
        }

        // Checks to see if there is nothing more to pickup on that street
        else if (!this.frontIsClear() && this.getIntersection().countSims(IPredicate.aThing) == 0) {
            this.goToLocation(this.getStreet(), this.getAvenue(), this.getStreet(), this.doorX);
            this.faceNorth();

            if (this.frontIsClear()) {
                this.move();
            }
            else {
                this.finished = true;
                this.goToLocation(this.getStreet(), this.getAvenue(), this.doorY + 1, this.doorX);
                this.goToLocation(this.getStreet(), this.getAvenue(), this.storageY - 1, this.storageX);
                this.faceNorth();
            }

            this.offsetY++;
            this.moveUp = false;
        }
        else {
            this.pickUpSafely();
        }

    }

    /**
     * @param type The door location type
     * @description Checks to see which type of door location is it based on the previous method and does the according action
     */
    private void findStorage(int type) {

        //Checks whether the door is on the right-most part of the cafeteria
        if (type == 1) {

            // Checks if this is the first time the robot is finding the storage
            if (this.firstTime) {
                this.frontMove();
                this.turnRight();
                this.frontMove();
                this.firstTime = false;
            } else {
                this.goToLocation(this.doorY, this.doorX, this.storageY, storageX);
            }

        }

        // Checks whether the door is NOT the right-most or the left-most part of the cafeteria
        else if (type == 2) {
            this.turnLeft();
            this.frontMove();
            this.turnRight();
            this.frontMove();
        }

        // Checks if the door is the left-most of the cafeteria
        else if (type == 3) {
            this.turnLeft();
            this.frontMove();
        }

        this.storageY = this.getStreet();
        this.storageX = this.getAvenue();

    }

    /**
     * @param startingY The Y-location of where to return to
     * @param startingX The X-location of where to return to
     * @param finalY    The Y-location of where to return from
     * @param finalX    The X-location of where to return from
     * @description Performs the calculations to make the robot return to the door
     */
    private void goToLocation(int startingY, int startingX, int finalY, int finalX) {

        int returnY = finalY - startingY;
        int returnX = finalX - startingX;

        // Checks to see which direction the robot has to orient
        if (returnX > 0) {
            this.faceEast();
        } else if (returnX < 0) {
            this.faceWest();
        }

        //Moves the required amount of times in the X
        for (int i = Math.abs(returnX); i > 0; i--) {
            this.move();
        }

        // Checks the direction the robot should orient
        if (returnY < 0) {
            this.faceNorth();
        } else if (returnY > 0) {
            this.faceSouth();
        }

        //Moves the required amount of times in the Y
        for (int i = Math.abs(returnY); i > 0; i--) {
            this.move();
        }

    }

    /**
     * @param type The type of where the door will spawn
     * @description The method to pick up the chairs and stack them
     */
    private void pickChairs(int type) {

        // Checks which door type the cafe has
        if (type == 1) {
            caseOneThreePick('W');
        } else if (type == 2) {
            caseTwoPick();
        } else {
            caseOneThreePick('E');
        }
    }

    /**
     * @description Method to pickup chairs if it is Case 1
     */
    private void caseOneThreePick(char direction) {
        boolean running = true;

        // Runs as long as there are chairs to pickup
        while (running) {

            // Checks to see if the robot should move to the next street
            if (this.offsetY > 0 && this.moveUp) {

                // Moves the required amount of times
                for (int i = 0; i < this.offsetY; i++) {
                    this.move();
                }
            }

            this.moveUp = true;

            // Checks which direction the robot has to orient based on the door spawn
            if (direction == 'W') {
                this.faceWest();
            } else if (direction == 'E') {
                this.faceEast();
            }

            // Moves the robot until it encounters a chair
            while (this.frontIsClear() && !this.canPickThing()) {
                this.move();
            }

            // Sees if the robot can pick something up
            if (this.frontIsClear()) {
                this.pickUpSafely();
            } else {
                this.advanceStreet();
            }

            // Checks if the robot has finished
            if (this.finished) {
                running = false;
            }

        }

    }

    /**
     * @description Method to pickup chairs if it is Case 2
     */
    private void caseTwoPick() {

        boolean running = true;
        boolean clearLeft = true;
        boolean clearRight = false;

        // Runs as long as the robot has to clean up the cafeteria
        while (running) {

            // Runs as long as the robot has to clear the left side of the door
            while (clearLeft) {

                // Checks if the robot has to increment the street
                if (this.offsetY > 0 && this.moveUp) {

                    // Moves the required amount of times
                    for (int i = 0; i < this.offsetY; i++) {
                        this.move();
                    }
                }

                this.moveUp = true;
                this.faceWest();

                // Moves as long as the robot can't pick anything up
                while (this.frontIsClear() && !this.canPickThing()) {
                    this.move();
                }

                // Checks if the robot is at the left end of the cafe and there's more to pickup
                if (this.frontIsClear() || (!this.frontIsClear() && this.getIntersection().countSims(IPredicate.aThing) > 1)) {
                    this.pickUpSafely();
                }

                // Checks if the robot is at the left end of the cafe and there is one more chair to pickup
                else if (!this.frontIsClear() && this.getIntersection().countSims(IPredicate.aThing) == 1) {
                    this.pickUpSafely();
                    clearLeft = false;
                    clearRight = true;
                    this.goToLocation(this.getStreet(), this.getAvenue(), this.doorY, this.doorX);
                } else {
                    clearLeft = false;
                    clearRight = true;
                    this.moveUp = false;
                }

            }

            // Cleans up the right side of the cafe
            while (clearRight) {
                // Checks if the robot has to increment the street
                if (this.offsetY > 0 && this.moveUp) {
                    // Moves the required amount of times
                    for (int i = 0; i < this.offsetY; i++) {
                        this.move();
                    }
                }

                this.moveUp = true;
                this.faceEast();

                // Runs as long as the robot can't pick anything up
                while (this.frontIsClear() && !this.canPickThing()) {
                    this.move();
                }

                // Checks if the robot is at the right end of the cafe and there's more to pickup
                if (this.frontIsClear() || (!this.frontIsClear() && this.getIntersection().countSims(IPredicate.aThing) > 1)) {
                    this.pickUpSafely();
                } else {
                    clearLeft = true;
                    clearRight = false;
                    this.advanceStreet();
                }

            }

            // Sees if the robot is done cleaning up
            if (this.finished) {
                running = false;
            }

        }

    }

}
