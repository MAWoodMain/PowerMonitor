package org.ladbury.powerMonitor.circuits;

import java.time.Instant;

public class CircuitPowerData
{
    public int channel;
    public String circuitTag;
    public String time;
    public CircuitPowerReadings readings;
    public CircuitPowerData()
    {
        channel = -1;
        circuitTag = "";
        time = Instant.now().toString();
        readings = new CircuitPowerReadings();
    }
    public CircuitPowerData(Circuit circuit)
    {
        channel = circuit.getChannelNumber();
        circuitTag = circuit.getDisplayName();
        time = Instant.now().toString();
        readings = new CircuitPowerReadings();
    }

}
