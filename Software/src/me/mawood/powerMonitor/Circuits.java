package me.mawood.powerMonitor;

import me.mawood.powerMonitor.metrics.monitors.configs.CurrentClampConfig;

public interface Circuits
{
    String getDisplayName();
    int getChannelNumber();
    CurrentClampConfig getClampConfig();
}
