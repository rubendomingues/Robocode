package man;

import robocode.*;
import static robocode.util.Utils.normalRelativeAngle;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

/**
 * Robot with 90 Degrees direction to the enemy robot
 * The Dodge is based on the change of the enemy energy, we store enemy energy and everytime we scan it
 * we see if the new enemy energy is different from the last storaged, so we know that he shot a bullet
 * when we shot him and we hit he loses energy too, so everytime we hit him with a shot we update the storaged enemy
 * It dodges moving forward and backwards randomly so it can't be easily predicted
 */
public class Nix extends AdvancedRobot {
	double enemyEnergy = 100;
	double wallDistance = 100;
	int moveDirection = 1;
	double battleFieldHeight;
	double battleFieldWidth;
	
	//List to store enemyCords to dodge enemy
	LinkedList<Point2D.Double> enemyCords;
	
	public void run() {
		//Store enemy cords
		this.enemyCords = new LinkedList<Point2D.Double>();
		//Get batteField dimensions
    	this.battleFieldHeight = getBattleFieldHeight();
    	this.battleFieldWidth = getBattleFieldWidth();
		// Set colors
		setBodyColor(Color.pink);
		setGunColor(Color.pink);
		setRadarColor(Color.pink);
		setScanColor(Color.pink);
		setBulletColor(Color.pink);
		
		// Loop forever
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
 
    	if(this.getX() < 75 || this.getY() < 75 || this.battleFieldHeight - this.getY() < 75 || this.battleFieldWidth - this.getX() < 75) {
    		 double centerAngle = Math.atan2(getBattleFieldWidth()/2-getX(), getBattleFieldHeight()/2-getY());
    		 setTurnRightRadians(normalRelativeAngle(centerAngle - getHeadingRadians()));
    		 ahead(75);
    		 execute();
    	}
    	
		// Turn the radar to find enemy robot
	    turnRadarRightRadians(Double.POSITIVE_INFINITY);
		while (true) {
			scan();
		}
	}
	/**
	 * This function is called when he detects an opponent
	 * It makes the robot to dodge from the enemy bullet
	 * It makes the robot to fire to the enemy bullet
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		dodgeWall();
				//Lock enemy robot in radar
		double radarTurn = getHeadingRadians() + e.getBearingRadians() - getRadarHeadingRadians();
		setTurnRadarRightRadians(normalRelativeAngle(radarTurn));
		
		double newEnergy = e.getEnergy();
    	//Rotate 90 degrees from enemy
    	setTurnRight(e.getBearing() + 90);
    	
    	//Dodge the enemy robot
    	if(e.getDistance() < 100) {
    		setAhead(this.moveDirection*100);
			dodgeWall();
    	}
    	
    	//Dodge the enemy bullet
    	if(newEnergy != this.enemyEnergy) {
    		//Update enemy energy
    		this.enemyEnergy = newEnergy;
    		
    		//Add random factor to change direction so it can't be easily predicted
   		 	int randomInteger = -10 + (int) (Math.random() * ((10 - (-10)) + 1));
   		 	if(randomInteger == 0)
   		 		this.moveDirection*=-1;
   		 	else
   		 		this.moveDirection*= (randomInteger/Math.abs(randomInteger));
   		 	
   		 	//Add random factor to change the distance he runs so it can't be easily predicted
   		 	int randomDistance = (int) (Math.random() * (100 - 1)) + 1;
   		 	//Add to 125 to that distance because we don't want him to run small distance
   		 	randomDistance += 125;
   		 	
   		 	//Move left and Right 
   		 	setAhead(this.moveDirection*randomDistance);	
			dodgeWall();
   	 	}		
    	
    	// Calculate exact location of the robot
    	double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
    	// Using enemy velocity predict where he is gonna go if he dont change is direction
    	double bearingFromGun = normalRelativeAngle(absoluteBearing - getGunHeadingRadians() + (e.getVelocity() * Math.sin(e.getHeadingRadians() - absoluteBearing) / 13.0));
    	
    			
    	// Calculate coordinates of the enemy robot
    	double enemyBearing = e.getBearing();
    	double enemyX = getX() + e.getDistance() * Math.sin(Math.toRadians(enemyBearing));
    	double enemyY = getY() + e.getDistance() * Math.cos(Math.toRadians(enemyBearing));
    	    	
    	// Save all the coordinates enemy was
    	this.enemyCords.addFirst(new Point2D.Double(enemyX, enemyY));
    	// If it's close enough, fire!
    	if (Math.abs(bearingFromGun) <= 3) {
    		setTurnGunRightRadians(bearingFromGun);
    		// We check gun heat here, because calling fire()
    		// uses a turn, which could cause us to lose track
    		// of the other robot.
    		if (getGunHeat() == 0) {
    			if(this.getEnergy()>4) {
    				fire(1.72);
    			}
    		}
    	} // otherwise just set the gun to turn.
    	// Note:  This will have no effect until we call scan()
    	else {
    		setTurnGunRight(bearingFromGun);
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
    	this.moveDirection*=-1;
    	setAhead(this.moveDirection*150);
    	dodgeWall();
    }
    
    //Run when hitted by enemy robot
    public void onHitRobot(HitRobotEvent event) {
    	this.moveDirection*=-1;
    	setAhead(this.moveDirection*150);
    	dodgeWall();
    }
    
    //Calculate if near a wall and dodge it
    public void dodgeWall() {
    	if(this.getX() < 75 || this.getY() < 75 || this.battleFieldHeight - this.getY() < 75 || this.battleFieldWidth - this.getX() < 75) {
    		setAhead(this.moveDirection*-1*150);
    	}
    }
}			