package org.ladbury.powerMonitor.circuits;

import org.ladbury.powerMonitor.packets.monitors.configs.CurrentClampConfig;

public class Circuits
{
    public static final int MIN_CHANNEL_NUMBER = 1;
    public static final int MAX_CHANNEL_NUMBER = 9;
    private String[] circuitNames =
             {
                    "Upstairs Lighting",
                    "Downstairs Lighting",
                    "Extension Lighting",
                    "Outside Lighting",
                    "Lounge End Plugs",
                    "Kitchen Plugs",
                    "Outside Plugs",
                    "Cooker",
                    "Whole House"
             };

    private CurrentClampConfig[] clamps =
            {
                    CurrentClampConfig.SCT013_5A1V,
                    CurrentClampConfig.SCT013_5A1V,
                    CurrentClampConfig.SCT013_5A1V,
                    CurrentClampConfig.SCT013_5A1V,
                    CurrentClampConfig.SCT013_20A1V,
                    CurrentClampConfig.SCT013_30A1V,
                    CurrentClampConfig.SCT013_20A1V,
                    CurrentClampConfig.SCT013_30A1V,
                    CurrentClampConfig.SCT013_100A1V
            };

    private int[] channelNbrs =
            {1,2,3,4,5,6,7,8,9};

    // Getters and Setters
    public String getCircuitName(CircuitID circuitID)
    {
        return circuitNames[circuitID.ordinal()];
    }
    public void setCircuitName(CircuitID circuitID, String name )
    {
        circuitNames[circuitID.ordinal()] = name;
    }
    public void setChannelNumber(CircuitID circuitID, int channelNbr)
    {
        channelNbrs[circuitID.ordinal()] = channelNbr;
    }

    public CurrentClampConfig getClampConfig(CircuitID circuitID)
    {
        return clamps[circuitID.ordinal()];
    }
    public void setClampConfig(CircuitID circuitID, CurrentClampConfig ccc)
    {
        clamps[circuitID.ordinal()] = ccc;
    }
}


