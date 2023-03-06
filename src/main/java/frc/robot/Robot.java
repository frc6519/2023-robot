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
import edu.wpi.first.wpilibj.motorcontrol.Spark;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.UsbCamera;
import edu.wpi.first.cscore.VideoMode;
// import frc.robot.LimelightHelpers;
// import frc.robot.LimelightHelpers.LimelightTarget_Detector;
import edu.wpi.first.cscore.VideoMode.PixelFormat;

import com.kauailabs.navx.frc.AHRS;

public class Robot extends TimedRobot {
  // Autonomous & Teleop vars
  private final Timer timer = new Timer();
  private boolean autoBalance = false;
  private boolean limelightmode = false;
  private String controlMode = "Disabled";
  private double batteryVoltage = -1;
  // Motors
  private final TalonSRX leftMotor1 = new TalonSRX(1); // Confirm these ids later
  private final TalonSRX leftMotor2 = new TalonSRX(2);
  private final TalonSRX rightMotor1 = new TalonSRX(3);
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
  private double driveSpeed = 0.30;
  private double tmpDriveSpeed = driveSpeed;
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
    UsbCamera cam = CameraServer.startAutomaticCapture(0);
    cam.setPixelFormat(PixelFormat.kMJPEG);
    leftMotor1.setInverted(true);
    leftMotor2.setInverted(true);
    SmartDashboard.putString("Autobalance: ",String.valueOf(autoBalance));
    SmartDashboard.putString("Control Mode: ",controlMode);
    motorUpdate(0,0,0,0);
    SmartDashboard.putNumber("Arm Output: ",0);
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
    batteryVoltage = DriverStation.getInstance().getBatteryVoltage();
    SmartDashboard.putNumber("Battery Voltage: ",batteryVoltage);
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
  }

  @Override
  public void autonomousPeriodic() {
    int time = (int) timer.get();
    if (between(0,2,time)) {
      drive(0.2);
    } else if (between(3, 4,time)) {
      drive(-0.2);
    } else if (between(5, 6, time)) {
      gyro_rotate(-90);
    } else if (between(7, 8, time)) {
      gyro_rotate(0);
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
    if (batteryVoltage <= 8.00) {
      System.out.println("Should stop the robot, or reduce speed");
      driveSpeed = tmpSpeed/2;
    } else {
      System.out.println("Battery usage is fine"); 
      driveSpeed = tmpSpeed;
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
        armMotor(xcontroller.getRightTriggerAxis()/12*-1);
      }
    } else {
      if (autoBalance && !limelightmode) {
        autoBalancePeriodic();
      } else if(!autoBalance && limelightmode) {
        limelightPeriodic()
      }
    }
    for (int i = 1; i < macroStick.getButtonCount(); i++) {
      if (macroStick.getRawButtonPressed(i)) {
        switch(i) {
          case 3:
            autoBalance = !autoBalance;
            break;
          case 1:
            limelightmode = !limelightmode;
            break;
          default:
            if (debugButtons) {
              System.out.println("Button Pressed: "+i);
            }
        }
      }
    }
  }

  public void limelightPeriodic() {
    double targetXAxis = LimelightHelpers.getTX("");
    double targetYAxis = LimelightHelpers.getTY("");
    double targetArea = LimelightHelpers.getTA("");
    double speedNerf = 100;

    if (!between(-2.5,2.5, targetXAxis)) {
      leftMotor1.set(ControlMode.PercentOutput, targetXAxis/speedNerf);
      leftMotor2.set(ControlMode.PercentOutput, targetXAxis/speedNerf);
      rightMotor1.set(ControlMode.PercentOutput, (targetXAxis/speedNerf)*-1);
      rightMotor2.set(ControlMode.PercentOutput, (targetXAxis/speedNerf)*-1);
      motorUpdate(targetXAxis/speedNerf,targetXAxis/speedNerf,(targetXAxis/speedNerf)*-1,(targetXAxis/speedNerf)*-1);
    } else {
      resetMotors();
    }

    SmartDashboard.putNumber("LimelightX", targetXAxis);
    SmartDashboard.putNumber("LimelightY", targetYAxis);
    SmartDashboard.putNumber("LimelightArea", targetArea);

    System.out.println(targetXAxis+'\n'+targetYAxis);
  }

  public void autoBalancePeriodic() {
    if (ahrs.isConnected() && !ahrs.isCalibrating()) {
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
    leftMotor1.set(ControlMode.PercentOutput, speed);
    leftMotor2.set(ControlMode.PercentOutput, speed);
    rightMotor1.set(ControlMode.PercentOutput, speed);
    rightMotor2.set(ControlMode.PercentOutput, speed);
    motorUpdate(speed,speed,speed,speed);
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
    leftMotor1.set(ControlMode.PercentOutput, speed);
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
}
