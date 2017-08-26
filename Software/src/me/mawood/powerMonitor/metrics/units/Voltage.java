package me.mawood.powerMonitor.metrics.units;

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
    public UnitType getType()
    {
        return UnitType.VOLTAGE;
    }
    @Override
    public String getName()
    {
        return name;
    }
    @Override
    public String getSymbol()
    {
        return symbol;
    }
}
