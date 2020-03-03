package man;

import robocode.*;
import robocode.util.Utils;

import static robocode.util.Utils.normalRelativeAngleDegrees;
import static robocode.util.Utils.normalRelativeAngle;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

/**
 * WaveBullet used on Guess Factor
 * This Class was copied from robowiki only to improve the attack algorithm of the robot
 * @author http://robowiki.net/wiki/GuessFactor_Targeting_Tutorial
 */
class WaveBullet{
	private double startX, startY, startBearing, power;
	private long   fireTime;
	private int    direction;
	private int[]  returnSegment;
 
	public WaveBullet(double x, double y, double bearing, double power, int direction, long time, int[] segment)
	{
		startX         = x;
		startY         = y;
		startBearing   = bearing;
		this.power     = power;
		this.direction = direction;
		fireTime       = time;
		returnSegment  = segment;
	}
	
	public double getBulletSpeed()
	{
		return 20 - power * 3;
	}
 
	public double maxEscapeAngle()
	{
		return Math.asin(8 / getBulletSpeed());
	}
	
	public boolean checkHit(double enemyX, double enemyY, long currentTime)
	{
		// if the distance from the wave origin to our enemy has passed
		// the distance the bullet would have traveled...
		if (Point2D.distance(startX, startY, enemyX, enemyY) <= (currentTime - fireTime) * getBulletSpeed())
		{
			double desiredDirection = Math.atan2(enemyX - startX, enemyY - startY);
			double angleOffset = Utils.normalRelativeAngle(desiredDirection - startBearing);
			double guessFactor = Math.max(-1, Math.min(1, angleOffset / maxEscapeAngle())) * direction;
			int index = (int) Math.round((returnSegment.length - 1) /2 * (guessFactor + 1));
			returnSegment[index]++;
			return true;
		}
		return false;
	}
}

/**
 * Robot with 90 Degrees direction to the enemy robot
 * The Dodge is based on the change of the enemy energy, we store enemy energy and everytime we scan it
 * we see if the new enemy energy is different from the last storaged, so we know that he shot a bullet
 * when we shot him and we hit he loses energy too, so everytime we hit him with a shot we update the storaged enemy
 * It dodges moving forward and backwards randomly so it can't be easily predicted
 * It uses Guess Factor to predict enemy movements 
 */
public class Nix extends AdvancedRobot {
	double enemyEnergy = 100;
	double wallDistance = 100;
	int dodgeDistance = 1;
	int moveDirection = 1;
	double battleFieldHeight;
	double battleFieldWidth;
	
	//List to store enemyCords to dodge enemy
	LinkedList<Point2D.Double> enemyCords;
	
	//Guess Factor from RoboWiki
	ArrayList<WaveBullet> waves = new ArrayList<WaveBullet>();
	static int[] stats = new int[31]; // 31 is the number of unique GuessFactors we're using
	// Note: this must be odd number so we can get
	// GuessFactor 0 at middle.
	int direction = 1;
	
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
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
 
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
		//Lock enemy robot in radar
		double radarTurn = getHeadingRadians() + e.getBearingRadians() - getRadarHeadingRadians();
		setTurnRadarRightRadians(normalRelativeAngle(radarTurn));
		
		double newEnergy = e.getEnergy();
    	//Rotate 90 degrees from enemy
    	setTurnRight(e.getBearing() + 90);
    	if(e.getDistance() < 200) {
    		System.out.println("Perto!");
    		setTurnLeft(normalRelativeAngleDegrees(radarTurn)+180);
    		setAhead(100);
    	}
    	if(newEnergy != this.enemyEnergy) {
    		//Update enemy energy
    		this.enemyEnergy = newEnergy;
    		
    		//Add random factor so it can't be easily predicted
   		 	int randomInteger = -10 + (int) (Math.random() * ((10 - (-10)) + 1));
   		 	if(randomInteger == 0)
   		 		this.moveDirection*=-1;
   		 	else
   		 		this.moveDirection*= (randomInteger/Math.abs(randomInteger));
   		 	
   		 	//Move left and Right 
   		 	if(this.dodgeDistance == 1) {
   		 		setAhead(this.moveDirection*100);
   		 		this.dodgeDistance = 0;
   		 	}
   		 	else {
   	   		 	setAhead(this.moveDirection*200); 	
   		 		this.dodgeDistance = 1;
   		 	}
   	 	}		
    	/* Enemy coordinates calculation

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
		
		*/
    	
    	//------------------------------------------------------------------------------------------------
    	// Enemy absolute bearing, you can use your one if you already declare it.
    	double absBearing = getHeadingRadians() + e.getBearingRadians();
    	 
    	// find our enemy's location:
    	double enemyX = getX() + Math.sin(absBearing) * e.getDistance();
    	double enemyY = getY() + Math.cos(absBearing) * e.getDistance();
    	 
    	// Let's process the waves now:
    	for (int i=0; i < waves.size(); i++)
    	{
    		WaveBullet currentWave = (WaveBullet)waves.get(i);
    		if (currentWave.checkHit(enemyX, enemyY, getTime()))
    		{
    			waves.remove(currentWave);
    			i--;
    		}
    	}
    	 
    	// Because of the velocity of the bullet and the escape angle of the enemy
    	// the power = 1.72 is the best
    	double power = 1.72;
    	// don't try to figure out the direction they're moving 
    	// they're not moving, just use the direction we had before
    	if (e.getVelocity() != 0)
    	{
    		if (Math.sin(e.getHeadingRadians()-absBearing)*e.getVelocity() < 0) {
    			direction = -1;
    		}
    		else {
    			direction = 1;
    		}
    	}
    	WaveBullet newWave = new WaveBullet(getX(), getY(), absBearing, power, direction, getTime(), stats);
    	int bestindex = 15;	// initialize it to be in the middle, guessfactor 0.
    	for (int i=0; i<31; i++)
    		if (stats[bestindex] < stats[i])
    			bestindex = i;
    	 
    	// if enemy stopped just shoot at the exact position
    	if(e.getVelocity() == 0)
    		bestindex = 15;
    	
    	// this should do the opposite of the math in the WaveBullet:
    	double guessfactor = (double)(bestindex - (stats.length - 1) / 2) / ((stats.length - 1) / 2);
    	double angleOffset = direction * guessfactor * newWave.maxEscapeAngle();
    	double gunAdjust = Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + angleOffset);
    	setTurnGunRightRadians(gunAdjust);
    	fire(power);
        waves.add(newWave);
	}

	//Update enemy energy when he gets hit
    public void onBulletHit(BulletHitEvent event) {
   	 	this.enemyEnergy = event.getEnergy();
    }
    
    //Walks away from the wall
    public void onHitWall(HitWallEvent e) {
    	setAhead(this.moveDirection*-1*100);
    }
    
    public void onHitRobot(HitRobotEvent event) {
    	setAhead(this.moveDirection*-1*100);
    }
}			