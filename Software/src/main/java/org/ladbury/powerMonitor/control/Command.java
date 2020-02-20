package org.ladbury.powerMonitor.control;

public class Command
{
    private Commands command;
    private String commandSubject;
    private String[] params;

    public Command(Commands command, String commandSubject, String[] params)
    {
        this.command = command;
        this.commandSubject = commandSubject;
        this.params = params;
    }

    public Commands getCommand()
    {
        return command;
    }

    public void setCommand(Commands command)
    {
        this.command = command;
    }

    public String getCommandSubject()
    {
        return commandSubject;
    }

    public void setCommandSubject(String commandSubject)
    {
        this.commandSubject = commandSubject;
    }

    public String[] getParams()
    {
        return params;
    }

    public void setParams(String[] params)
    {
        this.params = params;
    }
}
