package org.ladbury.powerMonitor.circuits;

class CircuitPowerReadings
{
    Double voltage;
    Double current;
    Double realPower;
    Double reactivePower;
    Double apparentPower;
    Double powerFactor;

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
