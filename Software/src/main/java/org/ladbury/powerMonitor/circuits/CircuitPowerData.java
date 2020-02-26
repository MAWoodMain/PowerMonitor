package org.ladbury.powerMonitor.circuits;

import org.ladbury.powerMonitor.Main;
import java.time.Instant;

public class CircuitPowerData
{
    String device;
    int channel;
    String circuitTag;
    String time;
    CircuitPowerReadings readings;
    CircuitPowerData()
    {
        device = Main.getMqttHandler().getClientID();
        channel = -1;
        circuitTag = "";
        time = Instant.now().toString();
        readings = new CircuitPowerReadings();
    }
    public CircuitPowerData(Circuit circuit)
    {
        device = Main.getMqttHandler().getClientID();
        channel = circuit.getChannelNumber();
        circuitTag = circuit.getTag();
        time = Instant.now().toString();
        readings = new CircuitPowerReadings();
    }
}
