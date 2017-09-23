package me.mawood.powerMonitor.circuits;

import me.mawood.powerMonitor.packets.monitors.configs.CurrentClampConfig;

public enum HomeCircuits implements Circuits
{
    UPSTAIRS_LIGHTING("Upstairs Lighting", 1, CurrentClampConfig.SCT013_5A1V),
    DOWNSTAIRS_LIGHTING("Downstairs Lighting", 2, CurrentClampConfig.SCT013_5A1V),
    EXTENSION_LIGHTING("Extension Lighting", 3, CurrentClampConfig.SCT013_5A1V),
    OUTSIDE_LIGHTING("Outside Lighting", 4, CurrentClampConfig.SCT013_5A1V),
    LOUNGE_PLUGS("Lounge End Plugs", 5, CurrentClampConfig.SCT013_20A1V),
    KITCHEN_PLUGS("Kitchen Plugs", 6, CurrentClampConfig.SCT013_30A1V),
    OUTSIDE_PLUGS("Outside Plugs", 7, CurrentClampConfig.SCT013_20A1V),
    COOKER("Cooker", 8, CurrentClampConfig.SCT013_30A1V),
    WHOLE_HOUSE("Whole House", 9, CurrentClampConfig.SCT013_100A1V);

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
