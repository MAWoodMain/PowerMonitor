package me.mawood.powerMonitor.metrics;

import me.mawood.powerMonitor.metrics.units.Unit;
import me.mawood.powerMonitor.metrics.units.UnitType;

public enum MetricDefinition implements Unit
{
    VOLTS("Voltage","V", "Volts"),
    MILLI_VOLTS("Voltage","mV", "milli-volts"),
    AMPS("Current","A", "Amps"),
    MILLI_AMPS("Current","mA", "milli-amps"),
    WATT_HOURS("Energy","WH", "Watt Hours"),
    KILOWATT_HOURS("Energy","kWH", "KiloWatt Hours"),
    WATTS("Real Power","W","Watts"), //real power
    VA("Apparent Power","VA", "Volt Amperes"), //Apparent power
    VAR("Reactive Power","VAR", "Volt Amperes Reactive"), //Reactive Power
    KILOWATT("Real Power","KW", "Kilowatt"),
    POWERFACTOR("Power Factor","PF","Power Factor");

    final String metricName;
    final String symbol;
    final String unitName;

    MetricDefinition(String metricName, String symbol, String unitName)
    {
        this.metricName = metricName;
        this.symbol = symbol;
        this.unitName = unitName;
    }

    @Override
    public UnitType getType()
    {
        return UnitType.CURRENT;
    }
    @Override
    public String getUnitName()
    {
        return unitName;
    }
    @Override
    public String getSymbol()
    {
        return symbol;
    }

    public String getMetricName()
    {
        return metricName;
    }

}