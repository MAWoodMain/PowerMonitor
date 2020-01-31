package me.mawood.powerMonitor.metrics;

public enum Metric
{
    VOLTS("Voltage",1.0,"V", "Volts"),
    MILLI_VOLTS("Voltage",0.001,"mV", "milli-volts"),
    AMPS("Current",1.0,"A", "Amps"),
    MILLI_AMPS("Current",0.001,"mA", "milli-amps"),
    WATT_HOURS("Energy",1.0,"WH", "Watt Hours"),
    KILOWATT_HOURS("Energy",1000.0,"kWH", "KiloWatt Hours"),
    WATTS("Real",1.0,"W","Watts"),
    KILOWATT("Real",1000.0,"KW", "Kilowatt"),
    VA("Apparent", 1.0,"VA", "Volt Amperes"),
    VAR("Reactive", 1.0,"VAR", "Volt Amperes Reactive"),
    POWERFACTOR("Power Factor", 1.0,"PF","Power Factor");

    final String metricName;
    final double factor;
    final String symbol;
    final String unitName;

    Metric(String metricName, double factor, String symbol, String unitName)
    {
        this.metricName = metricName;
        this.factor = factor;
        this.symbol = symbol;
        this.unitName = unitName;
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

}