package me.mawood.powerMonitor;

import me.mawood.powerMonitor.circuits.Circuit;
import me.mawood.powerMonitor.circuits.HomeCircuits;
import me.mawood.powerMonitor.control.CommandProcessor;
import me.mawood.powerMonitor.metrics.PowerMetricCalculator;
import me.mawood.powerMonitor.packets.STM8PacketCollector;
import me.mawood.powerMonitor.packets.monitors.CurrentMonitor;
import me.mawood.powerMonitor.packets.monitors.RealPowerMonitor;
import me.mawood.powerMonitor.packets.monitors.VoltageMonitor;
import me.mawood.powerMonitor.packets.monitors.configs.VoltageSenseConfig;
import me.mawood.powerMonitor.publishers.PMLogger;
import me.mawood.powerMonitor.publishers.PowerDataAPIPublisher;
import me.mawood.powerMonitor.publishers.PowerDataMQTTPublisher;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Main
{
    private static boolean enable_MQTT = true;
    private static boolean enable_API = false;
    private static HashMap<Circuit, PowerMetricCalculator> circuitMap = new HashMap<>();
    private static VoltageMonitor vm;
    private static STM8PacketCollector packetCollector;
    private static PowerDataMQTTPublisher powerDataMQTTPublisher;
    private static PowerDataAPIPublisher powerDataDataBaseUpdater;
    private static CommandProcessor commandProcessor;
    private static LinkedBlockingQueue<String> commandQ;
    private static LinkedBlockingQueue<String> loggingQ;
    private static PMLogger logger;
    // Getters and Setters
    public static boolean isEnabled_MQTT()
    {
        return enable_MQTT;
    }
    public static void enable_MQQT()
    {
        enable_MQTT = true;
    }
    public static void disable_MQQT()
    {
        enable_MQTT = false;
    }

    public static boolean isEnabled_API()
    {
        return enable_API;
    }
    public static void enable_API()
    {
        enable_API = true;
    }
    public static void disable_API()
    {
        enable_API = false;
    }

    public static HashMap<Circuit, PowerMetricCalculator> getCircuitMap() {return circuitMap;}
    public static PowerDataMQTTPublisher getPowerDataMQTTPublisher() {return powerDataMQTTPublisher;}
    public static PowerDataAPIPublisher getPowerDataDataBaseUpdater() {return powerDataDataBaseUpdater;}
    public static LinkedBlockingQueue<String>  getCommandQ() {return commandQ;}
    public static LinkedBlockingQueue<String>  getLoggingQ() {return loggingQ;}

    public static void enableCollection(Circuit circuit)
    {
        circuitMap.put(
                circuit,
                new PowerMetricCalculator(vm,
                new CurrentMonitor(1000, circuit.getClampConfig(), circuit.getChannelNumber(), packetCollector),
                new RealPowerMonitor(1000, VoltageSenseConfig.UK9V, circuit.getClampConfig(), circuit.getChannelNumber(), packetCollector)));
        loggingQ.add("Monitoring circuit "+circuit.getDisplayName());
    }
    public static void disableCollection(Circuit circuit)
    {
        circuitMap.remove(circuit);
        loggingQ.add("Not monitoring circuit "+circuit.getDisplayName());
    }

    public static void main(String[] args) throws IOException
    {
        commandQ = new LinkedBlockingQueue<>();
        loggingQ = new LinkedBlockingQueue<>();

        //if required enable publishing processes
        if (enable_MQTT) {
            try {
                powerDataMQTTPublisher = new PowerDataMQTTPublisher(circuitMap,loggingQ, commandQ);
                powerDataMQTTPublisher.start(); // run in separate thread
                loggingQ.add("Enabled MQTT");
            } catch (MqttException e) {
                PowerDataMQTTPublisher.handleMQTTException(e);
                System.exit(9);
            }
        }
        if (enable_API) {
            powerDataDataBaseUpdater = new PowerDataAPIPublisher(circuitMap);
            powerDataDataBaseUpdater.start();
            loggingQ.add("Enabled API");
        }

        // set up & start support processes
        logger = new PMLogger(loggingQ);
        logger.start();
        loggingQ.add("Enabled Logger");
        commandProcessor = new CommandProcessor(commandQ,loggingQ);
        commandProcessor.start();
        loggingQ.add("Enabled CommandProcessor");

        // Start packet collection
        packetCollector = new STM8PacketCollector(1000);
        //packetCollector.addPacketEventListener(System.out::println);
        vm = new VoltageMonitor(1000, VoltageSenseConfig.UK9V, packetCollector);
        loggingQ.add("Enabled PacketCollector and VoltageMonitor ");
        // Enable interpretation for required circuits
        boolean[] circuitRequired = {false, false, false, false, false, false, false, false, false, true}; // 0-9 0 not used, 9 is Whole_House
        for(Circuit circuit: HomeCircuits.values())
        {
            if (circuitRequired[circuit.getChannelNumber()])
                enableCollection(circuit);
        }
    }
}
