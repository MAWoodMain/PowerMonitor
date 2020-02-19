package org.ladbury.powerMonitor.circuits;

import org.ladbury.powerMonitor.monitors.configs.CurrentClampConfig;

public class Circuit
{
    private String displayName;
    private String tag;
    private  int channelNumber;
    private CurrentClampConfig clampConfig;
    private boolean monitor;

    //Constructor
    Circuit(String displayName,  int channelNumber, CurrentClampConfig clampConfig, boolean monitor)
    {
        this.displayName = displayName;
        this.tag = displayName.replace(" ", "_").toLowerCase();
        this.channelNumber = channelNumber;
        this.clampConfig = clampConfig;
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
    public CurrentClampConfig getClampConfig()
    {
        return clampConfig;
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
        clampConfig = cct.clampConfig;
        monitor = cct.monitor;
    }
    public void setNames(String displayName)
    {
        this.displayName = displayName;
        this.tag = displayName.replace(" ", "_").toLowerCase();
    }
    public void setClampConfig( CurrentClampConfig clampConfig) {this.clampConfig = clampConfig;}
    public void setMonitoring(boolean monitor){ this.monitor = monitor;}
}
