package me.mawood.powerMonitor.metrics.units;

import me.mawood.powerMonitor.metrics.MetricType;

public enum Voltage implements Unit
{
    VOLTS("V", "Volts"),
    MILLI_VOLTS("mV", "milli-volts");

    final String symbol;
    final String name;

    Voltage(String symbol, String name)
    {
        this.symbol = symbol;
        this.name = name;
    }

    @Override
    public MetricType getType()
    {
        return MetricType.VOLTAGE;
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
