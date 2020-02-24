package org.ladbury.powerMonitor.control;

public class CommandResponse
{
    private Command command;
    private String result;
    private String detail;
    private String location;

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
