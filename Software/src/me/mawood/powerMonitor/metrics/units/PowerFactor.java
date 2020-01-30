package me.mawood.powerMonitor.metrics.units;

import me.mawood.powerMonitor.metrics.MetricType;

public enum PowerFactor implements Unit
{
    POWER_FACTOR;

    @Override
    public MetricType getType()
    {
        return MetricType.POWER_FACTOR;
    }
    @Override
    public String getUnitName()
    {
        return "Power Factor";
    }
    @Override
    public String getSymbol()
    {
        return "PF";
    }
}
