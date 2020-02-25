package org.ladbury.powerMonitor.circuits;

import java.time.Instant;

public class CircuitEnergyData
{
    public int channel;
    public String circuitName;
    public String time;
    public CircuitEnergyReadings readings;
    public CircuitEnergyData()
    {
        channel = -1;
        circuitName = "";
        time = Instant.now().toString();
        readings = new CircuitEnergyReadings();
    }
}
