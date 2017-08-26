package me.mawood.powerMonitor.metrics.units;

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
    public UnitType getType()
    {
        return UnitType.POWER;
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
