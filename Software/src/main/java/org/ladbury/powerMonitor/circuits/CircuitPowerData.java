package org.ladbury.powerMonitor.circuits;

import java.time.Instant;

public class CircuitPowerData
{
    public int channel;
    public String circuitName;
    public String time;
    public CircuitPowerReadings readings;
    public CircuitPowerData()
    {
        channel = -1;
        circuitName = "";
        time = Instant.now().toString();
        readings = new CircuitPowerReadings();
    }
}
