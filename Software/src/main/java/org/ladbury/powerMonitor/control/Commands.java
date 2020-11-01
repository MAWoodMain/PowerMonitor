package org.ladbury.powerMonitor.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public class Commands
{
    private final ArrayList<Command> commands = new ArrayList<>();
    private final HashMap<Command, Function<Command,String>> commandFunctionMap = new HashMap<>();
    public Commands (CommandProcessor cp)
    {
        Command command;

        command = new Command("get","circuit");
        commandFunctionMap.put(command,cp::getCircuit);
        commands.add(command);

        command = new Command("set","circuit");
        commandFunctionMap.put( command, cp::setCircuit);
        commands.add(command);

        command = new Command("get","clamp");
        commandFunctionMap.put( command, cp::getClamp );
        commands.add(command);

        command = new Command("set","clamp");
        commandFunctionMap.put(command, cp::setClamp  );
        commands.add(command);

        command = new Command("get","metricreading");
        commandFunctionMap.put(command, cp::getMetricReading);
        commands.add(command);

        command = new Command("get","powerdata");
        commandFunctionMap.put(command, cp::getCircuitPowerData);
        commands.add(command);

        command = new Command("get","energydata");
        commandFunctionMap.put(command, cp::getCircuitEnergyData);
        commands.add(command);
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
    public String callCommand(Command command)
    {
        Command MatchedCommand = getCommand(command.getCommand(),command.getSubject());
        if (MatchedCommand == null)
        {
            return "Failed to find command: " + command.toString();
        }
        return commandFunctionMap.get(MatchedCommand).apply(command);
    }
}
