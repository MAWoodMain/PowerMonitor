package org.ladbury.powerMonitor.circuits;

import org.ladbury.powerMonitor.Main;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class Circuits
{
    public static final int MIN_CHANNEL_NUMBER = 1;
    public static final int MAX_CHANNEL_NUMBER = 9;
    private final Circuit[] circuits = new Circuit[MAX_CHANNEL_NUMBER];
    private final LinkedBlockingQueue<String> loggingQ;

    public Circuits()
    {

        circuits[0] = new Circuit("Upstairs Lighting", 1, "SCT013_5A1V", false);
        circuits[1] = new Circuit("Downstairs Lighting", 2, "SCT013_5A1V", false);
        circuits[2] = new Circuit("Extension Lighting", 3, "SCT013_5A1V", false);
        circuits[3] = new Circuit("Outside Lighting", 4, "SCT013_5A1V", false);
        circuits[4] = new Circuit("Lounge End Plugs", 5, "SCT013_20A1V", false);
        circuits[5] = new Circuit("Kitchen Plugs", 6, "SCT013_30A1V", false);
        circuits[6] = new Circuit("Outside Plugs", 7, "SCT013_20A1V", false);
        circuits[7] = new Circuit("Cooker", 8, "SCT013_30A1V", false);
        circuits[8] = new Circuit("Whole House", 9, "SCT013_100A1V", true);
        loggingQ = Main.getLoggingQ();
    }

    public static boolean validChannel(int channel)
    {
        return ((channel >= MIN_CHANNEL_NUMBER) && (channel <= MAX_CHANNEL_NUMBER));
    }

    // Getters

    public Circuit getCircuit(int channel)
    {
        if (validChannel(channel))
            return circuits[channel-1];
        else return circuits[MAX_CHANNEL_NUMBER-1];
    }

    public int getChannelByName(String circuitName)
    {
        for( int i = 0; i < MAX_CHANNEL_NUMBER; i++)
        {
            if (circuits[i].getDisplayName().equalsIgnoreCase(circuitName)) return i+1;
        }
        return -1;
    }

    public int getChannelByTag(String circuitTag)
    {
        for( int i = 0; i < MAX_CHANNEL_NUMBER; i++)
        {
            if (circuits[i].getTag().equalsIgnoreCase(circuitTag)) return i+1;
        }
        return -1;
    }

    public int getChannelFromInput(String input)
    {
        int channel;
        loggingQ.add("getChannelFromInput: (" + input + ")");
        try {
            channel = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            channel = -1;
        }
        loggingQ.add("getChannelFromInput: channel1- " + channel );
        if (Circuits.validChannel(channel)){
            return channel;
        } else
            {
            //assume we have a circuit tag
            channel = getChannelByTag(input);
            loggingQ.add("getChannelFromInput: channel2- " + channel );
            if (channel ==-1)
            {
                // not a tag, try by name
                channel = getChannelByName(input);
            }
        }
        loggingQ.add("getChannelFromInput: channel3- " + channel );
        return channel;
    }
    public String getCircuitName(int channel) {return getCircuit(channel).getDisplayName();}
    public boolean isMonitored(int channel) {return getCircuit(channel).isMonitored();}
    public int getChannelNumber(int channel) {return getCircuit(channel).getChannelNumber();}
    public String getClampName(int channel){return getCircuit(channel).getClampName();}

    // Setters
    public void setClampName(int channel, String clampName)
    {
        if (validChannel(channel))
            circuits[channel].setClampName(clampName);
    }

    public void setCircuitName(int channel, String name)
    {
        if (validChannel(channel))
            circuits[channel].setNames(name);
    }

    public void setMonitoring(int channel, boolean monitor)
    {
        if (validChannel(channel))
            circuits[channel].setMonitoring(monitor);
    }
}