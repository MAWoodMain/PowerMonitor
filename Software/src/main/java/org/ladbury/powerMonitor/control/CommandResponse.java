package org.ladbury.powerMonitor.control;

public class CommandResponse
{
    private final Command command;
    private final String result;
    private final String detail;
    private final String location;

    CommandResponse(Command command)
    {
        this.command = command;
        this.result = "Success";
        this.detail = "";
        this.location = "";
    }
    CommandResponse(Command command, String result, String detail, String location )
    {
        this.command = command;
        this.result = result;
        this.detail = detail;
        this.location = location;
    }
    public String toString()
    {
        return ( location + ": " + result + " " + detail + " - " +command.toString());
    }
}
