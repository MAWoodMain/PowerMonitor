package me.mawood.powerMonitor;

import me.mawood.powerMonitor.circuits.Circuit;
import me.mawood.powerMonitor.circuits.HomeCircuits;
import me.mawood.powerMonitor.metrics.PowerMetricCalculator;
import me.mawood.powerMonitor.packets.STM8PacketCollector;
import me.mawood.powerMonitor.packets.monitors.CurrentMonitor;
import me.mawood.powerMonitor.packets.monitors.RealPowerMonitor;
import me.mawood.powerMonitor.packets.monitors.VoltageMonitor;
import me.mawood.powerMonitor.packets.monitors.configs.VoltageSenseConfig;
import me.mawood.powerMonitor.publishers.PowerDataAPIPublisher;
import me.mawood.powerMonitor.publishers.PowerDataMQTTPublisher;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class Main
{
    static boolean enable_MQTT = true;
    static boolean enable_API = false;
    static boolean[] enableCircuits = new boolean[9];

    public static boolean isEnable_MQTT()
    {
        return enable_MQTT;
    }

    public static boolean isEnable_API()
    {
        return enable_API;
    }

    public void setEnable_MQQT(boolean mqtt)
    {
        enable_MQTT = mqtt;
    }

    public void setEnable_API(boolean api)
    {
        enable_API = api;
    }

    public static void main(String[] args) throws IOException
    {
        Arrays.fill(enableCircuits, Boolean.FALSE);
        enableCircuits[8]=true; // switch connection for whole house on
        enableCircuits[6]=true; // 20 amp coil
        STM8PacketCollector packetCollector = new STM8PacketCollector(1000);
        //packetCollector.addPacketEventListener(System.out::println);
        VoltageMonitor vm = new VoltageMonitor(1000, VoltageSenseConfig.UK9V, packetCollector);

        HashMap<Circuit, PowerMetricCalculator> circuitMap = new HashMap<>();

        for(Circuit circuit: HomeCircuits.values())
        {
            if (enableCircuits[circuit.getChannelNumber()-1]) { //only monitor enabled circuits
                circuitMap.put(circuit, new PowerMetricCalculator(vm,
                        new CurrentMonitor(1000, circuit.getClampConfig(), circuit.getChannelNumber(), packetCollector),
                        new RealPowerMonitor(1000, VoltageSenseConfig.UK9V, circuit.getClampConfig(), circuit.getChannelNumber(), packetCollector)));
            }
        }
        if (enable_API) {
            PowerDataAPIPublisher powerDataDataBaseUpdater = new PowerDataAPIPublisher(circuitMap);
            powerDataDataBaseUpdater.start();
        }
        if (enable_MQTT) {
            PowerDataMQTTPublisher powerDataMQTTPublisher;
            try {
                powerDataMQTTPublisher = new PowerDataMQTTPublisher(circuitMap);
                powerDataMQTTPublisher.start(); // run in separate thread
            } catch (MqttException e) {
                PowerDataMQTTPublisher.handleMQTTException(e);
                System.exit(9);
            }
        }
    }
}
