package man;
import robocode.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

 public class Nix extends AdvancedRobot {
	 double enemyEnergy = 100;
	 double wallDistance = 100;
	 int moveDirection = 1;
	 double battleFieldHeight;
	 double battleFieldWidth;
	 LinkedList<Point2D.Double> enemyCords;

     public void run() {
    	this.enemyCords = new LinkedList<Point2D.Double>();
    	this.battleFieldHeight = getBattleFieldHeight();
    	this.battleFieldWidth = getBattleFieldWidth();
    	setBodyColor(Color.pink);
 		setGunColor(Color.pink);
 		setRadarColor(Color.pink);
 		setScanColor(Color.pink);
 		setBulletColor(Color.pink);
        while (true) {
        	//Scanning all battlefield looking for enemys
        	setAdjustGunForRobotTurn(true);
            setAdjustRadarForGunTurn(true);
            turnRadarRightRadians(Double.POSITIVE_INFINITY);
        }
     }

     public void onScannedRobot(ScannedRobotEvent e) {
    	//Get enemy energy
    	double newEnergy = e.getEnergy();
    	//Rotate 90 degrees from enemy
    	setTurnRight(e.getBearing() + 90);
    	if(newEnergy != this.enemyEnergy) {
    		//Update enemy energy
    		this.enemyEnergy = newEnergy;
   		 	//Move left and Right 
   		 	setAhead(moveDirection*100);
   		 	moveDirection*=-1;
   	 	}
    	
    	// Calculate exact location of the robot
    	double absoluteBearing = getHeading() + e.getBearing();
    	double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
    	
    	// Calculate exacty coordinates of the robot
    	double enemyBearing = e.getBearing();
    	double enemyX = getX() + e.getDistance() * Math.sin(Math.toRadians(enemyBearing));
    	double enemyY = getY() + e.getDistance() * Math.cos(Math.toRadians(enemyBearing));
    	
    	// Save all the coordinates enemy was
    	this.enemyCords.addFirst(new Point2D.Double(enemyX, enemyY));

    	// If it's close enough, fire!
    	if (Math.abs(bearingFromGun) <= 3) {
    		turnGunRight(bearingFromGun);
    		// We check gun heat here, because calling fire()
    		// uses a turn, which could cause us to lose track
    		// of the other robot.
    		if (getGunHeat() == 0) {
    			fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
    		}
    	} // otherwise just set the gun to turn.
    	// Note:  This will have no effect until we call scan()
    	else {
    		turnGunRight(bearingFromGun);
    	}
    	// Generates another scan event if we see a robot.
    	// We only need to call this if the gun (and therefore radar)
    	// are not turning.  Otherwise, scan is called automatically.
    	if (bearingFromGun == 0) {
    		scan();
    	}
     }
     
     //Update enemy energy when he gets hit
     public void onBulletHit(BulletHitEvent event) {
    	 this.enemyEnergy = event.getEnergy();
     }
     
     //When you hit the wall just try to leave it
     public void onHitWall(HitWallEvent event) {
         setTurnRight(180);
         setAhead(100);
     }
    
 }