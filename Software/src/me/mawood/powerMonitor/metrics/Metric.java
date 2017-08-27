package me.mawood.powerMonitor.metrics;

import me.mawood.powerMonitor.metrics.units.Unit;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class Metric<T extends Unit>
{
    private final double value;
    private final Instant timestamp;
    // Using generics to facilitate unit conversion later.
    private final T unit;

    public Metric(double value,Instant timestamp, T unit)
    {
        this.value = value;
        this.timestamp = timestamp;
        this.unit = unit;
    }
    public double getValue()
    {
        return value;
    }

    public Instant getTimestamp()
    {
        return timestamp;
    }

    @Override
    public String toString()
    {
        final DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime( FormatStyle.MEDIUM )
                        .withLocale( Locale.UK )
                        .withZone( ZoneId.systemDefault() );
        return String.format("Metric: {%.03f %s at %s}", value,unit.getSymbol(), formatter.format(timestamp));
    }
}
