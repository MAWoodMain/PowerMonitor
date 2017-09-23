package me.mawood.powerMonitor.processing;

import me.mawood.powerMonitor.circuits.Circuits;
import me.mawood.powerMonitor.metrics.InvalidDataException;
import me.mawood.powerMonitor.metrics.Metric;
import me.mawood.powerMonitor.metrics.PowerMetricCalculator;
import me.mawood.powerMonitor.metrics.units.Power;
import me.mawood.powerMonitor.metrics.units.Voltage;

import javax.naming.OperationNotSupportedException;
import java.time.Instant;
import java.util.Map;

public class PowerDataDataBaseUpdater extends Thread
{
    private class ChannelMap
    {
        final int channelNumber;
        final double scaleFactor;
        final String units;
        final String name;

        private ChannelMap(int channelNumber, double scaleFactor, String units, String name)
        {
            this.channelNumber = channelNumber;
            this.scaleFactor = scaleFactor;
            this.units = units;
            this.name = name;
        }
    }
    private static final String USERNAME = "emonpi";
    private static final String PASSWORD = "emonpimqtt2016";

    private long noMessagesSentOK;

    // run control variables
    private volatile boolean msgArrived;
    private final Map<Circuits, PowerMetricCalculator> circuitMap;

    /**
     * PowerDataMQTTPublisher   Constructor
     */
    public PowerDataDataBaseUpdater(Map<Circuits, PowerMetricCalculator> circuitMap)
    {
        this.circuitMap = circuitMap;
        noMessagesSentOK = 0;
        //connect to DB
        System.out.println("PowerDataDataBaseUpdater Connected");
    }

    /**
     * shutdownDataProcessing   Tidy shutdown of the processor
     */
    private void shutdownDataProcessing()
    {
        System.out.println("PowerDataDataBaseUpdate disconnected from DB");
        System.out.println(noMessagesSentOK + " messages sent successfully");
    }

    /**
     * publishToBroker - send a message to the MQTT broker
     *
     * @param subTopic - fully qualified TOPIC identifier
     * @param content  - message content "key data"
     */
    private void publishToBroker(String subTopic, String content)
    {
    }


    private void publishMetricToBroker(String subTopic, Metric metric)
    {

    }

    private void publishCircuitToBroker(Circuits circuit) throws InvalidDataException, OperationNotSupportedException
    {
        String subTopic;
        subTopic =  "/" + circuit.getDisplayName().replace(" ", "_");
        Metric apparent = circuitMap.get(circuit).getAverageBetween(Power.VA, Instant.now().minusSeconds(2), Instant.now().minusSeconds(1));
        publishMetricToBroker(subTopic + "/ApparentPower", apparent);
        Metric real = circuitMap.get(circuit).getAverageBetween(Power.WATTS, Instant.now().minusSeconds(2), Instant.now().minusSeconds(1));
        publishMetricToBroker(subTopic + "/RealPower", real);
        if (subTopic.contains("Whole_House"))
        {
            Metric voltage = circuitMap.get(circuit).getAverageBetween(Voltage.VOLTS, Instant.now().minusSeconds(2), Instant.now().minusSeconds(1));
            publishMetricToBroker(subTopic + "/Voltage", voltage);
        }
    }

    //
    // Runnable implementation
    //

    /**
     * run  The main processing loop
     */
    @Override
    public void run()
    {
        String subTopic;
        long startTime;
        try
        {
            // wait for first readings to be ready
            Thread.sleep(2010);
        } catch (InterruptedException ignored)
        {
        }
        while (!Thread.interrupted())
        {
            startTime = System.currentTimeMillis();
            //rawMetricsBuffer.printMetricsBuffer();

            for (Circuits circuit : circuitMap.keySet())
            {

                try
                {
                    publishCircuitToBroker(circuit);
                } catch (InvalidDataException | OperationNotSupportedException e)
                {
                    //System.out.println("no data for circuit: " + circuit.getDisplayName());
                }
            }


            //Frequency
            while (startTime + 1000 > System.currentTimeMillis())
            {
                // wait half the remaining time
                try
                {
                    Thread.sleep(Math.max(0, ((startTime + 1000) - System.currentTimeMillis()) / 2));
                } catch (InterruptedException ignore)
                {
                }
            }
        }
        shutdownDataProcessing();
        System.out.println("Data Processing Interrupted, exiting");
        System.exit(0);
    }
}
