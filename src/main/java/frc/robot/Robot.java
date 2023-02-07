// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.SerialPort.Port;
import edu.wpi.first.wpilibj.PS4Controller;
import edu.wpi.first.cameraserver.CameraServer;
// import edu.wpi.first.cscore.UsbCamera;

import com.kauailabs.navx.frc.AHRS;

public class Robot extends TimedRobot {
  // Toggle between Joystick and Xbox controls.
    private static final boolean XboxMode = true;
    private static final boolean PS4Mode = false;

  // Toggles between Comp. Bot & Test Bot.
    private static final boolean CompetitionBot = false;

  // Do you have a keyboard connected?
    private static final boolean usingKeyboard = false;

  // Declares default variables & device ports.

    // Default (Auto-Generated)
      private final Timer timer = new Timer();
      private static final String kDefaultAuto = "Default";
      private static final String kCustomAuto = "My Auto";
      private String m_autoSelected;
      private static final String cPOV = "POV Camera";
      private static final String cLimelight = "Limelight"; // <--  Placeholder
      private String c_autoSelected;
      private final SendableChooser<String> m_chooser = new SendableChooser<>();
      private final SendableChooser<String> c_chooser = new SendableChooser<>();
      private boolean teleopStatus = false;  
      private boolean autoBalance = false;

    // Drivetrain
      private final TalonSRX leftMotor1 = new TalonSRX(0); 
      private final TalonSRX rightMotor1 = new TalonSRX(1);
      private final TalonSRX leftMotor2 = new TalonSRX(2);
      private final TalonSRX rightMotor2 = new TalonSRX(3);

    // Claw
      private final TalonSRX armMotor1 = new TalonSRX(4);

    // Joystick
      private final Joystick joystick1 = new Joystick(0); 
      private final Joystick joystick2 = new Joystick(1);
      // PlayStation
        PS4Controller pcontroller = new PS4Controller(0);
      // Xbox
        XboxController xcontroller =  new XboxController(0); 
      // Keyboard pretending to be a joystick
        private final Joystick keyboard = new Joystick(2);
      // Customization options
        // joystick1 or joystick2 or xcontroller (Which joystick listens for macros); Don't forget to change variable type
        private final XboxController macroStick = xcontroller; 
        private final boolean debugButtons = false; // When a button is pressed we print out the buttons id, for easy debugging
        private boolean macrosEnabled = true;

    // Accelerometer
      Accelerometer accelerometer = new BuiltInAccelerometer(); 
      double prevXAccel = 0;
      double prevYAccel = 0;
      double xAccel = 0;
      double yAccel = 0;

    // Cameras - Network tables & Smart dashboard needed & Limelight needed eventually & I probably did this wrong (Ignore i'll fix it myself)
      // UsbCamera POV = new UsbCamera("POV", "dev/video0");
      // UsbCamera Limelight = new UsbCamera("Limelight", "dev/video1");

    // Gyroscope
      // private final AHRS ahrs = new AHRS();
      private static final AHRS ahrs = new AHRS(Port.kUSB); 

  // Functions/Methods
  // (Hover mouse over functions for their definitions.)

    // Initialization - Code that starts (initializes) under certain conditions.

      @Override
      public void robotInit() {
        timer.reset();
        m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
        m_chooser.addOption("Custom Auto", kCustomAuto);
        c_chooser.setDefaultOption("POV Camera", cPOV);
        c_chooser.addOption("Limelight", cLimelight);
        SmartDashboard.putData("Camera Options",c_chooser);
        SmartDashboard.putData("Auto choices", m_chooser);
        // Initialize motor variables
        leftMotor1.configFactoryDefault(); leftMotor1.set(ControlMode.PercentOutput, 0.00);
        // leftMotor2.configFactoryDefault(); leftMotor2.set(ControlMode.PercentOutput, 0.00);
        rightMotor1.configFactoryDefault(); rightMotor1.set(ControlMode.PercentOutput, 0.00);
        // rightMotor2.configFactoryDefault(); rightMotor2.set(ControlMode.PercentOutput, 0.00);
        armMotor1.configFactoryDefault(); armMotor1.set(ControlMode.PercentOutput, 0.00);

        if (!CompetitionBot) {
          leftMotor1.setInverted(true);
        }

        ahrs.calibrate();

        /*
         * Note for camera (On HP laptop):
         * 0 - Internal Camera
         * 1 - External Camera
         * 
         * (On roborio)
         * 0 - Microsoft Camera
         * 1 - Limelight (Not present yet)
         */ 
        CameraServer.startAutomaticCapture(0);
        CameraServer.startAutomaticCapture(1);
      }

      /**
       * This autonomous (along with the chooser code above) shows how to select between different
       * autonomous modes using the dashboard. The sendable chooser code works with the Java
       * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
       * uncomment the getString line to get the auto name from the text box below the Gyro
       *
       * <p>You can add additional auto modes by adding additional comparisons to the switch structure
       * below with additional strings. If using the SendableChooser make sure to add them to the
       * chooser code above as well.
       */
        @Override
        public void autonomousInit() {
          System.out.println("Autonomous Time!");
          m_autoSelected = m_chooser.getSelected();
          c_autoSelected = m_chooser.getSelected();
          // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
          System.out.println("Auto selected: " + m_autoSelected);
          macrosEnabled = false;
          timer.reset();
          timer.start();
          teleopStatus = false;
        }

      /** 
       * This function is called once when teleop is enabled. -- Important*/ 
        @Override
        public void teleopInit() {
          macrosEnabled = false;
          teleopStatus = true;
        }

      /** 
       * This function is called once when the robot is disabled. */
        @Override
        public void disabledInit() {}

      /** 
       * This function is called once when the robot is first started up. */
        @Override
        public void simulationInit() {}

      /** 
       * This function is called once when test mode is enabled. */
        @Override
        public void testInit() {}

    // Periodic - Code that runs constantly under certain conditions.

      /**
       * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
       * that you want ran during disabled, autonomous, teleoperated and test.
       *
       * <p>This runs after the mode specific periodic functions, but before LiveWindow and
       * SmartDashboard integrated updating.
       */
        @Override
        public void robotPeriodic() {
          // Accelorometer
          double xAccel = accelerometer.getX();
          double yAccel = accelerometer.getY();
          prevXAccel = xAccel;
          prevYAccel = yAccel;

          // Camera
          switch (c_autoSelected) {
            case cLimelight:
              // CameraServer.getServer().setSource(Limelight);
              break;
            case cPOV:
            default:
              // CameraServer.getServer().setSource(POV);
              break;
          }
        }
      /** 
       * This function is called periodically during autonomous. -- Important*/
        @Override
        public void autonomousPeriodic() {
          int time = (int) timer.get();
          switch (m_autoSelected) {
            case kCustomAuto:
              // Put custom auto code here
              break;
            case kDefaultAuto:
            default:
              // Put default auto code here
              if (time <= 14) { // Total 15s
                System.out.println(time);
              }
              break;
          }
        }

      /** 
       * This function is called periodically during operator control. -- Important*/
        @Override
        public void teleopPeriodic() {
          if (macrosEnabled && teleopStatus) {
            // int num = keyboard.getPOV();
            int num;
            if (usingKeyboard) {
              num = keyboard.getPOV();
            } else {
              num = 0;
            }
            switch(num) {
              case 225: // Num 1
                System.out.println("1");
                break;
              case 180: // Num 2
                System.out.println("2");
                break;
              case 135: // Num 3
                System.out.println("3");
                break;
              case 270: // Num 4
                System.out.println("4");
                break;
              case 90: // Num 6
                System.out.println("6");
                break;
              case 315: // Num 7
                System.out.println("7");
                break;
              case 0: // Num 8
                System.out.println("8");
                break;
              case 45: // Num 9
                System.out.println("9");
                break;
              default:
                if (debugButtons) {
                  System.out.println("Button Pressed: "+num);
                }
                break;
            }
          }
          if (!autoBalance) { // Autobalance is disabled
            if (XboxMode) { // Xbox
              if (!CompetitionBot) {
                leftMotor1.set(ControlMode.PercentOutput, xcontroller.getLeftY()/3);
                rightMotor1.set(ControlMode.PercentOutput,xcontroller.getRightY()/3);
              } else {
                leftMotor1.set(ControlMode.PercentOutput, xcontroller.getLeftY()/3);
                leftMotor2.set(ControlMode.PercentOutput, xcontroller.getLeftY()/3);
                rightMotor1.set(ControlMode.PercentOutput,xcontroller.getRightY()/3);
                rightMotor2.set(ControlMode.PercentOutput,xcontroller.getRightY()/3);
              }
            } else if (PS4Mode) { // PS4
              if (!CompetitionBot) {
                leftMotor1.set(ControlMode.PercentOutput, pcontroller.getLeftY()/3);
                rightMotor1.set(ControlMode.PercentOutput,pcontroller.getRightY()/3);
              } else {
                leftMotor1.set(ControlMode.PercentOutput, pcontroller.getLeftY()/3);
                leftMotor2.set(ControlMode.PercentOutput, pcontroller.getLeftY()/3);
                rightMotor1.set(ControlMode.PercentOutput,pcontroller.getRightY()/3);
                rightMotor2.set(ControlMode.PercentOutput,pcontroller.getRightY()/3);
              }
            } else { // Joystick
              if(!CompetitionBot) {
                leftMotor1.set(ControlMode.PercentOutput, joystick1.getY()/3);
                rightMotor1.set(ControlMode.PercentOutput, joystick2.getY()/3);
              } else {
                leftMotor1.set(ControlMode.PercentOutput,  joystick1.getY()/3);
                leftMotor2.set(ControlMode.PercentOutput,  joystick1.getY()/3);
                rightMotor1.set(ControlMode.PercentOutput, joystick2.getY()/3);
                rightMotor2.set(ControlMode.PercentOutput, joystick2.getY()/3);
              }
            }
          } else {
            if (teleopStatus && autoBalance) {
              // System.out.println("Should be auto balancing!");
              /*
              * Auto balance goes in here, could be a function or just have the full code in here
              * autoBalance is automagically toggled when a button is pressed on the controller, you don't need to worry about it
              * you will have to convert their mecanum drive to our direct motors
              * 
              * Example at: https://gist.githubusercontent.com/kauailabs/163e909a85819c49512f/raw/e1589a2c170f041e0294b72f04c7635b91b2995c/AutoBalanceRobot.java
              */
              // ahrs.calibrate();
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

      /** 
       * This function is called periodically when disabled. */
        @Override
        public void disabledPeriodic() {}

      /** 
       * This function is called periodically during test mode. */
        @Override
        public void testPeriodic() {}

      /** 
       * This function is called periodically whilst in simulation. */
        @Override
        public void simulationPeriodic() {}

        public void autoBalancePeriodic() {
          if (ahrs.isCalibrating()) {
            System.out.println("Calibrating..");
          } else if (!ahrs.isConnected()) {
            System.out.println("Gyro not connected!");
          } else {
            double pitch = ahrs.getPitch();
            double roll = ahrs.getRoll();
            double yaw = ahrs.getYaw();
            System.out.println("autoBalancePeriodic: "+pitch+roll+yaw); // Debug - Leave here
            /*
            * The values for motor speed and their +- may need to be changed because I don't know which
            * way counts as a positive pitch and a negative pitch, but they will be adjusted as needed
            */
            if (pitch >= 7) {
              // Drive backwards
              System.out.println("Go backwards");
              if (!CompetitionBot) {
                leftMotor1.set(ControlMode.PercentOutput, -0.25); // Weird values because one motor is put on backwards
                rightMotor1.set(ControlMode.PercentOutput, -0.25);
              }
            } else if (pitch <= -3) {
              // Drive forwards
              System.out.println("Go forwards");
              if (!CompetitionBot) {
                leftMotor1.set(ControlMode.PercentOutput, 0.25);
                rightMotor1.set(ControlMode.PercentOutput, 0.25);
              }
            } else {
              System.out.println("Already balanced"); // Debug - Leave here
              leftMotor1.set(ControlMode.PercentOutput, 0.00);
              rightMotor1.set(ControlMode.PercentOutput, 0.00);
              /*
              * By not setting autoBalance to false we keep this periodic going until the
              * driver decides to stop balancing, by doing this we ensure that if it gets
              * tipped by an external force that the robot will readjust itself without
              * the driver having to restart the auto balance
              */
            }
          }
        }
      }