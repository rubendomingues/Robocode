package man;

import robocode.*;
import static robocode.util.Utils.normalRelativeAngle;
import java.awt.*;

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
	
	/**
	 * In this function we store the battleField dimensions,
	 * We change our robot colors
	 * We make our radar, gun and robot be independent from each other
	 * We also see if our robot starts near the wall, if it does we rotate our robot to the center and move forward
	 * We finally rotate our radar to find enemy robot
	 */
	public void run() {
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
	 * In this function we always rotate our robot in a 90 degrees from the opponent
	 * If the enemy shoots we just need to move forward or backward to dodge
	 * We added a random factor to the direction our robot dodges and the distance he goes (The distance is going from 126 to 225)
	 * We store enemy energy and when his energy changes we know he shot a bullet so we need to dodge it
	 * If the enemy is near we just run away
	 * We calculate the enemy next position (If he change his direction our robot we probably miss the bullet
	 * Our scanner is always locked on enemy
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
    	
    	// Rotate gun for enemy
		setTurnGunRightRadians(bearingFromGun);
		// Only shoot if he can
    	if (getGunHeat() == 0) {
    		// For security he doesn't shoot while low energy, only if the enemy is below 1 energy
			if(this.getEnergy()>4 || e.getEnergy() < 1) {
				setFire(1.72);
			}
		}
	}

	/**
	 * This function updates the storaged enemy health 
	 * when he gets hit
	 */
    public void onBulletHit(BulletHitEvent event) {
   	 	this.enemyEnergy = event.getEnergy();
    }
    
    /**
     * If the robot hit the wall, we change his direction and move away
     * Just for precaution we call the dodgeWall() function
     */
    public void onHitWall(HitWallEvent e) {
    	this.moveDirection*=-1;
    	setAhead(this.moveDirection*150);
    	dodgeWall();
    }
    
    /**
     * If the robots collides with another robot, we change his direction and move away
     * Just for precaution we call the dodgeWall() function
     */
    public void onHitRobot(HitRobotEvent event) {
    	this.moveDirection*=-1;
    	setAhead(this.moveDirection*150);
    	dodgeWall();
    }
    
    /**
     * We use this function to see if our robot is near a wall
     * if he is we just move away from the wall
     */
    public void dodgeWall() {
    	if(this.getX() < 75 || this.getY() < 75 || this.battleFieldHeight - this.getY() < 75 || this.battleFieldWidth - this.getX() < 75) {
    		setAhead(this.moveDirection*-1*150);
    	}
    }
}			