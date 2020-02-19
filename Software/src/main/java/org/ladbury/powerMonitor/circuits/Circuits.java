package org.ladbury.powerMonitor.circuits;

import org.ladbury.powerMonitor.packets.monitors.configs.CurrentClampConfig;

public class Circuits
{
    public static final int MIN_CHANNEL_NUMBER = 1;
    public static final int MAX_CHANNEL_NUMBER = 9;
    private Circuit[] circuits = new Circuit[MAX_CHANNEL_NUMBER-MIN_CHANNEL_NUMBER];

    void circuits()
    {

        circuits[0] = new Circuit ("Upstairs Lighting", 1, CurrentClampConfig.SCT013_5A1V, false);
        circuits[1] = new Circuit("Downstairs Lighting", 2, CurrentClampConfig.SCT013_5A1V, false);
        circuits[2] = new Circuit("Extension Lighting", 3, CurrentClampConfig.SCT013_5A1V, false);
        circuits[3] = new Circuit("Outside Lighting", 4, CurrentClampConfig.SCT013_5A1V, false);
        circuits[4] = new Circuit("Lounge End Plugs", 5, CurrentClampConfig.SCT013_20A1V, false);
        circuits[5] = new Circuit("Kitchen Plugs", 6, CurrentClampConfig.SCT013_30A1V, false);
        circuits[6] = new Circuit("Outside Plugs", 7, CurrentClampConfig.SCT013_20A1V, false);
        circuits[7] = new Circuit("Cooker", 8, CurrentClampConfig.SCT013_30A1V, false);
        circuits[8] = new Circuit("Whole House", 9, CurrentClampConfig.SCT013_100A1V, true);
    }

    // Getters and Setters
    public String getCircuitName(CircuitID circuitID)
    {
        return circuits[circuitID.ordinal()].getDisplayName();
    }
    public void setCircuitName(CircuitID circuitID, String name )
    {
        circuits[circuitID.ordinal()].setNames(name);
    }
    public int getChannelNumber(CircuitID circuitID)
    {
        return circuits[circuitID.ordinal()].getChannelNumber();
    }

    public CurrentClampConfig getClampConfig(CircuitID circuitID)
    {
        return circuits[circuitID.ordinal()].getClampConfig();
    }
    public void setClampConfig(CircuitID circuitID, CurrentClampConfig ccc)
    {
        circuits[circuitID.ordinal()].setClampConfig(ccc);
    }
}


