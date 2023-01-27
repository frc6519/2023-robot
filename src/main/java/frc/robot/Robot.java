// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.Joystick;

// Xbox controlls because
import edu.wpi.first.wpilibj.XboxController;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;

public class Robot extends TimedRobot {
  private final Timer timer = new Timer();
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private static final String keyboardStyle = "keyboardStyle";
  private static final String tankStyle = "tankStyle";
  private String m_autoSelected;
  private static final boolean XboxMode = true;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  private final SendableChooser<String> ct_chooser = new SendableChooser<>();

  // Ignore ports, I’m using last year’s as place holders.
  private final TalonSRX leftMotor1 = new TalonSRX(0);
  private final TalonSRX rightMotor1 = new TalonSRX(1);
  private final TalonSRX armMotor1 = new TalonSRX(2);
  // private final TalonSRX rightMotor1 = new TalonSRX(2);
  // private final TalonSRX rightMotor2 = new TalonSRX(3);

  private final Joystick joystick1 = new Joystick(0);
  private final Joystick joystick2 = new Joystick(1);

  // Disabled because auto balance is not implemented yet.
  private boolean teleopStatus = false;
  private boolean autoBalance = false;

  // Macros
  private boolean macrosEnabled = true;
  // private Joystick keyboard = new Joystick(0);

  // Xbox
  XboxController xcontroller =  new XboxController(0);

  // Accelerometer
  Accelerometer accelerometer = new BuiltInAccelerometer();
  double prevXAccel = 0;
  double prevYAccel = 0;
  double xAccel = 0;
  double yAccel = 0;

  @Override
  public void robotInit() {
    timer.reset();
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
    // Initialize motor variables
    leftMotor1.configFactoryDefault(); leftMotor1.set(ControlMode.PercentOutput, 0.00);
    // leftMotor2.configFactoryDefault(); leftMotor2.set(ControlMode.PercentOutput, 0.00);
    rightMotor1.configFactoryDefault(); rightMotor1.set(ControlMode.PercentOutput, 0.00);
    // rightMotor2.configFactoryDefault(); rightMotor2.set(ControlMode.PercentOutput, 0.00);
    armMotor1.configFactoryDefault(); armMotor1.set(ControlMode.PercentOutput, 0.00);
    ct_chooser.setDefaultOption("Tank Style (Default)", tankStyle);
    ct_chooser.addOption("Keyboard (Debug)", keyboardStyle);
    SmartDashboard.putData("Teleop Controls", ct_chooser);
  }

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
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
    macrosEnabled = false;
    timer.reset();
    timer.start();
    teleopStatus = false;
  }

  /** This function is called periodically during autonomous. -- Important*/
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

  /** This function is called once when teleop is enabled. -- Important*/ 
  @Override
  public void teleopInit() {
    macrosEnabled = true;
    teleopStatus = true;
  }

  /** This function is called periodically during operator control. -- Important*/
  @Override
  public void teleopPeriodic() {
    if (macrosEnabled) { 
      // int num = keyboard.getPOV();
      int num = 0;
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
      }
    }
    if (!XboxMode) {
      leftMotor1.set(ControlMode.PercentOutput, (joystick1.getY()/3 * -1));
      rightMotor1.set(ControlMode.PercentOutput, joystick2.getY()/3);
    } else {
      leftMotor1.set(ControlMode.PercentOutput, xcontroller.getLeftY());
      rightMotor1.set(ControlMode.PercentOutput,xcontroller.getRightY());
    }
    // leftMotor2.set(ControlMode.PercentOutput, joystick1.getY()/3);
    
    // rightMotor2.set(ControlMode.PercentOutput, joystick2.getY()/3);
    // if(teleopStatus && autoBalance) {
    //   double xAxisRate            = xAccel;
    //   double yAxisRate            = yAccel;
    //   double pitchAngleDegrees    = getAccPitch();
    //   double rollAngleDegrees     = getAccRoll();
    //   boolean autoBalanceXMode = false;
    //   boolean autoBalanceYMode = false;
      
    //   if ( !autoBalanceXMode && 
    //       (Math.abs(pitchAngleDegrees) >= 
    //         Math.abs(kOffBalanceAngleThresholdDegrees))) {
    //       autoBalanceXMode = true;
    //   }
    //   else if ( autoBalanceXMode && 
    //             (Math.abs(pitchAngleDegrees) <= 
    //             Math.abs(kOonBalanceAngleThresholdDegrees))) {
    //       autoBalanceXMode = false;
    //   }
    //   if ( !autoBalanceYMode && 
    //       (Math.abs(pitchAngleDegrees) >= 
    //         Math.abs(kOffBalanceAngleThresholdDegrees))) {
    //       autoBalanceYMode = true;
    //   }
    //   else if ( autoBalanceYMode && 
    //             (Math.abs(pitchAngleDegrees) <= 
    //             Math.abs(kOonBalanceAngleThresholdDegrees))) {
    //       autoBalanceYMode = false;
    //   }
      
    //   // Control drive system automatically, 
    //   // driving in reverse direction of pitch/roll angle,
    //   // with a magnitude based upon the angle
      
    //   if ( autoBalanceXMode ) {
    //       double pitchAngleRadians = pitchAngleDegrees * (Math.PI / 180.0);
    //       xAxisRate = Math.sin(pitchAngleRadians) * -1;
    //   }
    //   if ( autoBalanceYMode ) {
    //       double rollAngleRadians = rollAngleDegrees * (Math.PI / 180.0);
    //       yAxisRate = Math.sin(rollAngleRadians) * -1;
    //   }
    //   // myRobot.mecanumDrive_Cartesian(xAxisRate, yAxisRate, joystick1.getTwist(),0); ADD REAL DRIVE HERE
    //   Timer.delay(0.005);		// wait for a motor update time
    // }
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {}

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}

  public double getAccPitch() {
	
    double Y = accelerometer.getY();
    double Z = accelerometer.getZ();
    
  
    return Math.atan2(Y,Z) *180 /Math.PI;
  }
  
  public double getAccRoll()
  {
    
    double X = accelerometer.getX();
    double Y = accelerometer.getY();
    double Z = accelerometer.getZ();
    
    return Math.atan2(-X, Math.sqrt(Y*Y + Z*Z)) * 180/Math.PI;
  }
}