
package man;

import robocode.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public class Nix extends AdvancedRobot {
	int waitFire = 0;
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
		// Set colors
		setBodyColor(Color.pink);
		setGunColor(Color.pink);
		setRadarColor(Color.pink);
		setScanColor(Color.pink);
		setBulletColor(Color.pink);

		// Loop forever
		while (true) {
			turnGunRight(360); // Scans automatically
		}
	}
	/**
	 * This function is called when he detects an opponent
	 * It makes the robot to dodge from the enemy bullet
	 * It makes the robot to fire to the enemy bullet
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
    	this.dodgeEnemy(e);
		// Calculate exact location of the robot
		double absoluteBearing = getHeading() + e.getBearing();
		double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
		
		// Calculate coordinates of the enemy robot
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
    
    //Walks away from the wall
    public void onHitWall(HitWallEvent e) {
    	setAhead(this.moveDirection*-1*100);
    }
    
    public void dodgeEnemy(ScannedRobotEvent e) {
		double newEnergy = e.getEnergy();
    	//Rotate 90 degrees from enemy
    	setTurnRight(e.getBearing() + 90);
    	if(newEnergy != this.enemyEnergy) {
    		//Update enemy energy
    		this.enemyEnergy = newEnergy;
    		//Add random factor so it can't be easily predicted
   		 	int randomInteger = -10 + (int) (Math.random() * ((10 - (-10)) + 1));
   		 	if(randomInteger == 0)
   		 		moveDirection*=-1;
   		 	else
   		 		moveDirection*= (randomInteger/Math.abs(randomInteger));
   		 	//Move left and Right 
   		 	setAhead(moveDirection*100);
   	 	}
    }
    
}				