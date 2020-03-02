package org.ladbury.powerMonitor.metrics;

public enum Metric
{
    VOLTS("Voltage",1.0,"V", "Volts"),
    MILLI_VOLTS("Voltage",0.001,"mV", "milli-volts"),
    AMPS("Current",1.0,"A", "Amps"),
    MILLI_AMPS("Current",0.001,"mA", "milli-amps"),
    WATT_HOURS("Energy",1.0,"WH", "Watt Hours"),
    KILOWATT_HOURS("Energy",1000.0,"kWH", "KiloWatt Hours"),
    WATTS("Real",1.0,"W","Watts"),
    VA("Apparent", 1.0,"VA", "Volt Amperes"),
    VAR("Reactive", 1.0,"VAR", "Volt Amperes Reactive"),
    KILOWATT("Real",1000.0,"KW", "Kilowatt"),
    POWERFACTOR("PowerFactor", 1.0,"PF","Power Factor");


    private final String metricName;
    private final double factor;
    private final String symbol;
    private final String unitName;

    //Constructor
    Metric( String metricName, double factor, String symbol, String unitName)
    {

        this.metricName = metricName;
        this.factor = factor;
        this.symbol = symbol;
        this.unitName = unitName;
    }

    //Getters
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