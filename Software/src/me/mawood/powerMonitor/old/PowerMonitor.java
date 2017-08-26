package me.mawood.powerMonitor.old;

public interface PowerMonitor
{
    MetricsBuffer getRawMetricsBuffer();
    MetricsBuffer getAndResetRawMetricsBuffer();
}
