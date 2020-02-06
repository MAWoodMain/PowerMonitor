package org.ladbury.powerMonitor;

import com.beust.jcommander.JCommander;
import org.ladbury.powerMonitor.circuits.Circuit;
import org.ladbury.powerMonitor.circuits.CircuitCollector;
import org.ladbury.powerMonitor.circuits.EnergyBucketFiller;
import org.ladbury.powerMonitor.circuits.HomeCircuits;
import org.ladbury.powerMonitor.control.CommandProcessor;
import org.ladbury.powerMonitor.metrics.PowerMetricCalculator;
import org.ladbury.powerMonitor.packets.STM8PacketCollector;
import org.ladbury.powerMonitor.packets.monitors.CurrentMonitor;
import org.ladbury.powerMonitor.packets.monitors.RealPowerMonitor;
import org.ladbury.powerMonitor.packets.monitors.VoltageMonitor;
import org.ladbury.powerMonitor.packets.monitors.configs.VoltageSenseConfig;
import org.ladbury.powerMonitor.publishers.MQTTHandler;
import org.ladbury.powerMonitor.publishers.PMLogger;
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
    private static MQTTHandler mqttHandler;
    private static CommandProcessor commandProcessor;
    private static LinkedBlockingQueue<String> commandQ;
    private static LinkedBlockingQueue<String> loggingQ;
    private static PMLogger logger;
    private static CircuitCollector circuitCollector;
    private static EnergyBucketFiller bucketfiller;

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
    public static MQTTHandler getMqttHandler() {return mqttHandler;}
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

    public static void main(String[] argv) throws IOException
    {
        Args args = new Args();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        commandQ = new LinkedBlockingQueue<>();
        loggingQ = new LinkedBlockingQueue<>();
        int energyBucketInterval = 5; // Minutes

        //if required enable publishing processes
        if (isEnabled_MQTT()) {
            try {
                loggingQ.add("Enabling MQTT");
                mqttHandler = new MQTTHandler(getLoggingQ(), getCommandQ());
            } catch (MqttException e) {
                MQTTHandler.handleMQTTException(e);
                System.exit(9);
            }
        }

        // set up & start support processes
        logger = new PMLogger(loggingQ);
        logger.start();
        loggingQ.add("Enabled Logger");
        loggingQ.add("Enabling CommandProcessor");
        commandProcessor = new CommandProcessor(getCommandQ(),getLoggingQ());
        commandProcessor.start();

        loggingQ.add("Enabling CircuitCollector");
        boolean[] circuitRequired = {false, false, false, false, false, false, false, false, false, true}; // 0-9 0 not used, 9 is Whole_House
        circuitCollector = new CircuitCollector(getCircuitMap(), mqttHandler,5,getLoggingQ());


        loggingQ.add("Enabling EnergyBucketFiller");
        bucketfiller = new EnergyBucketFiller(energyBucketInterval,true,circuitCollector,getLoggingQ());
        bucketfiller.start();

        // Start packet collection
        loggingQ.add("Enabling PacketCollector");
        packetCollector = new STM8PacketCollector(1000);
        //packetCollector.addPacketEventListener(System.out::println);
        loggingQ.add("Enabling VoltageMonitor ");
        vm = new VoltageMonitor(1000, VoltageSenseConfig.UK9V, packetCollector);
        // Enable interpretation for required circuits
        for(Circuit circuit: HomeCircuits.values())
        {
            if (circuitRequired[circuit.getChannelNumber()])
                enableCollection(circuit);
        }
        circuitCollector.start();
    }
}
