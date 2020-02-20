package org.ladbury.powerMonitor.control;

import org.ladbury.powerMonitor.circuits.Circuit;

import java.util.ArrayList;

public class Commands
{
    private final ArrayList<Command> commands = new ArrayList<>();
    public Commands ()
    {
        try
        {
            commands.add(new Command("get","circuit", Class.forName("org.ladbury.powerMonitor.circuits.Circuit")));
            commands.add(new Command("set","circuit", Class.forName("org.ladbury.powerMonitor.circuits.Circuit")));
            commands.add(new Command("get","clamp", Class.forName("org.ladbury.powerMonitor.currentClamps.Clamp")));
            commands.add(new Command("set","clamp", Class.forName("org.ladbury.powerMonitor.currentClamps.Clamp")));
            commands.add(new Command("get","metricreading", Class.forName("org.ladbury.powerMonitor.metrics.MetricReading")));
            commands.add(new Command("get","circuitdata", Class.forName("org.ladbury.powerMonitor.circuits.CircuitData")));
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

}
