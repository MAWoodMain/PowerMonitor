package me.mawood.powerMonitor.metrics;

import me.mawood.powerMonitor.metrics.units.Unit;

public class Metric<T extends Unit>
{
    private final double value;
    // Using generics to facilitate unit conversion later.
    private final T unit;

    public Metric(double value, T unit)
    {
        this.value = value;
        this.unit = unit;
    }
    public double getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return String.format("Metric: {%.03f %s}", value,unit.getSymbol());
    }
}
