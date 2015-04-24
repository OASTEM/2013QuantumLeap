/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oastem.frc.ascent.shooting;

import org.oastem.frc.control.DriveSystem;

/**
 *
 * @author STEM
 */
public class GoalDriver implements Runnable {

    private double angle = 0.0;
    private double width = 0.0;
    
    private double lastAngle = 0.0;
    private double lastWidth = 0.0;
    
    private double left = 0.0;
    private double right = 0.0;
    private double lastLeft = 0.0;
    private double lastRight = 0.0;
    
    private long lastUpdate = System.currentTimeMillis();
    private final double DRIVE_SPEED = 0.33; // base driving speed
    private final int DRIVE_DELAY = 50;
    private final double GOAL_DISTANCE = 100;
    private final double ANGLE_DRIVE_RATIO = 0.33;
    private final double ZONE = 0.2; // for angle
    private final double GOAL_THRESHOLD = 5; // distance threshold
    private final double SPEED_THRESHOLD = 0.15; // threshold to average out motor speeds
    private boolean canShoot = false;
    private boolean stop = false;

    public GoalDriver() {
    }
    
    public GoalDriver(double angle, double width) {
        update(angle, width);
    }

    public void update(double angle, double width) {
        lastAngle = this.angle;
        lastWidth = this.width;
        this.angle = angle;
        this.width = width;
        lastUpdate = System.currentTimeMillis();
    }
    
    public boolean canShoot() {
        return canShoot;
    }
    
    public void stop() {
        stop = true;
    }

    public void run() {
        DriveSystem drive = DriveSystem.getInstance();
        //double ok = 0.0;
        while (true) {
            if (stop) { 
                break;
            } else if (System.currentTimeMillis() - lastUpdate > 5000) {
                long time = (System.currentTimeMillis() - lastUpdate)/1000;
                System.out.println("GoalDriver: No updates received for " + time + " seconds!");
                drive.tankDrive(0.0, 0.0);
            } else {
                double avgWidth = (lastWidth + width)/2;
                double avgAngle = (lastAngle + angle)/2;
                
                System.out.println("Angler PID Output: " + avgAngle);
                lastLeft = left;
                lastRight = right;
                
                left = 0.0;
                right = 0.0;
                
                double delta = avgWidth - GOAL_DISTANCE;
                System.out.println("Driver PID width: " + avgWidth);
                if (delta > GOAL_THRESHOLD) {
                    // too close, back up
                    left = -DRIVE_SPEED * delta/50;
                    right = -DRIVE_SPEED * delta/50;
                } else if (delta < -GOAL_THRESHOLD) {
                    // too far, go forward
                    left = DRIVE_SPEED * delta/50;
                    right = DRIVE_SPEED * delta/50;
                }
                
                if (avgAngle < -ZONE) {
                    left += avgAngle * ANGLE_DRIVE_RATIO;
                    right -= avgAngle * ANGLE_DRIVE_RATIO;
                } else if (avgAngle > ZONE) {
                    left += avgAngle * ANGLE_DRIVE_RATIO;
                    right -= avgAngle * ANGLE_DRIVE_RATIO;
                }
                
                // slowly rev up the motor instead of slamming it
                double dLeft = left - lastLeft;
                double dRight = right - lastRight;
                if (Math.abs(dLeft) < SPEED_THRESHOLD) {
                    left = (lastLeft + left)/2;
                }
                
                if (Math.abs(dRight) < SPEED_THRESHOLD) {
                    right = (lastRight + right)/2;
                }
                
                canShoot = (Math.abs(left) < ZONE && Math.abs(right) < ZONE);
                
                drive.tankDrive(left, right);
            }
            try {
                Thread.sleep(DRIVE_DELAY);
            } catch (Exception e) {
            
            }
        }
    }
}