package me.mawood.powerMonitor.metrics.units;

public enum Energy implements Unit
{
    WATT_HOURS("WH", "Watt Hours"),
    KILOWATT_HOURS("kWH", "KiloWatt Hours");

    final String symbol;
    final String name;

    Energy(String symbol, String name)
    {
        this.symbol = symbol;
        this.name = name;
    }

    @Override
    public UnitType getType()
    {
        return UnitType.ENERGY;
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
