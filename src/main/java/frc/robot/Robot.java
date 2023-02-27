// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.SerialPort.Port;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.cameraserver.CameraServer;
import frc.robot.LimelightHelpers;
import com.kauailabs.navx.frc.AHRS;

public class Robot extends TimedRobot {
  // Autonomous & Teleop vars
  private final Timer timer = new Timer(); 
  private boolean autoBalance = false;
  // Motors
  private final TalonSRX leftMotor1 = new TalonSRX(1); 
  private final TalonSRX rightMotor1 = new TalonSRX(3);
  private final TalonSRX leftMotor2 = new TalonSRX(2);
  private final TalonSRX rightMotor2 = new TalonSRX(4);
  // Claw
  private final TalonSRX armMotor1 = new TalonSRX(5);
  // Xbox Controller
  XboxController xcontroller =  new XboxController(0);
  // Customization options
  private final XboxController macroStick = xcontroller; 
  private final boolean debugButtons = true; // When a button is pressed we print out the buttons id, for easy debugging
  private float turnSpeed = 0.2f;
  private int pitchOffset = 0;
  private double currentAngle;
  private int upRateLimit = 6;
  private String controlMode = "Disabled";
  private int limelightprofile = 0;
  private final int maxlimelightprofiles = 2;
  private double l1 = 0;
  private double l2 = 0;
  private double r1 = 0;
  private double r2 = 0;
  // Gyroscope
  private static final AHRS ahrs = new AHRS(Port.kUSB); 

  // Functions/Methods
  @Override
  public void robotInit() {
    timer.reset();
    leftMotor1.configFactoryDefault(); leftMotor1.set(ControlMode.PercentOutput, 0.00);
    leftMotor2.configFactoryDefault(); leftMotor2.set(ControlMode.PercentOutput, 0.00);
    rightMotor1.configFactoryDefault(); rightMotor1.set(ControlMode.PercentOutput, 0.00);
    rightMotor2.configFactoryDefault(); rightMotor2.set(ControlMode.PercentOutput, 0.00);
    armMotor1.configFactoryDefault(); armMotor1.set(ControlMode.PercentOutput, 0.00);
    ahrs.calibrate();
    CameraServer.startAutomaticCapture(0);
    leftMotor1.setInverted(true);
    leftMotor2.setInverted(true);
    SmartDashboard.putString("Autobalance: ",String.valueOf(autoBalance));
    SmartDashboard.putString("Control Mode: ",controlMode);
    SmartDashboard.putNumber("Left Motor1 Output: ",0);
    SmartDashboard.putNumber("Left Motor2 Output: ",0);
    SmartDashboard.putNumber("Right Motor1 Output: ",0);
    SmartDashboard.putNumber("Right Motor2 Output: ",0);
    SmartDashboard.putNumber("Arm Output: ",0);
    SmartDashboard.putNumber("Pipeline: ", limelightprofile);
    SmartDashboard.putNumber("Limelight X: ", -999);
    SmartDashboard.putNumber("Limelight Y: ", -999);
  }

  @Override
  public void autonomousInit() {
    System.out.println("Autonomous Time!");
    timer.reset();
    timer.start();
    controlMode = "Autonomous";
  }

  @Override
  public void teleopInit() { controlMode = "Teleop";}

  @Override
  public void robotPeriodic() {
    currentAngle = ahrs.getYaw();
    SmartDashboard.putString("Autobalance: ",String.valueOf(autoBalance));
    if (ahrs.isConnected()) {
      SmartDashboard.putNumber("Roll: ",ahrs.getRoll());
      SmartDashboard.putNumber("Pitch: ",ahrs.getPitch());
      SmartDashboard.putNumber("Yaw: ",ahrs.getYaw());
      SmartDashboard.putBoolean("Gyro connected: ", true);
    } else {
      SmartDashboard.putNumber("Roll: ",-999);
      SmartDashboard.putNumber("Pitch: ",-999);
      SmartDashboard.putNumber("Yaw: ",-999);
      SmartDashboard.putBoolean("Gyro connected: ", false);
    }
    SmartDashboard.putString("Control Mode: ",controlMode);
    SmartDashboard.putNumber("Up speed rate limit: ", Integer.valueOf(upRateLimit));
    SmartDashboard.putNumber("Pipeline: ", limelightprofile);
    double targetXAxis = LimelightHelpers.getTX("");
    double targetYAxis = LimelightHelpers.getTY("");    
    SmartDashboard.putNumber("Limelight X: ", targetXAxis);
    SmartDashboard.putNumber("Limelight Y: ", targetYAxis);
    motorOutput();
  }

  @Override
  public void autonomousPeriodic() {
    int time = (int) timer.get();
    if (between(0,2,time)) {
      drive(0.2);
    } else if (between(3, 4,time)) {
      drive(-0.2);
    } else if (between(5, 6, time)) {
      rotate(-90);
    } else if (between(7, 8, time)) {
      rotate(0);
    } else {
      resetMotors();
    }
  }

  @Override
  public void disabledPeriodic() {controlMode = "Disabled";};
  @Override
  public void simulationPeriodic() {};

  @Override
  public void teleopPeriodic() {
    // Limelight -- Temp
    double targetXAxis = LimelightHelpers.getTX("");
    System.out.println(targetXAxis);
    if (targetXAxis != 0) {
      if (targetXAxis > -2) {
        rightMotor1.set(ControlMode.PercentOutput, 0.2);
        rightMotor2.set(ControlMode.PercentOutput, 0.2);
        leftMotor1.set(ControlMode.PercentOutput, -0.2);
        leftMotor2.set(ControlMode.PercentOutput, -0.2);
      } else if (targetXAxis < 2) {
        leftMotor1.set(ControlMode.PercentOutput, 0.2);
        leftMotor2.set(ControlMode.PercentOutput, 0.2);
        rightMotor1.set(ControlMode.PercentOutput, -0.2);
        rightMotor2.set(ControlMode.PercentOutput, -0.2);
      }
    }

    // if (!autoBalance) {
    //   leftMotor1.set(ControlMode.PercentOutput, xcontroller.getLeftY()/3); l1 = xcontroller.getLeftY()/3;
    //   leftMotor2.set(ControlMode.PercentOutput, xcontroller.getLeftY()/3); l2 = xcontroller.getLeftY()/3;
    //   rightMotor1.set(ControlMode.PercentOutput,xcontroller.getRightY()/3); r1 = xcontroller.getRightY()/3;
    //   rightMotor2.set(ControlMode.PercentOutput,xcontroller.getRightY()/3); r2 = xcontroller.getRightY()/3;
    //   if (xcontroller.getLeftTriggerAxis() >= 0.01) {
    //     armMotor(xcontroller.getLeftTriggerAxis()/upRateLimit);
    //   } else {
    //     armMotor(xcontroller.getRightTriggerAxis()/12*-1);
    //   }
    // } else {
    //   autoBalancePeriodic();
    // }
    // for (int i = 1; i < macroStick.getButtonCount(); i++) {
    //   if (macroStick.getRawButtonPressed(i)) {
    //     switch(i) {
    //       case 3:
    //         autoBalance = !autoBalance;
    //         System.out.println("Auto Balance: "+autoBalance);
    //         break;
    //       case 2:
    //         if (upRateLimit == 1) {
    //           upRateLimit = 6;
    //         } else {
    //           upRateLimit = 1;
    //         }
    //         System.out.println("Up speed rate limit: "+upRateLimit);
    //         break;
    //       case 4:
    //         if (limelightprofile == maxlimelightprofiles) {
    //           limelightprofile = 0;
    //         } else {
    //           limelightprofile++;
    //         }
    //         System.out.println("Limelight mode: "+limelightprofile);
    //         LimelightHelpers.setPipelineIndex("", limelightprofile);
    //         break;
    //       default:
    //         if (debugButtons) {
    //           System.out.println("Button Pressed: "+i);
    //         }
    //     }
    //   }
    // }
  }

  public void autoBalancePeriodic() {
    if (!ahrs.isConnected()) {
      System.out.println("The Gyro not connected or cannot be read.");
    } else if (ahrs.isCalibrating()) {
      System.out.println("The Gryo is calibrating..");
    } else {
      double pitch = ahrs.getPitch();
      if (pitch >= (5+pitchOffset)) {
        System.out.println("Go backwards");
        drive(-0.25);
      } else if (pitch <= (-5+pitchOffset)) {
        System.out.println("Go forwards");
        drive(0.25);
      } else {
        System.out.println("Already balanced");
        resetMotors();
      }
    }
  }

  public void drive(double speed) {
    leftMotor1.set(ControlMode.PercentOutput, speed); l1 = speed;
    leftMotor2.set(ControlMode.PercentOutput, speed); l2 = speed;
    rightMotor1.set(ControlMode.PercentOutput, speed); r1 = speed;
    rightMotor2.set(ControlMode.PercentOutput, speed); r2 = speed;
  }

  public void rotate(double angle) {
    if (!between(angle-1,angle+1,currentAngle)) {
      if (angle >= 1) {
        rightMotor1.set(ControlMode.PercentOutput, turnSpeed); r1 = turnSpeed;
        rightMotor2.set(ControlMode.PercentOutput, turnSpeed); r2 = turnSpeed;
      } else {
        leftMotor1.set(ControlMode.PercentOutput, turnSpeed); l1 = turnSpeed;
        leftMotor2.set(ControlMode.PercentOutput, turnSpeed); l2 = turnSpeed;
      }
    }
  }

  public void resetMotors() {
    leftMotor1.set(ControlMode.PercentOutput, 0); l1 = 0;
    leftMotor2.set(ControlMode.PercentOutput, 0); l2 = 0;
    rightMotor1.set(ControlMode.PercentOutput, 0); r1 = 0;
    rightMotor2.set(ControlMode.PercentOutput, 0); r2 = 0;
  }

  public boolean between(double start, double end, double time) {
    if (start <= time && end >= time) {
      return true;
    } else {
      return false;
    }
  }

  public void armMotor(double power) {
    armMotor1.set(ControlMode.PercentOutput,power);
    SmartDashboard.putNumber("Arm Output: ",power);
  }

  public void motorOutput() {
    SmartDashboard.putNumber("Left Motor1 Output: ",l1);
    SmartDashboard.putNumber("Left Motor2 Output: ",l2);
    SmartDashboard.putNumber("Right Motor1 Output: ",r1);
    SmartDashboard.putNumber("Right Motor2 Output: ",r2);
  }
}