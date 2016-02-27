package frc.team3223.drive;

import frc.team3223.robot2016.RobotConfiguration;
import frc.team3223.util.Pair;
import jaci.openrio.toast.core.io.Storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Replayer {
    ArrayList<DriveMotorPowers> powers;
    private RobotConfiguration conf;
    private boolean replaying = false;
    private long startTime;
    private int currentIndex;

    public Replayer(RobotConfiguration conf) {
        this.conf = conf;
        this.powers = new ArrayList<>();
    }

    public void replayPeriodic() {
        long now = System.currentTimeMillis() - startTime;
        DriveMotorPowers p = powers.get(currentIndex);
        System.out.printf("T=%s (vs %s), RF=%.2f, RR=%.2f, LF=%.2f, LR=%.2f\n", now, p.tick,
                p.frontRight, p.backRight, p.frontLeft, p.backLeft);

        currentIndex++;
        if (currentIndex == powers.size()) {
            replaying = false;
        }

    }

    public void setup(String name) {
        File target_file = Storage.highestPriority("system/recorder/" + name + ".csv");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(target_file));
            String line;
            boolean header = true;
            int timeIndex = 0;
            int rightFrontIndex = 1;
            int rightRearIndex = 2;
            int leftFrontIndex = 3;
            int leftRearIndex = 4;
            while((line=reader.readLine()) != null) {
                if(header) {
                    String[] columns = line.split(",");
                    if(!columns[timeIndex].equals("Time")) {
                        throw new Exception("Bad file!");
                    }
                    if(!columns[rightFrontIndex].equals("RF")) {
                        throw new Exception("Bad file!");
                    }
                    if(!columns[rightRearIndex].equals("RR")) {
                        throw new Exception("Bad file!");
                    }
                    if(!columns[leftFrontIndex].equals("LF")) {
                        throw new Exception("Bad file!");
                    }
                    if(!columns[leftRearIndex].equals("LR")) {
                        throw new Exception("Bad file!");
                    }

                    header = false;
                }else{
                    String[] columns = line.split(",");
                    long time = Long.parseLong(columns[timeIndex]);
                    double leftFront = Double.parseDouble(columns[leftFrontIndex]);
                    double rightFront = Double.parseDouble(columns[rightFrontIndex]);
                    double leftRear = Double.parseDouble(columns[leftRearIndex]);
                    double rightRear = Double.parseDouble(columns[rightRearIndex]);
                    DriveMotorPowers p = new DriveMotorPowers(time,
                            leftFront, rightFront,
                            leftRear, rightRear);
                    powers.add(p);

                }
            }
            replaying = true;
            startTime = System.currentTimeMillis();
            currentIndex = 0;
        }catch (IOException ex) {

        }catch(Exception ex) {
            // probably bad file
        }
    }

    public boolean isReplaying() {
        return replaying;
    }
}
