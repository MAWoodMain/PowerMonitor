package org.ladbury.powerMonitor.circuits;

import org.ladbury.powerMonitor.packets.monitors.configs.CurrentClampConfig;

public interface CircuitData
{
    String getDisplayName();
    int getChannelNumber();
    CurrentClampConfig getClampConfig();
    String getTag();
}
