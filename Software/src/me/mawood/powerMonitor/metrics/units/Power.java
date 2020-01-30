package me.mawood.powerMonitor.metrics.units;

import me.mawood.powerMonitor.metrics.MetricType;

public enum Power implements Unit
{
    WATTS("W","Watts"), //real power
    VA("VA", "Volt Amperes"), //Apparent power
    VAR("VAR", "Volt Amperes Reactive"), //Reactive Power
    KILOWATT("KW", "Kilowatt");

    final String symbol;
    final String name;

    Power(String symbol, String name)
    {
        this.symbol = symbol;
        this.name = name;
    }

    @Override
    public MetricType getType()
    {
        return MetricType.POWER;
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
