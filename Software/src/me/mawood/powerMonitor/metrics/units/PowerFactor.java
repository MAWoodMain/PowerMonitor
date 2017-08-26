package me.mawood.powerMonitor.metrics.units;

public enum PowerFactor implements Unit
{
    POWER_FACTOR;

    @Override
    public UnitType getType()
    {
        return UnitType.POWER_FACTOR;
    }
    @Override
    public String getName()
    {
        return "Power Factor";
    }
    @Override
    public String getSymbol()
    {
        return "PF";
    }
}
