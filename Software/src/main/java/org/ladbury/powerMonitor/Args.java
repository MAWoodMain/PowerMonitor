package org.ladbury.powerMonitor;

import com.beust.jcommander.Parameter;

public class Args
{
    // Command line parameters
    @Parameter(names={"--mqqtserver", "-m"})
    private String mqttServer;
    @Parameter(names={"--ClientName", "-c"})
    private String clientName;
    @Parameter(names={"--interval", "-i"})
    private int accumulationInterval;
    @Parameter(names = "--help", help = true)
    private boolean help;

    //Getters
    public String getMqttServer() {return mqttServer;}
    public String getClientName() {return clientName;}
    public int getAccumulationInterval(){return accumulationInterval;}
}