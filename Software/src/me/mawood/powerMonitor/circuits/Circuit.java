package me.mawood.powerMonitor.circuits;

import me.mawood.powerMonitor.packets.monitors.configs.CurrentClampConfig;

public interface Circuit
{
    String getDisplayName();
    int getChannelNumber();
    CurrentClampConfig getClampConfig();
    String getTag();
}
