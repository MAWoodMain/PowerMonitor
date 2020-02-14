package org.ladbury.powerMonitor;

import com.beust.jcommander.Parameter;

public class Args
{
    // Command line parameters
    @Parameter(names={"--mqqtserver", "-m"})
    private String mqttServer;
    @Parameter(names={"--interval", "-i"})
    private int accumulationInterval;
    @Parameter(names = "--help", help = true)
    private boolean help;

    public String getMqttServer()
    {
        return mqttServer;
    }

    public int getAccumulationInterval()
    {
        return accumulationInterval;
    }
}

