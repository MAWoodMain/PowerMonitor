package org.ladbury.powerMonitor;

import com.beust.jcommander.Parameter;

public class Args
{
    // Command line parameters
    @SuppressWarnings("SpellCheckingInspection")
    @Parameter(names={"--mqqtserver", "-m"})
    private String mqttServer = "10.0.128.20";
    @Parameter(names={"--ClientName", "-c"})
    private String clientName = "Pmon10";
    @Parameter(names={"--interval", "-i"})
    private int accumulationInterval = 5;
    @Parameter(names={"LoggingLevel","-l"})
    private String loggingLevelStr = "info";
    @Parameter(names = {"--help","-h"}, help = true)
    private boolean help;

    //Getters
    public String getMqttServer() {return mqttServer;}
    public String getClientName() {return clientName;}
    public int getAccumulationInterval(){return accumulationInterval;}
    public String getLoggingLevelStr() {return loggingLevelStr;}
    public boolean isHelp(){return help;}
}