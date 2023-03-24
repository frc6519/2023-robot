// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.UsbCamera;
import edu.wpi.first.cscore.VideoMode.PixelFormat;
import edu.wpi.first.hal.PowerJNI;
import edu.wpi.first.wpilibj.SerialPort.Port;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.motorcontrol.Spark;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends TimedRobot {
  // Autonomous & Teleop vars
  private final Timer timer = new Timer();
  private Timer timerInch = new Timer();
  private boolean autoBalance = false;
  private boolean limelightmode = false;
  private String controlMode = "Disabled";
  private double batteryVoltage = PowerJNI.getVinVoltage();
  private boolean reachedApriltag = false;
  private boolean deployAutobalance = false;
  // Motors
  private final TalonSRX leftMotor1 = new TalonSRX(1);
  private final TalonSRX leftMotor2 = new TalonSRX(2);
  private final TalonSRX rightMotor1 = new TalonSRX(3);
  private final TalonSRX rightMotor2 = new TalonSRX(4);
  // Claw
  private final TalonSRX armMotor1 = new TalonSRX(5);
  private final Spark leftClawMotor = new Spark(1);
  private final Spark rightClawMotor = new Spark(2);
  // Xbox Controller
  XboxController xcontroller =  new XboxController(0);
  // Customization options
  private final XboxController macroStick = xcontroller; 
  private final boolean debugButtons = false;
  private float turnSpeed = 0.2f;
  private int pitchOffset = 0;
  private double currentAngle;
  private double driveSpeed = 0.7;
  private int pipelineIndex = 0;
  private double tmpDriveSpeed = driveSpeed;
  private int autoCount = 0;
  // Charging location
  private static final String sleft = "Left";
  private static final String smiddle = "Middle";
  private static final String sright = "Right";
  private String s_autoSelected;
  private final SendableChooser<String> ssc = new SendableChooser<>();
  // Autocode selector
  private static final String duy = "Duy";
  private static final String ethan = "Ethan";
  private String a_autoSelected;
  private final SendableChooser<String> ac = new SendableChooser<>();
  // Gyroscope
  private static final AHRS ahrs = new AHRS(Port.kUSB); 

  // Functions/Methods
  @Override
  public void robotInit() {
    // Charging location
    ssc.addOption("Left", sleft);
    ssc.addOption("Middle", smiddle);
    ssc.addOption("Right", sright);
    ssc.setDefaultOption("Middle", smiddle);
    SmartDashboard.putData(ssc);
    // Autocode (for testing)
    ac.addOption("Ethan",ethan);
    ac.addOption("Duy",duy);
    ac.setDefaultOption("Ethan",ethan);
    SmartDashboard.putData(ac);
    // Regular
    timer.reset();
    leftMotor1.configFactoryDefault(); leftMotor1.set(ControlMode.PercentOutput, 0.00);
    leftMotor2.configFactoryDefault(); leftMotor2.set(ControlMode.PercentOutput, 0.00);
    rightMotor1.configFactoryDefault(); rightMotor1.set(ControlMode.PercentOutput, 0.00);
    rightMotor2.configFactoryDefault(); rightMotor2.set(ControlMode.PercentOutput, 0.00);
    armMotor1.configFactoryDefault(); armMotor1.set(ControlMode.PercentOutput, 0.00);
    ahrs.calibrate();
    UsbCamera cam = CameraServer.startAutomaticCapture(0);
    cam.setPixelFormat(PixelFormat.kMJPEG);
    leftMotor1.setInverted(true);
    leftMotor2.setInverted(true);
    SmartDashboard.putString("Autobalance: ",String.valueOf(autoBalance));
    SmartDashboard.putString("Autobalance Job: ", "Not started.");
    SmartDashboard.putString("Control Mode: ",controlMode);
    motorUpdate(0,0,0,0);
    SmartDashboard.putNumber("Arm Output: ",0);
    SmartDashboard.putNumber("Max speed: ",driveSpeed);
    SmartDashboard.putBoolean("Limelight:", limelightmode);
    SmartDashboard.putString("Auto Timer: ", "Not started.");
    clawSpeed(0,0);
  }

  @Override
  public void autonomousInit() {
    a_autoSelected = ac.getSelected();
    autoBalance = false;
    timer.reset();
    timer.start();
    controlMode = "Autonomous";
    reachedApriltag = false;
    deployAutobalance = false;
  }

  @Override
  public void teleopInit() { controlMode = "Teleop"; autoBalance = false; reachedApriltag = false; deployAutobalance = false; }

  @Override
  public void robotPeriodic() {
    currentAngle = ahrs.getYaw();
    batteryVoltage = PowerJNI.getVinVoltage();
    SmartDashboard.putNumber("Battery Voltage: ",batteryVoltage);
    SmartDashboard.putString("Battery Status: ", resolveBatteryStatus());
    SmartDashboard.putString("Autobalance: ",String.valueOf(autoBalance));
    if (ahrs.isConnected()) {
      SmartDashboard.putNumber("Roll: ",ahrs.getRoll());
      SmartDashboard.putNumber("Pitch: ",ahrs.getPitch());
      SmartDashboard.putNumber("Yaw: ",ahrs.getYaw());
      SmartDashboard.putBoolean("Gyro connected: ", true);
      SmartDashboard.putBoolean("Gyro calibrating: ", ahrs.isCalibrating());
    } else {
      SmartDashboard.putNumber("Roll: ",-999);
      SmartDashboard.putNumber("Pitch: ",-999);
      SmartDashboard.putNumber("Yaw: ",-999);
      SmartDashboard.putBoolean("Gyro connected: ", false);
      SmartDashboard.putBoolean("Gyro calibrating: ", false);
    }
    SmartDashboard.putString("Control Mode: ",controlMode);
    tmpDriveSpeed = SmartDashboard.getNumber("Max speed: ", driveSpeed);
  }

  @Override
  public void autonomousPeriodic() {
    int time = (int) timer.get();
    SmartDashboard.putString("Auto Timer: ", String.valueOf(timer.get()));
    if (a_autoSelected == ethan) { // Ethan
      double targetArea = LimelightHelpers.getTA("");
      SmartDashboard.putNumber("LimelightArea", targetArea);
      
      if (targetArea <= 20 && !reachedApriltag) { // 20 is a placeholder, this is probably a dangerous value dont run this unless you ask first
        // Approach the aprilTag 
        drive(0.1); // Approach at 10% speed
        // Here we would either drop the cone or if we push it we ignore this part
        reachedApriltag = true;
      } else if (reachedApriltag && !deployAutobalance) { // Reverse onto charging station
        drive(-0.1);
        if (ahrs.getPitch() >= 2) { // Reached the charging station
          deployAutobalance = true;
        }
      } else if (deployAutobalance) { // Balance on the charging station
        autoBalance = true;
        autoBalancePeriodic();
      }
    } else { // Duy
      if (between(0, 15, time)) {
        driveInch(6);
      }
    }
  }

  @Override
  public void disabledPeriodic() {controlMode = "Disabled";};
  @Override
  public void simulationPeriodic() {controlMode = "Simulation";};

  @Override
  public void teleopPeriodic() {
    if (batteryVoltage <= 8.00) {
      driveSpeed = tmpDriveSpeed/2;
    } else {
      driveSpeed = tmpDriveSpeed;
    }
    if (!autoBalance && !limelightmode) {
      leftMotor1.set(ControlMode.PercentOutput, xcontroller.getLeftY()*driveSpeed);
      leftMotor2.set(ControlMode.PercentOutput, xcontroller.getLeftY()*driveSpeed);
      rightMotor1.set(ControlMode.PercentOutput,xcontroller.getRightY()*driveSpeed);
      rightMotor2.set(ControlMode.PercentOutput,xcontroller.getRightY()*driveSpeed);
      motorUpdate(xcontroller.getLeftY()*driveSpeed,xcontroller.getLeftY()*driveSpeed,xcontroller.getRightY()*driveSpeed,xcontroller.getRightY()*driveSpeed);
      if (xcontroller.getLeftTriggerAxis() >= 0.01) {
        armMotor(xcontroller.getLeftTriggerAxis());
      } else {
        armMotor((xcontroller.getRightTriggerAxis()*-1)/8);
      }
    } else {
      if (autoBalance && !limelightmode) {
        autoBalancePeriodic();
      } else if(!autoBalance && limelightmode) {
        limelightPeriodic();
      }
    }
    // Regular Macros
    for (int i = 1; i < macroStick.getButtonCount(); i++) {
      if (macroStick.getRawButtonPressed(i)) {
        switch(i) {
          case 8:
            driveSpeed = driveSpeed+0.1;
            if (driveSpeed >= 1.0) {
              driveSpeed = 0;
            }
            SmartDashboard.putNumber("Max speed: ",driveSpeed);
            break;
          case 7:
            driveSpeed = driveSpeed-0.1;
            if (driveSpeed <= 0) {
              driveSpeed = 1;
            }
            SmartDashboard.putNumber("Max speed: ",driveSpeed);
            break;
          case 4:
            pipelineIndex++;
            if (pipelineIndex == 3) {
              pipelineIndex = 0;
            }
            LimelightHelpers.setPipelineIndex("", pipelineIndex);
            SmartDashboard.putNumber("Pipeline:", pipelineIndex);
          case 3:
            autoBalance = !autoBalance;
            break;
          case 1:
            limelightmode = !limelightmode;
            SmartDashboard.putBoolean("Limelight:", limelightmode);
            break;
          default:
            if (debugButtons) {
              System.out.println("Button Pressed: "+i);
            }
        }
      }
    }
    // Claw Macros
    if (macroStick.getLeftBumper()) { // Close
      clawSpeed(-0.1,0.1);
    } else if (macroStick.getRightBumper()) { // Open
      clawSpeed(0.1,-0.1);
    } else { // Reset
      clawSpeed(0,0);
    }
  }

  public void limelightPeriodic() {
    double targetXAxis = LimelightHelpers.getTX("");
    double targetYAxis = LimelightHelpers.getTY("");
    double targetArea = LimelightHelpers.getTA("");
    SmartDashboard.putNumber("LimelightX", targetXAxis);
    SmartDashboard.putNumber("LimelightY", targetYAxis);
    SmartDashboard.putNumber("LimelightArea", targetArea);
    double speedNerf = 85;

    if (!between(-2.5,2.5, targetXAxis)) {
      rightMotor1.set(ControlMode.PercentOutput, targetXAxis/speedNerf);
      rightMotor2.set(ControlMode.PercentOutput, targetXAxis/speedNerf);
      leftMotor1.set(ControlMode.PercentOutput, (targetXAxis/speedNerf)*-1);
      leftMotor2.set(ControlMode.PercentOutput, (targetXAxis/speedNerf)*-1);
      motorUpdate(targetXAxis/speedNerf,targetXAxis/speedNerf,(targetXAxis/speedNerf)*-1,(targetXAxis/speedNerf)*-1);
    } else {
      resetMotors();
    }
  }

  public void autoBalancePeriodic() {
    if (ahrs.isConnected() && !ahrs.isCalibrating()) {
      double pitch = ahrs.getPitch();
      if (pitch >= (5+pitchOffset)) {
        SmartDashboard.putString("Autobalance Job: ", "Go backwards");
        drive(-0.25);
      } else if (pitch <= (-5+pitchOffset)) {
        SmartDashboard.putString("Autobalance Job: ", "Go forwards");
        drive(0.25);
      } else {
        SmartDashboard.putString("Autobalance Job: ", "Already balanced");
        resetMotors();
      }
    }
  }

  public void drive(double speed) {
    speed = speed*-1; leftMotor1.set(ControlMode.PercentOutput, speed);
    leftMotor2.set(ControlMode.PercentOutput, speed);
    rightMotor1.set(ControlMode.PercentOutput, speed);
    rightMotor2.set(ControlMode.PercentOutput, speed);
    motorUpdate(speed,speed,speed,speed);
  }

  //The function runs 1 inch of distance for every 1 second passed. 
  //Example:"if you want to make it travel 12 inches in 2 seconds
  //you would have to make it 6 inches for the number being sent to the function as it 
  //will now travel for 6 inches per second adding up to 12 inches traveled."
  //This only works depending on the weight of the robot
  public void driveInch(double inch) {
    double oneInch = inch/147.5; // Duy made this
    timerInch.start();
    int timeInch = (int) timerInch.get();
    if (between(0,2,timeInch)) {
      drive(oneInch);
    } else {
      resetMotors();
      System.out.println("working");
    }
  }    

  public void gyro_rotate(double angle) {
    if (!between(angle-1,angle+1,currentAngle)) {
      if (angle >= 1) {
        rightMotor1.set(ControlMode.PercentOutput, turnSpeed);
        rightMotor2.set(ControlMode.PercentOutput, turnSpeed);
        leftMotor1.set(ControlMode.PercentOutput, turnSpeed*-1);
        leftMotor2.set(ControlMode.PercentOutput, turnSpeed*-1);
        motorUpdate(turnSpeed*-1,turnSpeed*-1,turnSpeed,turnSpeed);
      } else {
        leftMotor1.set(ControlMode.PercentOutput, turnSpeed);
        leftMotor2.set(ControlMode.PercentOutput, turnSpeed);
        rightMotor1.set(ControlMode.PercentOutput, turnSpeed*-1);
        rightMotor2.set(ControlMode.PercentOutput, turnSpeed*-1);
        motorUpdate(turnSpeed,turnSpeed,turnSpeed*-1,turnSpeed*-1);
      }
    }
  }

  public void rotate(double speed) {
    speed = speed*-1; leftMotor1.set(ControlMode.PercentOutput, speed);
    leftMotor2.set(ControlMode.PercentOutput, speed);
    rightMotor1.set(ControlMode.PercentOutput, speed*-1);
    rightMotor2.set(ControlMode.PercentOutput, speed*-1);
    motorUpdate(speed,speed,speed*-1,speed*-1);
  }

  public void resetMotors() {
    leftMotor1.set(ControlMode.PercentOutput, 0);
    leftMotor2.set(ControlMode.PercentOutput, 0);
    rightMotor1.set(ControlMode.PercentOutput, 0);
    rightMotor2.set(ControlMode.PercentOutput, 0);
    motorUpdate(0,0,0,0);
  }

  public boolean between(double start, double end, double time) {
    if (start <= time && end >= time) {
      return true;
    } else {
      return false;
    }
  }

  public String resolveBatteryStatus() {
    if (batteryVoltage <= 8.00) {
      return "Bad";
    } else {
      return "Ok";
    }
  }

  public void motorUpdate(double l1, double l2, double r1, double r2) {
    SmartDashboard.putNumber("Left Motor1 Output: ",l1);
    SmartDashboard.putNumber("Left Motor2 Output: ",l2);
    SmartDashboard.putNumber("Right Motor1 Output: ",r1);
    SmartDashboard.putNumber("Right Motor2 Output: ",r2);
  }

  public void armMotor(double power) {
    armMotor1.set(ControlMode.PercentOutput,power);
    SmartDashboard.putNumber("Arm Output: ",power);
  }
  
  public void clawMotor(double lspeed, double rspeed) {
    leftClawMotor.set(lspeed);
    rightClawMotor.set(rspeed);
    SmartDashboard.putNumber("Left Claw Output: ",lspeed);
    SmartDashboard.putNumber("Right Claw Output: ",rspeed); 
  }
}
