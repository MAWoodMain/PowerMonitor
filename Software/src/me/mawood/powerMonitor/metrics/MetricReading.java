package me.mawood.powerMonitor.metrics;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MetricReading implements Comparable<MetricReading>
{
    private static final double NOISE_FILTER = 2d; // ignore metrics whose absolute value is smaller than this

    private double value;
    private final Instant timestamp;
    // Using generics to facilitate unit conversion later.
    private final Metric unit;

    public MetricReading(double value, Instant timestamp, Metric unit)
    {
        this.value = value;
        this.timestamp = timestamp;
        this.unit = unit;
    }
    public Double getValue()
    {
        return Math.round(value * 1000000.0) / 1000000.0; // round to 6 decimals
    }

    public Instant getTimestamp()
    {
        return timestamp;
    }

    public Metric getUnit()
    {
        return unit;
    }

    @Override
    public String toString()
    {
        final DateTimeFormatter formatter =
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        .withLocale( Locale.UK )
                        .withZone( ZoneId.systemDefault() );
        //System.out.println(String.format("MetricReading: {%.03f %s at %s}", value,unit.getSymbol(), formatter.format(timestamp)));
        return String.format("%.03f %s at %s", value,unit.getSymbol(), formatter.format(timestamp));
    }
    @Override
    public int compareTo(MetricReading o)
    {
        return (int)(timestamp.toEpochMilli() - o.timestamp.toEpochMilli());
    }
    public void suppressNoise()
    {
        if (Math.abs(value) < NOISE_FILTER) value = 0d;

    }
}
