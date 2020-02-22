package org.ladbury.powerMonitor;

import com.beust.jcommander.JCommander;
import org.ladbury.powerMonitor.circuits.*;
import org.ladbury.powerMonitor.control.CommandProcessor;
import org.ladbury.powerMonitor.currentClamps.Clamps;
import org.ladbury.powerMonitor.metrics.PowerMetricCalculator;
import org.ladbury.powerMonitor.packets.STM8PacketCollector;
import org.ladbury.powerMonitor.monitors.CurrentMonitor;
import org.ladbury.powerMonitor.monitors.RealPowerMonitor;
import org.ladbury.powerMonitor.monitors.VoltageMonitor;
import org.ladbury.powerMonitor.monitors.configs.VoltageSenseConfig;
import org.ladbury.powerMonitor.publishers.MQTTHandler;
import org.ladbury.powerMonitor.publishers.PMLogger;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Main
{

    private static final HashMap<Circuit, PowerMetricCalculator> circuitMap = new HashMap<>();
    private static VoltageMonitor vm;
    private static STM8PacketCollector packetCollector;
    private static MQTTHandler mqttHandler;
    private static LinkedBlockingQueue<String> commandQ = new LinkedBlockingQueue<>();
    private static LinkedBlockingQueue<String> loggingQ = new LinkedBlockingQueue<>();
    private static final Circuits circuits= new Circuits();
    private static final Clamps clamps = new Clamps();
    private static CircuitCollector circuitCollector;

    // Getters
    public static HashMap<Circuit, PowerMetricCalculator> getCircuitMap()
    {
        return circuitMap;
    }
    public static MQTTHandler getMqttHandler()
    {
        return mqttHandler;
    }
    public static LinkedBlockingQueue<String> getCommandQ()
    {
        return commandQ;
    }
    public static LinkedBlockingQueue<String> getLoggingQ()
    {
        return loggingQ;
    }
    public static Circuits getCircuits(){return circuits;}
    public static Clamps getClamps(){return clamps;}
    public static CircuitCollector getCircuitCollector() {return circuitCollector;}
    //Setters
    public static void enableCollection(Circuit circuit)
    {
        circuits.setMonitoring(circuit.getChannelNumber(),true);
        circuitMap.put(
                circuit,
                new PowerMetricCalculator(vm,
                        new CurrentMonitor(1000,
                                            clamps.getClamp(circuit.getClampName()),
                                            circuit.getChannelNumber(),
                                            packetCollector),
                        new RealPowerMonitor(1000,
                                            VoltageSenseConfig.UK9V,
                                            clamps.getClamp(circuit.getClampName()),
                                            circuit.getChannelNumber(),
                                            packetCollector)));
        loggingQ.add("Monitoring circuitData " + circuit.getDisplayName());
    }

    public static void disableCollection(Circuit circuit)
    {
        circuits.setMonitoring(circuit.getChannelNumber(),false);
        circuitMap.remove(circuit);
        loggingQ.add("Not monitoring circuitData " + circuit.getDisplayName());
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static void help()
    {
        System.out.println( "Energy Monitor Help");
        System.out.println();
        System.out.println(" The following parameters may be entered on the command line:");
        System.out.println("--mqqtserver or -m  This switch must be followed by an IP address for the MQTT server");
        System.out.println("--ClientName or -c  This switch must be followed by a client name for this monitor");
        System.out.println("--interval or -i  This switch must be followed by an integer number of minutes over which time energy is to be accumulated ");
        System.out.println( "--help or -h  This switch causes this help to be displayed and the program terminates" );
        System.out.println( "Any or all of the parameters may be omitted, in which case hard coded values will be used (see code)");
    }

    public static void main(String[] argv) throws IOException
    {
        //Initialise variables
        int energyBucketInterval = 5; // Minutes

        //Handle arguments
        Args args = new Args();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        if (args.isHelp())
        {
            help();
            System.exit(0);
        }
        if (args.getAccumulationInterval()>0){energyBucketInterval = args.getAccumulationInterval();}
        //if required enable publishing processes
        try {
            loggingQ.add("Enabling MQTT");
            mqttHandler = new MQTTHandler(args.getMqttServer(), args.getClientName(), getLoggingQ(), getCommandQ());
        } catch (MqttException e) {
            MQTTHandler.handleMQTTException(e);
            System.exit(9);
        }

        // set up & start support processes
        PMLogger logger = new PMLogger(loggingQ);
        logger.start();
        loggingQ.add("Enabled Logger");
        loggingQ.add("Enabling CommandProcessor");
        CommandProcessor commandProcessor = new CommandProcessor(getCommandQ(), getLoggingQ());
        commandProcessor.start();

        loggingQ.add("Enabling CircuitCollector");
        CircuitCollector circuitCollector = new CircuitCollector(getCircuitMap(), mqttHandler, 5, getLoggingQ());

        loggingQ.add("Enabling EnergyBucketFiller");
        EnergyBucketFiller bucketFiller = new EnergyBucketFiller(energyBucketInterval, true, circuitCollector, getLoggingQ());
        bucketFiller.start();

        // Start packet collection
        loggingQ.add("Enabling PacketCollector");
        packetCollector = new STM8PacketCollector(1000);
        //packetCollector.addPacketEventListener(System.out::println);
        loggingQ.add("Enabling VoltageMonitor ");
        vm = new VoltageMonitor(1000, VoltageSenseConfig.UK9V, packetCollector);
        // Enable interpretation for required circuits
        for( Circuit circuit : circuits.getCircuits()){
            if (circuit.isMonitored())
                enableCollection(circuit);
        }
        circuitCollector.start();
    }
}
