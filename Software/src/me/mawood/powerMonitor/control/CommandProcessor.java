package me.mawood.powerMonitor.control;

import me.mawood.powerMonitor.Main;

public class CommandProcessor extends Thread
{
    //
    // Runnable implementation
    //

    /**
     * run  The main Command Processor loop
     */
    public void processMQTTCommand(String command)
    {
        Main.getPowerDataMQTTPublisher().sendLogMessage("Command Received: "+ command);
    }
    @Override
    public void run()
    {

    }
}