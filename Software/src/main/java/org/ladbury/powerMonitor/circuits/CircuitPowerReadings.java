package org.ladbury.powerMonitor.circuits;

public class CircuitPowerReadings
{
    public Double voltage;
    public Double current;
    public Double realPower;
    public Double reactivePower;
    public Double apparentPower;
    public Double powerFactor;
    public Double energy;

    public CircuitPowerReadings()
    {
        voltage = 0.0;
        current = 0.0;
        realPower = 0.0;
        reactivePower = 0.0;
        apparentPower = 0.0;
        powerFactor = 0.0;
    }
}
