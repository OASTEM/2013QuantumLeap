/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package org.oastem.frc.ascent;

import org.oastem.frc.control.DriveSystem;
import org.oastem.frc.*;
import org.oastem.frc.ascent.roborealm.*;

import edu.wpi.first.wpilibj.ADXL345_I2C.Axes;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.buttons.DigitalIOButton;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.oastem.frc.ascent.roborealm.RealmServer;
import org.oastem.frc.ascent.external.FastMath;
import org.oastem.frc.ascent.shooting.GoalDriver;
import org.oastem.frc.ascent.shooting.ThreadedShooter;
import org.oastem.frc.pid.TargetOutput;

public class RobotMain extends SimpleRobot {

    // Victors
    public static final int TRAAM = 5;//5
    public static final int SHOOTER_WHEEL = 4;
    public static final int FEED_ARM = 9;//10//9
    public static final int LEFT_DRIVE = 2;
    public static final int RIGHT_DRIVE = 1;
    public static final int LEFT_DRIVE_2 = 3;
    public static final int RIGHT_DRIVE_2 = 6;
    
    private int TARGET_WIDTH_SOURCE = 0;
    private int TARGET_ANGLE_SOURCE = 1;
    private int TARGET_HEIGHT_SOURCE = 2;
    //private ADXL345_I2C accel = new ADXL345_I2C(1, ADXL345_I2C.DataFormat_Range.k2G);
    // These define the buttons used to manipulate the robot.
    // TRAAM up/down buttons
    private final int TRAAM_UP_BUTTON = 3;
    private final int TRAAM_DOWN_BUTTON = 2;
    // Engage the shooter and deliver payload
    private final int FIRE_BUTTON = 1;
    // Turn on the shooting wheel mechanism
    private final int SHOOTER_ON_BUTTON = 4;
    private final int SHOOTER_OFF_BUTTON = 5;
    private final int PARK_BUTTON = 8;
    private final int RELEASE_BUTTON = 9;
    // Is the robot in autonomous mode?
    public static boolean isAutonomous = false;
    // Scales the joystick drive
    private double joyScale = 0.75;
    // Controls the robot drive mechanism
    private DriveSystem drive = DriveSystem.getInstance();
    // Joysticks.
    private Joystick left = new Joystick(1);
    private Joystick right = new Joystick(2);
    private DualJoystick dual = new DualJoystick(left, right, Hand.kLeft);
    // Experimental...
    // The encoder. Plug into ports 5 and 7.
    //private Encoder enc = new Encoder(5, 7, false, CounterBase.EncodingType.k1X);
    private DigitalInput fireLim = new DigitalInput(1);
    //private DigitalInput traamMin = new DigitalInput(2);
    //private DigitalInput traamMax = new DigitalInput(3);
    //private DigitalInput encMagInc = new DigitalInput(8);
    //private DigitalInput encMagDec = new DigitalInput(9);
    //private DigitalIOButton dib = new DigitalIOButton(1);
    private long ticks = 0;
    private int tickTime = 5;
    private double latestEnc = 0.0;
    private double rawEnc = 0.0;
    private double lastRawEnc = 0.0;
    private boolean feedEngaged = false;
    private double dist = 0.0;
    private double shooterSpeed = 0.0;
    private double lastDist = 0.0;
    private NetworkTable table;
    private boolean firing = false;
    private TargetOutput to = new TargetOutput();
    private long lastFire = 0;
    private String[] debug = new String[6];
    private boolean parked = false;

    protected void robotInit() {
        Debug.clear();
        Debug.log(1, 1, "Robot initialized");

        drive.initializeDrive(RIGHT_DRIVE, LEFT_DRIVE);
        //drive.addVictor(TRAAM);
        drive.addVictor(SHOOTER_WHEEL);
        drive.addVictor(FEED_ARM);
        //drive.setSafetyEnabled(false);
        drive.setSafety(false);
        
        System.out.println("End of RobotInit");
        
        //park(debug);

        //enc.start();
        //enc.setDistancePerPulse(0.0879 * 2);

        //table = NetworkTable.getTable("camera");
    }

    public void autonomous() {
        park(debug);
        
        Debug.clear();
        //String[] debug = new String[6];
        this.isAutonomous = true;
        
        Debug.log(1, 1, "Autonomous online");
        
        /*table.putBoolean("autonomous", true);
        
        shooterSpeed = 0.9;
        controlShooter(true, debug);
        Debug.log(2, 1, "Spinning up wheel");
        Timer.delay(0.4);
        shooterSpeed = 0.55;
        
        Timer.delay(3.6);
        
        for (int i = 0; i < 3; i++) {
            Debug.log(2, 1, "Shooting frisbee " + (i + 1));
            dispensePayload(debug);
            Timer.delay(0.4);
        }
        
        shooterSpeed = 0.0;
        controlShooter(false, debug);
        Debug.log(2, 1, "Wheel off, lowering TRAAM");
        
        this.park(debug);
        
        Debug.log(3, 1, "lol jk");
        drive.set(TRAAM, -0.85);
        long start = System.currentTimeMillis();
        while(isAutonomous()) {
            if (!traamMin.get()) {
                drive.set(TRAAM, 0.0);
                Debug.log(2, 1, "TRAAM lowered.");
                Debug.clearLine(3);
                break;
            }

            if (System.currentTimeMillis() - start > 10000) {
                Debug.log(3, 1, "Uhhh... check the TRAAM...");
                break;
            }
            
            if (left.getRawButton(5)) break;
        }//*/
        

        /*table.putBoolean("autonomous", true);
        Debug.log(2, 1, "Waiting for Dashboard connection");
        long time = System.currentTimeMillis();
        boolean connected = false;
        while(true) {
            if (!connected && System.currentTimeMillis() - time > 5000) {
                Debug.log(2, 1, "Connection timed out");
                break;
            } else {
                if (!connected && table.containsKey("autoOk")) {
                    connected = true;
                    Debug.log(2, 1, "Connected.");
                }
                
                if (table.containsKey("targetSelected")) {
                    double height = table.getNumber("height");
                    double width = table.getNumber("width");
                    double angle = table.getNumber("angle");
                    
                    Debug.log(3, 1, "Height: " + height);
                    Debug.log(4, 1, "Angle: " + angle);
                    Debug.log(5, 1, "Width: " + width);
                    
                    double[] target = new double[] { width, angle, height };
                    autoFire(target);
                }
            }
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                
            }
        }*/
        
        Debug.log(1, 1, "Autonomous complete");
        //table.putBoolean("autonomous", false);
        //table.putBoolean("fired", true);
        //table.putNumber("targetSelected", -1);
        firing = false;

        this.isAutonomous = false;
    }

    private double scaleZ(double rawZ) {
        return 0.5 - 0.5 * rawZ;
    }

    public void operatorControl() {
        //String[] debug = new String[6];

        
        Debug.clear();
        
        //park(debug);

        boolean shooterPressed = false;
        boolean dashboardControlWheel = false;
        boolean crap = false;
        while (isOperatorControl() && isEnabled()) {
            long currentTime = System.currentTimeMillis();
            
            if (shooterSpeed > 1.0) {
                shooterSpeed = 1.0;
                this.controlShooter(true, debug);
            } else if (shooterSpeed < 0.0) {
                shooterSpeed = 0.0;
                this.controlShooter(true, debug);
            }
            
            /*if (currentTime - lastFire > 5000) {
                park(debug);
            }*/
            

            // Drives the robot using joystick control
            this.doArcadeDrive(debug);

            // Camera
            /*if (table.isConnected() && table.containsKey("online")) {
                debug[0] = "Tracker online...";
                if (table.containsKey("targetSelected") && table.getNumber("targetSelected") != -1) {
                    double width = table.getNumber("width");
                    double angle = table.getNumber("angle");
                    double height = table.getNumber("height");

                    debug[3] = "Height: " + height;
                    debug[4] = "Angle: " + angle;
                    debug[5] = "Width: " + width;

                    double[] params = (new double[]{width, angle, height});
                    autoFire(params);
                    //System.out.println("Updated... fire! " + (firing ? "true" : "false"));
                    //autoFire();


                }
            } else {
                debug[0] = "NT Offline";
            }*/

            // Controls the TRAAM (moves up and down)

            /*if (dual.getRawButton(TRAAM_UP_BUTTON)) {
                this.controlTraam(Direction.UP, debug);
            } else if (dual.getRawButton(TRAAM_DOWN_BUTTON)) {
                this.controlTraam(Direction.DOWN, debug);
            } else {
                this.controlTraam(Direction.RIGHT, debug); // RIGHT = turn off.
            }*/
            
            if (dashboardControlWheel) {
                debug[0] = "Dashboard given control";
                try {
                    shooterSpeed = SmartDashboard.getNumber("Slider 1")/100;
                    this.controlShooter(true, debug);
                } catch (Exception e) {
                    
                }
            }
            
            if (!crap && left.getRawButton(7)) {
                crap = true;
                dashboardControlWheel = !dashboardControlWheel;
            }
            
            if (!left.getRawButton(7)) {
                crap = false;
            }

            // Controls the shooter wheel.
            if (!left.getRawButton(SHOOTER_ON_BUTTON) && !left.getRawButton(SHOOTER_OFF_BUTTON)) {
                shooterPressed = false;
            }

            if (!shooterPressed && left.getRawButton(SHOOTER_ON_BUTTON)) {
                shooterPressed = true;
                if (shooterSpeed < 1.0) {
                    shooterSpeed = shooterSpeed + 0.1;
                }
                this.controlShooter(true, debug);
            } else if (!shooterPressed && left.getRawButton(SHOOTER_OFF_BUTTON)) {
                shooterPressed = true;
                if (shooterSpeed > 0.0) {
                    shooterSpeed = shooterSpeed - 0.1;
                }
                this.controlShooter(true, debug);
            }

            // Fires a frisbee from the magazine.
            if (!feedEngaged && currentTime - lastFire > 1200 && dual.getRawButton(FIRE_BUTTON)) {
                System.out.println("About to fire");
                this.dispensePayload(debug);
                lastFire = currentTime;
            }

            if (left.getRawButton(PARK_BUTTON)) {
                this.park(debug);
                System.out.println("Button 8 pressed");
            }

            if (left.getRawButton(RELEASE_BUTTON)) {
                this.release(debug);
            }
            
            if (left.getRawButton(11)) {
                shooterSpeed = 1.0;
                this.controlShooter(true, debug);
            }
            
            if (left.getRawButton(10)) {
                shooterSpeed = 0.0;
                this.controlShooter(false, debug);
            }
            
            /*if (left.getRawButton(6)) {
                if (tr != null) {
                    tr.stop();
                    gd.stop();
                }
            }*/

            debug[1] = "Shooter: " + shooterSpeed;

            debug[2] = fireLim.get() ? "t" : "f"; //"RPM: " + latestEnc;
            long timeDelta = currentTime - ticks;
            if (timeDelta > 250) {
                Debug.clear();
                //latestEnc = Math.abs((enc.getRate() * 60) / 360);
                ticks = currentTime;
            }

            // SmartDashboard stuff
            //SmartDashboard.putNumber("Shooter Wheel", shooterSpeed);

            // Log the data to the DS and do a small delay before next iteration.
            Debug.log(debug);
        }
    }

    /**
     * Runs after robot is disabled. Clears debug window.
     */
    public void disabled() {
        park(debug);
        //table.putBoolean("fired", true);
        //table.putNumber("targetSelected", -1);
        firing = false;
        Debug.log(1, 1, "GG NO RE");
        //Debug.clear();
    }
    
    /*private ThreadedShooter tr;
    private GoalDriver gd;
    private Thread gdt;
    private int timesFired = 0; 
    private void autoFire(double[] args) {
        String[] debug = new String[6];
        if (!firing) { 
            firing = true;
            tr = new ThreadedShooter(args, 2);
            gd = new GoalDriver(args[TARGET_ANGLE_SOURCE], args[TARGET_WIDTH_SOURCE]);
            gdt = new Thread(gd);
            
            tr.attach(TARGET_HEIGHT_SOURCE, to.angleTraamToGoal);
            tr.attach(TARGET_HEIGHT_SOURCE, to.powerWheelForGoal);
            tr.run();
            gdt.start();
        } else {
            gd.update(args[TARGET_ANGLE_SOURCE], args[TARGET_WIDTH_SOURCE]);
            tr.update(args);
            
            if (gd.canShoot() && timesFired < 4 && !feedEngaged &&
                    System.currentTimeMillis() - lastFire > 1500) {
                this.dispensePayload(debug);
                timesFired++;
            } else if (timesFired >= 4) {
                firing = false;
                table.putBoolean("fired", true);
                table.putNumber("targetSelected", -1);
            }
        }
    }*/

    /*private void controlTraam(Direction dir, String[] debug) {
        if (dir.equals(Direction.UP)) {
            if (!traamMax.get()) {
                drive.set(TRAAM, 0.0);

                debug[0] = "TRAAM at maximum.";
                return;
            }
            drive.set(TRAAM, 0.7);
        } else if (dir.equals(Direction.DOWN)) {
            if (!traamMin.get()) {
                drive.set(TRAAM, 0.0);

                debug[0] = "TRAAM at minimum.";
                return;
            }
            drive.set(TRAAM, -0.5);
        } else {
            drive.set(TRAAM, 0.0);
        }
    }*/

    private void controlShooter(boolean enabled, String[] debug) {
        drive.set(SHOOTER_WHEEL, enabled ? shooterSpeed : 0.0);
    }

    private void dispensePayload(String[] debug) {
        //if (parked) this.release(debug);
        System.out.println("About to park!");
        this.park(debug);
        Timer.delay(0.3);
        System.out.println("About to release!");
        this.release(debug);

        this.temporarilyAccelerateShooter();
    }

    private void temporarilyAccelerateShooter() {
        // should theoretically work.
        drive.set(SHOOTER_WHEEL, shooterSpeed + 0.1);
        Timer.delay(1.0);
        drive.set(SHOOTER_WHEEL, shooterSpeed);
    }

    private void park(String[] debug) {
        feedEngaged = true;
        parked = true;
        drive.set(FEED_ARM, 0.3);
        System.out.println("To For loop!");
        for (; fireLim.get() ;) {
            //Timer.delay(period);
        //int thresh = 0;
        //while(!fireLim.get() && thresh < 1000){
            System.out.println("OMG IN LOOP!");
            debug[1] = "dat loop!";
            if(left.getRawButton(3)){
                debug[1] = "y u break dis??!";
                break;
                
            }
            //thresh++;
            
        }
        System.out.println("Out of loop");
        drive.set(FEED_ARM, 0.0);

        feedEngaged = false;
    }

    private void release(String[] debug) {
        feedEngaged = true;
        drive.set(FEED_ARM, -0.3);
        //Timer.delay(avgStart * 1/0.4);
        Timer.delay(0.14);
        System.out.println("I am in release mode");
        debug[1] = "Gonna fire, dude";

        drive.set(FEED_ARM, 0.0);

        parked = false;
        feedEngaged = false;
    }

    private void doOrigArcadeDrive(String[] debug) {
        joyScale = scaleZ(left.getZ());

        double leftMove = (left.getY() * joyScale) * -1;
        double rightMove = (left.getX() * joyScale);

        debug[3] = "Scale: " + joyScale;
        debug[4] = "Left: " + leftMove;
        debug[5] = "Right: " + rightMove;

        drive.arcadeDrive(leftMove, rightMove);
    }

    private void doArcadeDrive(String[] debug) {
        double leftMove = 0.0;
        double rightMove = 0.0;
        double zone = 0.04;

        joyScale = scaleZ(left.getZ());

        double x = left.getX();
        double y = left.getY();

        if (Math.abs(y) > zone) {
            leftMove = y;
            rightMove = y;
        }

        if (Math.abs(x) > zone) {
            leftMove = correct(leftMove + x);
            rightMove = correct(rightMove - x);
        }

        leftMove *= joyScale * -1;
        rightMove *= joyScale * -1;

        debug[3] = "Scale: " + joyScale;
        debug[4] = "Left: " + leftMove;
        debug[5] = "Right: " + rightMove;

        drive.tankDrive(leftMove, rightMove);
    }

    private double correct(double val) {
        if (val > 1.0) {
            return 1.0;
        }
        if (val < -1.0) {
            return -1.0;
        }
        return val;
    }
}
