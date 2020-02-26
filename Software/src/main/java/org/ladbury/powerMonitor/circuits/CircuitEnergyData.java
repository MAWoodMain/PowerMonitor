package org.ladbury.powerMonitor.circuits;

import org.ladbury.powerMonitor.Main;

import java.time.Instant;

public class CircuitEnergyData
{
    String device;
    int channel;
    String circuitTag;
    String time;
    CircuitEnergyReadings readings;
    CircuitEnergyData()
    {
        device = Main.getMqttHandler().getClientID();
        channel = -1;
        circuitTag = "";
        time = Instant.now().toString();
        readings = new CircuitEnergyReadings();
    }
    public CircuitEnergyData(Circuit circuit)
    {
        device = Main.getMqttHandler().getClientID();
        channel = circuit.getChannelNumber();
        circuitTag = circuit.getDisplayName();
        time = Instant.now().toString();
        readings = new CircuitEnergyReadings();
    }
}
