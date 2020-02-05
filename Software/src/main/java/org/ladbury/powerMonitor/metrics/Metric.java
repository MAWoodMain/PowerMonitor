package org.ladbury.powerMonitor.metrics;

public enum Metric
{
    VOLTS(MetricType.VOLTAGE,"Voltage",1.0,"V", "Volts"),
    MILLI_VOLTS(MetricType.VOLTAGE,"Voltage",0.001,"mV", "milli-volts"),
    AMPS(MetricType.CURRENT,"Current",1.0,"A", "Amps"),
    MILLI_AMPS(MetricType.CURRENT,"Current",0.001,"mA", "milli-amps"),
    WATT_HOURS(MetricType.ENERGY,"Energy",1.0,"WH", "Watt Hours"),
    KILOWATT_HOURS(MetricType.ENERGY,"Energy",1000.0,"kWH", "KiloWatt Hours"),
    WATTS(MetricType.POWER,"Real",1.0,"W","Watts"),
    VA(MetricType.POWER,"Apparent", 1.0,"VA", "Volt Amperes"),
    VAR(MetricType.POWER,"Reactive", 1.0,"VAR", "Volt Amperes Reactive"),
    KILOWATT(MetricType.POWER,"Real",1000.0,"KW", "Kilowatt"),
    POWERFACTOR(MetricType.POWER_FACTOR,"PowerFactor", 1.0,"PF","Power Factor");

    final MetricType metricType;
    final String metricName;
    final double factor;
    final String symbol;
    final String unitName;

    Metric(MetricType metricType, String metricName, double factor, String symbol, String unitName)
    {
        this.metricType = metricType;
        this.metricName = metricName;
        this.factor = factor;
        this.symbol = symbol;
        this.unitName = unitName;
    }

    public MetricType getType()
    {
        return MetricType.CURRENT;
    }

    public String getUnitName()
    {
        return unitName;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public String getMetricName()
    {
        return metricName;
    }

    public double getFactor()
    {
        return factor;
    }

    public MetricType getMetricType()
    {
        return metricType;
    }
}