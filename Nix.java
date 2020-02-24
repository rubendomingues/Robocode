package man;
import robocode.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;

 public class Nix extends AdvancedRobot {
     public void run() {
         while (true) {
        	 //Scanning all battlefield looking for enemys
        	 setAdjustGunForRobotTurn(true);
             setAdjustRadarForGunTurn(true);
             turnRadarRightRadians(Double.POSITIVE_INFINITY);
         }
     }

     public void onScannedRobot(ScannedRobotEvent e) {
    	//Rotate 90 degrees from enemy
    	setTurnRight(e.getBearing() + 90);
    	// Calculate exact location of the robot
 		double absoluteBearing = getHeading() + e.getBearing();
 		double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());

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
 }