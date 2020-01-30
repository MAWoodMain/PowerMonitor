package me.mawood.powerMonitor.metrics.units;

import me.mawood.powerMonitor.metrics.MetricType;

public enum Energy implements Unit
{
    WATT_HOURS("WH", "Watt Hours"),
    KILOWATT_HOURS("kWH", "KiloWatt Hours");

    final String symbol;
    final String name;

    Energy(String symbol, String name)
    {
        this.symbol = symbol;
        this.name = name;
    }

    @Override
    public MetricType getType()
    {
        return MetricType.ENERGY;
    }
    @Override
    public String getUnitName()
    {
        return name;
    }
    @Override
    public String getSymbol()
    {
        return symbol;
    }
}
