package org.ladbury.powerMonitor.circuits;

public class Circuit
{
    private String displayName;
    private String tag;
    private  int channelNumber;
    private String clampName;
    private boolean monitor;
    private boolean publishPower;
    private boolean publishEnergy;

    //Constructors
    Circuit(String displayName,  int channelNumber, String clampName)
    {
        this.displayName = displayName;
        this.tag = displayName.replace(" ", "_");
        this.channelNumber = channelNumber;
        this.clampName = clampName;
        this.monitor = true;
        this.publishPower = true;
        this.publishEnergy = true;
    }
    Circuit(String displayName,  int channelNumber, String clampName, boolean monitor)
    {
        this.displayName = displayName;
        this.tag = displayName.replace(" ", "_");
        this.channelNumber = channelNumber;
        this.clampName = clampName;
        this.monitor = monitor;
        this.publishPower = monitor;
        this.publishEnergy = monitor;
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
    public boolean isPublishingPower() {return publishPower; }
    public boolean isPublishingEnergy() {return publishPower; }

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
    public void setPublishPower(boolean publish){this.publishPower = publish;}
    public void setPublishEnergy(boolean publish){this.publishEnergy = publish;}
}
