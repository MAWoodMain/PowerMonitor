package org.ladbury.powerMonitor;

import com.beust.jcommander.JCommander;
import org.ladbury.powerMonitor.PMhealth.MemoryMonitor;
import org.ladbury.powerMonitor.circuits.*;
import org.ladbury.powerMonitor.control.CommandProcessor;
import org.ladbury.powerMonitor.currentClamps.Clamps;
import org.ladbury.powerMonitor.publishers.MQTTHandler;
import org.ladbury.powerMonitor.publishers.PMLogger;
import org.eclipse.paho.client.mqttv3.MqttException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public class Main
{
    private static MQTTHandler mqttHandler;
    private static final LinkedBlockingQueue<String> commandQ = new LinkedBlockingQueue<>();
    private static final Circuits circuits= new Circuits();
    private static final Clamps clamps = new Clamps();
    private static CircuitCollector circuitCollector;
    private static MemoryMonitor memoryMonitor;
    private static PMLogger logger;
    private static Level loggingLevel;

    // Getters
    public static MemoryMonitor getMemoryMonitor(){return memoryMonitor;}
    public static MQTTHandler getMqttHandler()
    {
        return mqttHandler;
    }
    public static LinkedBlockingQueue<String> getCommandQ()
    {
        return commandQ;
    }
    public static Circuits getCircuits(){return circuits;}
    public static Clamps getClamps(){return clamps;}
    public static CircuitCollector getCircuitCollector() {return circuitCollector;}
    public static PMLogger getLogger() {return logger;}

    @SuppressWarnings("SpellCheckingInspection")
    private static void help()
    {
        System.out.println( "Energy Monitor Help");
        System.out.println();
        System.out.println(" The following parameters may be entered on the command line:");
        System.out.println("--mqqtserver or -m  This switch must be followed by an IP address for the MQTT server");
        System.out.println("--ClientName or -c  This switch must be followed by a client name for this monitor");
        System.out.println("--interval or -i  This switch must be followed by an integer number of minutes over which time energy is to be accumulated");
        System.out.println("--LoggingLevel or -l  This switch must be followed by a valid logging level string such as   fine, config, info, warning, severe");
        System.out.println( "--help or -h  This switch causes this help to be displayed and the program terminates" );
        System.out.println( "Any or all of the parameters may be omitted, in which case hard coded values will be used (see code)");
    }

    public static void main(String[] argv)
    {
        //Initialise variables
        int energyAccumulationIntervalMins = 5; // Minutes
        long samplingIntervalMilliSeconds = 1000; // Milliseconds
        logger = new PMLogger(Level.INFO);
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
        if (args.getAccumulationInterval()>0){energyAccumulationIntervalMins = args.getAccumulationInterval();}
        try {
            //logger.add("Enabling MQTT", Level.INFO, Main.class.getName());
            mqttHandler = new MQTTHandler(args.getMqttServer(), args.getClientName(), getCommandQ());
        } catch (MqttException e) {
            MQTTHandler.handleMQTTException(e);
            System.exit(9);
        }
        if (args.getLoggingLevelStr()!=null)
        {
            Level l;
            try {
                l = Level.parse(args.getLoggingLevelStr().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("illegal logging level");
                l = Level.INFO;
                System.exit(10);
            }
            logger.setLoggingLevel(l);
        }
        // set up & start support processes
        logger.start();
        logger.add("Enabled Logger", Level.INFO,Main.class.getName());
        logger.add("Enabling CommandProcessor", Level.FINE, Main.class.getName());
        memoryMonitor = new MemoryMonitor(5);
        logger.add("#"+getMemoryMonitor().getTotalHeapSize()+"#Initial heap size", Level.INFO, Main.class.getName());
        logger.add("#"+getMemoryMonitor().getCurrentHeapSize()+"#current heap size start of main", Level.INFO,Main.class.getName());
        CommandProcessor commandProcessor = new CommandProcessor(getCommandQ());
        commandProcessor.start();

        logger.add("Enabling CircuitCollector", Level.FINE, Main.class.getName());
        circuitCollector = new CircuitCollector(samplingIntervalMilliSeconds, energyAccumulationIntervalMins);
        //circuits has initial values set to monitor the whole house but no other circuits
        //additional circuits can be set to monitor via the mqtt commands
        for( Circuit circuit : circuits.getCircuits()){
            if (circuit.isMonitored())
                circuitCollector.enableCollection(circuit);
                circuit.setPublishPower(true);
                circuit.setPublishEnergy(true);
        }
        circuitCollector.start();
        logger.add("#"+getMemoryMonitor().getCurrentHeapSize()+"#Heap used after Main", Level.INFO, Main.class.getName());
        Runtime.getRuntime().gc();
    }
}
