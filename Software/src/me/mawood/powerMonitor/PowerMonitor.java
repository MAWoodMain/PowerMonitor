package me.mawood.powerMonitor;

public interface PowerMonitor
{
    MetricsBuffer getRawMetricsBuffer();
    MetricsBuffer getAndResetRawMetricsBuffer();
}
