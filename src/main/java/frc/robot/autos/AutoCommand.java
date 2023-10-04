package frc.robot.autos;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.RobotContainer;
import frc.robot.subsystems.DrivetrainSubsystem;
import frc.robot.utils.CommandUtil;

public class AutoCommand extends SequentialCommandGroup{    
    DrivetrainSubsystem drivetrain;

    List<Trajectory.State> trajectoryStates;

    List<Waypoint> waypoints;
    List<Marker> markers;

    Waypoint waypoint;
    Marker marker;

    RobotContainer container;

    public AutoCommand(RobotContainer container) {
        this.drivetrain = container.getDrivetrainSubsystem();
        this.container = container;

        readTrajectories();
        readWaypoints();
        
        drivetrain.getGyro().reset();
        drivetrain.setBrakeMode();
        drivetrain.resetOdometry(trajectoryStates.get(0).getPose());

        int lastIndex = 0;
        int waypointIndex = 0;
        int markerIndex = 0;
        for(int i = 0; i < trajectoryStates.size(); i++) {
            Trajectory.State state = trajectoryStates.get(i);
            Waypoint waypoint = waypoints.get(waypointIndex);
        //     //Marker marker = markers.get(markerIndex);

        //     // if(state.getPose().getX() == marker.anchorPoint.get("x") && 
        //     //    state.getPose().getY() == waypoint.anchorPoint.get("y")) {

            

        //     //    for(String s : marker.names)
        //     // } 

            if(state.getPose().getX() == waypoint.anchorPoint.get("x") && 
               state.getPose().getY() == waypoint.anchorPoint.get("y") && waypoint.isStopPoint) {
                if(i != lastIndex) {
                    addCommands(new FollowTrajectoryCommand(container, new Trajectory(trajectoryStates.subList(lastIndex, i))));
                    lastIndex = i;
                }
                
                addCommands(CommandUtil.getCommand(container, waypoint.stopEvent));

                waypointIndex++;
            }
        }

        if(lastIndex != trajectoryStates.size()) addCommands(new FollowTrajectoryCommand(container, new Trajectory(trajectoryStates.subList(0, trajectoryStates.size()))));
    }

    public void readTrajectories() {
        Path filepath = Filesystem.getDeployDirectory().toPath().resolve("pathplanner/generatedJSON/New Path.wpilib.json");
        String json = "";
        try {
            json = Files.readString(filepath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new GsonBuilder()
                        .excludeFieldsWithoutExposeAnnotation()
                        .create();

        Type type = new TypeToken<List<Trajectory.State>>() {}.getType();
        trajectoryStates = gson.fromJson(json, type);
    }

    public void readWaypoints() {
        String json = "";
        try {
            json = Files.readString(Filesystem.getDeployDirectory().toPath().resolve("pathplanner/New Path.path"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Convert JSON file into an ArrayList of type State
        // This creates an array list of all the waypoints, with
        // State representing the values within each waypoint.
        Gson gson = new GsonBuilder()
                        .excludeFieldsWithoutExposeAnnotation()
                        .create();

        Type type = new TypeToken<frc.robot.autos.Path>() {}.getType();
        frc.robot.autos.Path path = gson.fromJson(json, type);

        waypoints = path.waypoints;
        markers = path.markers;
    }
}
