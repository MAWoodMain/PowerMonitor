package me.mawood.powerMonitor.metrics.units;

import me.mawood.powerMonitor.metrics.MetricType;

public enum Current implements Unit
{
    AMPS("A", "Amps"),
    MILLI_AMPS("mA", "milli-amps");

    final String symbol;
    final String name;

    Current(String symbol, String name)
    {
        this.symbol = symbol;
        this.name = name;
    }

    @Override
    public MetricType getType()
    {
        return MetricType.CURRENT;
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
