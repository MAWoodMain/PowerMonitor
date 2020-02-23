package org.ladbury.powerMonitor.control;

public class Command
{
    private String command;
    private String subject;
    private String key;
    private String data;

    // Constructors

    public Command(String command, String subject)
    {
        this.command = command;
        this.subject = subject;
         this.key = "";
        this.data = "";
    }

    // Getters and Setters
    public String getCommand() {return command;}
    public void setCommand(String command) {this.command = command;}
    public String getSubject() {return subject;}
    public void setSubject(String subject) {this.subject = subject;}
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public String toString()
    {
        return "{ "+command + ", " + subject + ", "+key+", " + data + " }" ;
    }
}
