package org.ladbury.powerMonitor.control;

import java.util.ArrayList;
import java.util.HashMap;

public class Commands
{
    private final ArrayList<Command> commands = new ArrayList<>();
    private final HashMap<Command, Class> commandClassMap = new HashMap<>();
    public Commands ()
    {
        Command command;
        try
        {
            command = new Command("get","circuit");
            commandClassMap.put(command,Class.forName("org.ladbury.powerMonitor.circuits.Circuit"));
            commands.add(command);

            command = new Command("set","circuit");
            commandClassMap.put( command,Class.forName("org.ladbury.powerMonitor.circuits.Circuit") );
            commands.add(command);

            command = new Command("get","clamp");
            commandClassMap.put( command,Class.forName("org.ladbury.powerMonitor.currentClamps.Clamp") );
            commands.add(command);

            command = new Command("set","clamp");
            commandClassMap.put(command, Class.forName("org.ladbury.powerMonitor.currentClamps.Clamp")  );
            commands.add(command);

            command = new Command("get","metricreading");
            commandClassMap.put(command, Class.forName("org.ladbury.powerMonitor.metrics.MetricReading"));
            commands.add(command);

            command = new Command("get","circuitdata");
            commandClassMap.put(command, Class.forName("org.ladbury.powerMonitor.circuits.CircuitData") );
            commands.add(command);
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public Command getCommand( String commandString, String subjectString)
    {
        try
        {
            for( Command c : commands)
            {
                if (c.getCommand().equalsIgnoreCase(commandString) && c.getSubject().equalsIgnoreCase(subjectString)) return c;
            }
            } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public Class getCommandClass(Command command)
    {
        return commandClassMap.get(command);
    }
}
