package me.mawood.powerMonitor.metrics.units;

import me.mawood.powerMonitor.metrics.MetricType;

public interface Unit
{
    MetricType getType();
    String getUnitName();
    String getSymbol();
}
