package man;

import robocode.*;
import static robocode.util.Utils.normalRelativeAngle;
import static robocode.util.Utils.normalRelativeAngleDegrees;

/**
 * Robot with 90 Degrees direction to the enemy robot
 * The Dodge is based on the change of the enemy energy, we store enemy energy and everytime we scan it
 * we see if the new enemy energy is different from the last storaged, so we know that he shot a bullet
 * when we shot him and we hit he loses energy too, so everytime we hit him with a shot we update the storaged enemy
 * It dodges moving forward and backwards randomly so it can't be easily predicted
 */
public class Nix extends AdvancedRobot {
	double enemyEnergy = 100;
	double wallDistance = 125;
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
		
		// Loop forever
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
 
    	if(this.getX() < this.wallDistance || this.getY() < this.wallDistance 
    	|| this.battleFieldHeight - this.getY() < this.wallDistance 
    	|| this.battleFieldWidth - this.getX() < this.wallDistance) {
    		 double centerAngle = Math.atan2(getBattleFieldWidth()/2-getX(), getBattleFieldHeight()/2-getY());
    		 turnRightRadians(normalRelativeAngle(centerAngle - getHeadingRadians()));
    		 ahead(175);
    		 
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
	 * If the enemy is near we attack with full power
	 * Our scanner is always locked on enemy
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		//dodgeWall
		dodgeWall();
		//Lock enemy robot in radar
		double radarTurn = getHeadingRadians() + e.getBearingRadians() - getRadarHeadingRadians();
		setTurnRadarRightRadians(normalRelativeAngle(radarTurn));
		
    	//Rotate 90 degrees from enemy
    	setTurnRight(e.getBearing() + 90);
    	
    	//Full Power enemy robot
    	fullPowerBullet(e.getDistance());
    	
    	//Dodge the enemy bullet
		double newEnergy = e.getEnergy();
		dodgeEnemyBullet(newEnergy);
		dodgeWall();
    	// Calculate exact location of the robot
    	double absoluteBearing = getHeading() + e.getBearing();
    	double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
    	// If it's close enough, fire!
    	
    	setTurnGunRight(bearingFromGun);
		if (getGunHeat() == 0) {
			if(this.getEnergy()>4 || e.getEnergy() < 0.1) {
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
     */
    public void onHitWall(HitWallEvent e) {
    	setAhead(this.moveDirection*-1*150);
    }
    
    /**
     * If the robots collides with another robot, we change his direction and move away
     */
    public void onHitRobot(HitRobotEvent event) {
    	setAhead(this.moveDirection*-1*150);
    }
    
    /**
     * We use this function to see if our robot is near a wall
     * if he is we just move away from the wall
     */
    public void dodgeWall() {
    	if(this.getX() < this.wallDistance || this.getY() < this.wallDistance 
    	|| this.battleFieldHeight - this.getY() < this.wallDistance 
    	|| this.battleFieldWidth - this.getX() < this.wallDistance) {
    		setAhead(this.moveDirection*-1*100);
    	}
    }
    
    /**
     * We use this function to attack enemy with max bullet power when 
     * he gets close
     */
    public void fullPowerBullet(double distance) {
    	if(distance < 100) {
    		if (getGunHeat() == 0) {
    			setFire(3);
    		}
    	}
    }
    
    /**
     * In this function we compare enemy new energy with the old energy
     * if they are different it means he shot a bullet
     * In that case we use a randomInteger to get a random direction (front or back)
     * Then he also use a randomDistance to generate a random Integer between 1 and 100
     * We also add 125 to that randomDistance to have a secure distance
     * We move to dodge and just for the case we call dodgeWall() to prevent hitting wall
     */
    public void dodgeEnemyBullet(double newEnergy) {
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
   		 	randomDistance += 50;
   		 	
   		 	//Move left and Right 
   		 	setAhead(this.moveDirection*randomDistance);	
   	 	}		
    	
    }
}			