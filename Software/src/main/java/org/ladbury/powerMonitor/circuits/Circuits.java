package org.ladbury.powerMonitor.circuits;

import org.ladbury.powerMonitor.Main;
import org.ladbury.powerMonitor.publishers.PMLogger;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public class Circuits
{
    public static final int MIN_CHANNEL_NUMBER = 1;
    public static final int MAX_CHANNEL_NUMBER = 9;
    private final ArrayList<Circuit> circuits = new ArrayList<>();
    private final PMLogger logger;

    public Circuits()
    {

        circuits.add(new Circuit("Upstairs Lighting", 1, "SCT013_5A1V", false));
        circuits.add(new Circuit("Downstairs Lighting", 2, "SCT013_5A1V", false));
        circuits.add(new Circuit("Extension Lighting", 3, "SCT013_5A1V", false));
        circuits.add(new Circuit("Outside Lighting", 4, "SCT013_5A1V", false));
        circuits.add(new Circuit("Lounge End Plugs", 5, "SCT013_20A1V", false));
        circuits.add(new Circuit("Kitchen Plugs", 6, "SCT013_30A1V", false));
        circuits.add(new Circuit("Outside Plugs", 7, "SCT013_20A1V", false));
        circuits.add(new Circuit("Cooker", 8, "SCT013_30A1V", false));
        circuits.add(new Circuit("Whole House", 9, "SCT013_100A1V", true));
        logger = Main.getLogger();
    }

    public static boolean validChannel(int channel)
    {
        return ((channel >= MIN_CHANNEL_NUMBER) && (channel <= MAX_CHANNEL_NUMBER));
    }

    // Getters

    public ArrayList<Circuit> getCircuits(){return circuits;}
    public Circuit getCircuit(int channel)
    {
        if (validChannel(channel))
            return circuits.get(channel-1);
        else return circuits.get(MAX_CHANNEL_NUMBER-1);
    }

    public int getChannelByName(String circuitName)
    {
        for(Circuit circuit : circuits)
        {
            if (circuit.getDisplayName().equalsIgnoreCase(circuitName)) return circuit.getChannelNumber();
        }
        return -1;
    }

    public int getChannelByTag(String circuitTag)
    {
        for(Circuit circuit : circuits)
        {
            if (circuit.getTag().equalsIgnoreCase(circuitTag)) return circuit.getChannelNumber();
        }
        return -1;
   }

    public int getChannelFromInput(String input)
    {
        int channel;
        if (input == null) {
            logger.add("getChannelFromInput: null input", Level.WARNING,this.getClass().getName());
            return -1;
        }
        try {
            channel = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            channel = -1;
        }
        if (Circuits.validChannel(channel)){
            return channel;
        } else
            {
            //assume we have a circuit tag
            channel = getChannelByTag(input);
            if (!Circuits.validChannel(channel))
            {
                // not a tag, try by name
                channel = getChannelByName(input);
            }
        }
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
            circuits.get(channel-1).setClampName(clampName);
    }

    public void setCircuitName(int channel, String name)
    {
        if (validChannel(channel))
            circuits.get(channel-1).setNames(name);
    }

    public void setMonitoring(int channel, boolean monitor)
    {
        if (validChannel(channel))
            circuits.get(channel-1).setMonitoring(monitor);
    }
}