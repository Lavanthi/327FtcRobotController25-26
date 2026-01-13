// package and imports
package org.firstinspires.ftc.teamcode;

//importing to give access to OpMode, servo,motors and timer
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.util.ElapsedTime;//this is to import the time

@TeleOp(name = "scrimtele")
public class NewTele extends OpMode {

    // Drive motors
    private DcMotor frontLeft;
    private DcMotor backLeft;
    private DcMotor frontRight;
    private DcMotor backRight;
    

    // Mechanism motors/rollers/intake
    private DcMotor backRollerMotor;
    private DcMotor frontRollerMotor;

    // Servo
    private CRServo servo;

    // Timer + state
    private ElapsedTime timer = new ElapsedTime();
    private boolean sequenceRunning = false;

    @Override
    public void init() {
        //linking the motor variables to their name son the config
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");

        //linking the rollers
        backRollerMotor = hardwareMap.get(DcMotor.class, "backRoller");
        backRollerMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRollerMotor = hardwareMap.get(DcMotor.class, "leftEncoder");

        servo = hardwareMap.get(CRServo.class, "servo");//linkign the servo

        //reversing motors so robot cna drive straight
        frontRight.setDirection(DcMotor.Direction.REVERSE);
        
        //GO TO GOBUILDA SITE AND FIND THE # OF TICKS PER REVOLUTION ON THE MOTOR
        //RUN A ABACKGROUND WHICH RUNS THE MODULO TO FIGURE HOW OFTEN IT RUNS(HOW LONG IT TAKES FOR A FULL REOLVUIN AT FLL PWOER)
        //WAIT UNTIL IT REACH FULL POWER AND LIFTS IT UP,SET THE SERVO
    }

    @Override
    public void loop() {

         /* ---------------- DRIVING ---------------- */
        double forward = -gamepad1.left_stick_y;//forward/backward
        double strafe  =  gamepad1.right_stick_x;//turning
        double turn    =  gamepad1.left_stick_x;//strafing

        //calculate power for each wheel(mecanum drive)
        double fl = forward + strafe + turn;
        double bl = forward - strafe + turn;
        double fr = forward - strafe - turn;
        double br = forward + strafe - turn;

        //making sure no motor power goes over 100%
        double max = Math.max(
                Math.max(Math.abs(fl), Math.abs(bl)),
                Math.max(Math.abs(fr), Math.abs(br))
        );

        if (max > 1.0) {
            fl /= max;
            bl /= max;
            fr /= max;
            br /= max;
        }

        //send power to drive motors
        frontLeft.setPower(fl);
        backLeft.setPower(bl);
        frontRight.setPower(fr);
        backRight.setPower(br);
        

        /* ---------------- FRONT ROLLER ---------------- */
        
        //run front roller when right numper is pressed
        if (gamepad1.right_bumper) {
            frontRollerMotor.setPower(-1);
        } else {
            frontRollerMotor.setPower(0);//else roller doesnt move
        }
        
        /* ---------------- TIMED SEQUENCE & BACK ROLLER ---------------- */

        //run the servo while left bumper is pressed
        //this is as a backup, just incase we ever need to lower the servo for the ball to go through
        if(gamepad1.left_bumper){
            servo.setPower(-1);
        }
        else{servo.setPower(0);}
        
        // Start timer when right trigger is pressed
        if (gamepad1.right_trigger > 0 && !sequenceRunning) {
            timer.reset();//start timer at 0
            sequenceRunning = true;//set timer running as true
        }

        //as timer runs
        if (sequenceRunning) {
            if (timer.seconds() < 3.8) {
                // First 3.8 seconds: run back roller
                backRollerMotor.setPower(-0.99);
                servo.setPower(-0);
            } else {
                // After 3.8 seconds: stop roller, move servo to shoot the artifact
                backRollerMotor.setPower(0);
                servo.setPower(1);
            }
        }

        // Reset everything when trigger released
        if (gamepad1.right_trigger == 0) {
            sequenceRunning = false;
            backRollerMotor.setPower(0);
            servo.setPower(0);
        }

        /* ---------------- TELEMETRY ---------------- */
        
        //we wrote this so we could veiw the time in the telemetry
        //this way, we could edit or chnage the time if needed
        //SHOW IN THE TELEMETRY 
        telemetry.addData("Sequence Running", sequenceRunning);
        telemetry.addData("Timer", timer.seconds());
        telemetry.update();
    }
}
