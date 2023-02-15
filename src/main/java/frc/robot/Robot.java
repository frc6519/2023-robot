// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.SerialPort.Port;
import edu.wpi.first.cameraserver.CameraServer;

import com.kauailabs.navx.frc.AHRS;

public class Robot extends TimedRobot {

  // Do you have a keyboard connected?
    private static final boolean usingKeyboard = true;

  // Declares default variables & device ports.

  // Default (Auto-Generated)
    private final Timer timer = new Timer();
    private boolean teleopStatus = false;  
    private boolean autoBalance = false;
  // Drivetrain
    private final TalonSRX leftMotor1 = new TalonSRX(1); 
    private final TalonSRX rightMotor1 = new TalonSRX(2);
    private final TalonSRX leftMotor2 = new TalonSRX(3);
    private final TalonSRX rightMotor2 = new TalonSRX(4);
  // Claw
    private final TalonSRX armMotor1 = new TalonSRX(5);
  // Xbox
    XboxController xcontroller =  new XboxController(0); 
  // Keyboard pretending to be a joystick
    private final Joystick keyboard = new Joystick(1);
  // Customization options
    // joystick1 or joystick2 or xcontroller (Which joystick listens for macros); Don't forget to change variable type
    private final XboxController macroStick = xcontroller; 
    private final boolean debugButtons = false; // When a button is pressed we print out the buttons id, for easy debugging
    private boolean macrosEnabled = true;
    private float turnSpeed = 0.2f;
    private double armPosition = 0;
    private int pitchOffset = 0;
  // Gyroscope
    private static final AHRS ahrs = new AHRS(Port.kUSB); 

  // Functions/Methods
      @Override
      public void robotInit() {
        timer.reset();
        // Initialize motor variables
        leftMotor1.configFactoryDefault(); leftMotor1.set(ControlMode.PercentOutput, 0.00);
        leftMotor2.configFactoryDefault(); leftMotor2.set(ControlMode.PercentOutput, 0.00);
        rightMotor1.configFactoryDefault(); rightMotor1.set(ControlMode.PercentOutput, 0.00);
        rightMotor2.configFactoryDefault(); rightMotor2.set(ControlMode.PercentOutput, 0.00);
        armMotor1.configFactoryDefault(); armMotor1.set(ControlMode.PercentOutput, 0.00);

        ahrs.calibrate();

        /*
         * (On roborio)
         * 0 - Microsoft Camera
         * 1 - Limelight (Not present yet)
         */ 
        CameraServer.startAutomaticCapture(0);
      }

        @Override
        public void autonomousInit() {
          System.out.println("Autonomous Time!");
          macrosEnabled = false;
          timer.reset();
          timer.start();
          teleopStatus = false;
        }

        @Override
        public void teleopInit() {
          macrosEnabled = true;
          teleopStatus = true;
        }

        @Override
        public void robotPeriodic() {}

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
            rotate(90);
          } else {
            resetMotors();
          }
        }


        @Override
        public void teleopPeriodic() {
          if (macrosEnabled && teleopStatus) {
            int num;
            num = keyboard.getPOV();
            switch(num) {
              case 225: // Num 1
                armMotors(0.05);
                break;
              case 180: // Num 2
                if ((armPosition - 0.005) >= 0) {
                  armPosition = (armPosition - 0.005);
                } else {
                  armPosition = 0;
                }
                armMotors(armPosition);
                break;
              case 135: // Num 3
                armMotors(0.05);
                break;
              case 270: // Num 4
                armMotors(0.10);
                break;
              case 90: // Num 6
                armMotors(0.10);
                break;
              case 315: // Num 7
                armMotors(0.15);
                break;
              case 0: // Num 8
                if ((armPosition + 0.005) <= 0.15) {
                  armPosition = (armPosition + 0.005);
                } else {
                  armPosition = 0.15;
                }
                armMotors(armPosition);
                break;
              case 45: // Num 9
                armMotors(0.15);
                break;
              default:
                if (debugButtons) {
                  System.out.println("Button Pressed: "+num);
                }
                break;
            }
          }
          if (!autoBalance) {
            leftMotor1.set(ControlMode.PercentOutput, xcontroller.getLeftY()/3);
            leftMotor2.set(ControlMode.PercentOutput, xcontroller.getLeftY()/3);
            rightMotor1.set(ControlMode.PercentOutput,xcontroller.getRightY()/3);
            rightMotor2.set(ControlMode.PercentOutput,xcontroller.getRightY()/3);
            if (!usingKeyboard) {
              armMotor1.set(ControlMode.PercentOutput,xcontroller.getLeftTriggerAxis()/3);
              armPosition = (xcontroller.getLeftTriggerAxis()/3);
            }
          } else {
            if (teleopStatus && autoBalance) {
              autoBalancePeriodic();
            }
          }
          for (int i = 0; i < macroStick.getButtonCount(); i++) {
            if (macroStick.getRawButtonPressed(i)) {
              switch(i) {
                case 3:
                  autoBalance = !autoBalance;
                  System.out.println("Auto Balance: "+autoBalance);
                  break;
                default:
                  if (debugButtons) {
                    System.out.println("Button Pressed: "+i);
                  }
              }
            }
          }
        }

        public void autoBalancePeriodic() {
          if (ahrs.isCalibrating()) {
            System.out.println("The Gryo is calibrating..");
          } else if (!ahrs.isConnected()) {
            System.out.println("The Gyro not connected or cannot be read.");
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
          leftMotor1.set(ControlMode.PercentOutput, speed);
          leftMotor2.set(ControlMode.PercentOutput, speed);
          rightMotor1.set(ControlMode.PercentOutput, speed);
          rightMotor2.set(ControlMode.PercentOutput, speed);
        }

        public void rotate(double angle) {
          double currentAngle = ahrs.getYaw();
          if (!between(angle-1,angle+1,currentAngle)) {
            if (angle >= 1) { // Left
              rightMotor1.set(ControlMode.PercentOutput, turnSpeed);
              rightMotor2.set(ControlMode.PercentOutput, turnSpeed);
            } else { // Right
              leftMotor1.set(ControlMode.PercentOutput, turnSpeed);
              leftMotor2.set(ControlMode.PercentOutput, turnSpeed);
            }
          }
        }

        public void resetMotors() {
          leftMotor1.set(ControlMode.PercentOutput, 0);
          leftMotor2.set(ControlMode.PercentOutput, 0);
          rightMotor1.set(ControlMode.PercentOutput, 0);
          rightMotor2.set(ControlMode.PercentOutput, 0);
        }

        public boolean between(double start, double end, double time) {
          if (start <= time && end >= time) {
            return true;
          } else {
            return false;
          }
        }

        public void armMotors(double power) {
          armMotor1.set(ControlMode.Position,power);
          armPosition = power;
        }
      }