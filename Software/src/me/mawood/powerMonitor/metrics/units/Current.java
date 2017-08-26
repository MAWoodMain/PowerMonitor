package me.mawood.powerMonitor.metrics.units;

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
    public UnitType getType()
    {
        return UnitType.CURRENT;
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
