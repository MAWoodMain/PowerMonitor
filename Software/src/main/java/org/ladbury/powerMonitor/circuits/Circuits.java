package org.ladbury.powerMonitor.circuits;

public class Circuits
{
    public static final int MIN_CHANNEL_NUMBER = 1;
    public static final int MAX_CHANNEL_NUMBER = 9;
    private final Circuit[] circuits = new Circuit[MAX_CHANNEL_NUMBER];

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
    }

    public boolean validChannel(int channel)
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
        for( int i = MIN_CHANNEL_NUMBER; i <= MAX_CHANNEL_NUMBER; i++)
        {
            if (circuits[i].getDisplayName().equalsIgnoreCase(circuitName)) return i;
        }
        return 0;
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