package org.ladbury.powerMonitor.circuits;

public class Circuit
{
    private String displayName;
    private String tag;
    private  int channelNumber;
    private String clampName;
    private boolean monitor;

    //Constructor
    Circuit(String displayName,  int channelNumber, String clampName, boolean monitor)
    {
        this.displayName = displayName;
        this.tag = displayName.replace(" ", "_").toLowerCase();
        this.channelNumber = channelNumber;
        this.clampName = clampName;
        this.monitor = monitor;
    }

    //Getters
    public String getDisplayName()
    {
        return displayName;
    }
    public int getChannelNumber()
    {
        return channelNumber;
    }
    public String getClampName()
    {
        return clampName;
    }
    public String getTag()
    {
        return tag;
    }
    public boolean isMonitored() {return monitor; }

    //Setters
    public void setCircuit( Circuit cct)
    {
        displayName = cct.displayName;
        tag = cct.tag;
        channelNumber = cct.channelNumber;
        clampName = cct.clampName;
        monitor = cct.monitor;
    }
    public void setNames(String displayName)
    {
        this.displayName = displayName;
        this.tag = displayName.replace(" ", "_").toLowerCase();
    }
    public void setClampName(String clampName) {this.clampName = clampName;}
    public void setMonitoring(boolean monitor){ this.monitor = monitor;}
}
