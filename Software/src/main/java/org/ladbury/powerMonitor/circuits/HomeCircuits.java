package org.ladbury.powerMonitor.circuits;

import org.ladbury.powerMonitor.packets.monitors.configs.CurrentClampConfig;

public enum HomeCircuits implements CircuitData
{
    CH1("Upstairs Lighting", 1, CurrentClampConfig.SCT013_5A1V),
    CH2("Downstairs Lighting", 2, CurrentClampConfig.SCT013_5A1V),
    CH3("Extension Lighting", 3, CurrentClampConfig.SCT013_5A1V),
    CH4("Outside Lighting", 4, CurrentClampConfig.SCT013_5A1V),
    CH5("Lounge End Plugs", 5, CurrentClampConfig.SCT013_20A1V),
    CH6("Kitchen Plugs", 6, CurrentClampConfig.SCT013_30A1V),
    CH7("Outside Plugs", 7, CurrentClampConfig.SCT013_20A1V),
    CH8("Cooker", 8, CurrentClampConfig.SCT013_30A1V),
    CH9("Whole House", 9, CurrentClampConfig.SCT013_100A1V);

    final String displayName;
    final String tag;
    final int channelNumber;
    final CurrentClampConfig clampConfig;

    HomeCircuits(String displayName,  int channelNumber, CurrentClampConfig clampConfig)
    {
        this.displayName = displayName;
        this.tag = displayName.replace(" ", "_").toLowerCase();
        this.channelNumber = channelNumber;
        this.clampConfig = clampConfig;
    }
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
}
