package frc.robot.autos;

import java.util.List;

import com.google.gson.annotations.Expose;

public class StopEvent {
    @Expose
    public List<String> names;
    @Expose
    public String executionBehavior;
}
