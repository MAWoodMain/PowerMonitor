package me.mawood.powerMonitor;

import me.mawood.powerMonitor.circuits.Circuits;
import me.mawood.powerMonitor.circuits.HomeCircuits;
import me.mawood.powerMonitor.metrics.PowerMetricCalculator;
import me.mawood.powerMonitor.processing.PowerDataMQTTPublisher;
import me.mawood.powerMonitor.packets.monitors.CurrentMonitor;
import me.mawood.powerMonitor.packets.monitors.RealPowerMonitor;
import me.mawood.powerMonitor.packets.monitors.VoltageMonitor;
import me.mawood.powerMonitor.packets.monitors.configs.VoltageSenseConfig;
import me.mawood.powerMonitor.packets.STM8PacketCollector;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.util.HashMap;

public class Main
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        STM8PacketCollector packetCollector = new STM8PacketCollector(1000);
        VoltageMonitor vm = new VoltageMonitor(1000, VoltageSenseConfig.UK9V, packetCollector);

        HashMap<Circuits, PowerMetricCalculator> circuitMap = new HashMap<>();

        for(Circuits circuit: HomeCircuits.values())
        {
            circuitMap.put(circuit, new PowerMetricCalculator(vm,
                    new CurrentMonitor(1000, circuit.getClampConfig(), circuit.getChannelNumber(), packetCollector),
                    new RealPowerMonitor(1000, VoltageSenseConfig.UK9V, circuit.getClampConfig(), circuit.getChannelNumber(), packetCollector)));
        }
        PowerDataMQTTPublisher pdp;
        try
        {
            pdp = new PowerDataMQTTPublisher(circuitMap);
            pdp.start(); // run in separate thread
        } catch (MqttException e)
        {
            PowerDataMQTTPublisher.handleMQTTException(e);
            System.exit(9);
        }
    }
}
