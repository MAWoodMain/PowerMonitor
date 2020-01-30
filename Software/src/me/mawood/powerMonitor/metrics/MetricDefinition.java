package me.mawood.powerMonitor.metrics;

public enum MetricDefinition
{
    VOLTS(MetricType.VOLTAGE,"Voltage",1.0,"V", "Volts"),
    MILLI_VOLTS(MetricType.VOLTAGE,"Voltage",0.001,"mV", "milli-volts"),
    AMPS(MetricType.CURRENT,"Current",1.0,"A", "Amps"),
    MILLI_AMPS(MetricType.CURRENT,"Current",0.001,"mA", "milli-amps"),
    WATT_HOURS(MetricType.ENERGY,"Energy",1.0,"WH", "Watt Hours"),
    KILOWATT_HOURS(MetricType.ENERGY,"Energy",1000.0,"kWH", "KiloWatt Hours"),
    WATTS(MetricType.POWER,"Real Power",1.0,"W","Watts"),
    VA(MetricType.POWER,"Apparent Power", 1.0,"VA", "Volt Amperes"),
    VAR(MetricType.POWER,"Reactive Power", 1.0,"VAR", "Volt Amperes Reactive"),
    KILOWATT(MetricType.POWER,"Real Power",1000.0,"KW", "Kilowatt"),
    POWERFACTOR(MetricType.POWER_FACTOR,"Power Factor", 1.0,"PF","Power Factor");

    final MetricType metricType;
    final String metricName;
    final double factor;
    final String symbol;
    final String unitName;

    MetricDefinition(MetricType metricType, String metricName, double factor,String symbol, String unitName)
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