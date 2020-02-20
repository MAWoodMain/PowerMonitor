package org.ladbury.powerMonitor.control;

public class Command
{
    private String command;
    private String commandSubject;
    private Class parameterClass;

    // Constructor
    public Command(String command, String commandSubject, Class parameterClass)
    {
        this.command = command;
        this.commandSubject = commandSubject;
        this.parameterClass = parameterClass;
    }

    // Getters and Setters
    public String getCommand() {return command;}
    public void setCommand(String command) {this.command = command;}
    public String getCommandSubject() {return commandSubject;}
    public void setCommandSubject(String commandSubject) {this.commandSubject = commandSubject;}
    public Class getParameterClass() {return parameterClass;}
    public void setParameterClass(Class parameterClass) {this.parameterClass = parameterClass;}
}
