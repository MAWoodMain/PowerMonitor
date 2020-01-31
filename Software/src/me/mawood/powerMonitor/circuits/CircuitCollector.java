package me.mawood.powerMonitor.circuits;

import me.mawood.powerMonitor.metrics.InvalidDataException;
import me.mawood.powerMonitor.metrics.MetricDefinition;
import me.mawood.powerMonitor.metrics.MetricReading;
import me.mawood.powerMonitor.metrics.PowerMetricCalculator;
import me.mawood.powerMonitor.publishers.MQTTHandler;

import javax.naming.OperationNotSupportedException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;


public class CircuitCollector extends Thread
{
    MQTTHandler mqttHandler;

     // run control variables
    private final Map<Circuit, PowerMetricCalculator> circuitMap;
    LinkedBlockingQueue<String> loggingQ;

    /**
     * MQTTHandler   Constructor
     */
    public CircuitCollector(Map<Circuit, PowerMetricCalculator> circuitMap,
                            LinkedBlockingQueue<String> loggingQ,
                            MQTTHandler publisher
                            )
    {
        this.circuitMap = circuitMap;
        this.loggingQ = loggingQ;
        this.mqttHandler = publisher;
    }

    private void publishCircuitToBroker(Circuit circuit) throws InvalidDataException, OperationNotSupportedException
    {
        String subTopic= MQTTHandler.TOPIC + "/" + circuit.getDisplayName().replace(" ", "_");
        Instant readingTime =  Instant.now().minusSeconds(1);
        MetricReading voltage = circuitMap.get(HomeCircuits.WHOLE_HOUSE).getAverageBetween(MetricDefinition.VOLTS, Instant.now().minusSeconds(2), Instant.now().minusSeconds(1));
        MetricReading apparent = circuitMap.get(circuit).getAverageBetween(MetricDefinition.VA, Instant.now().minusSeconds(2), readingTime);
        MetricReading real = circuitMap.get(circuit).getAverageBetween(MetricDefinition.WATTS, Instant.now().minusSeconds(2), readingTime);
        MetricReading reactive = circuitMap.get(circuit).getAverageBetween(MetricDefinition.VAR, Instant.now().minusSeconds(2), readingTime);
        MetricReading current = circuitMap.get(circuit).getAverageBetween(MetricDefinition.AMPS, Instant.now().minusSeconds(2), readingTime);
        Double powerFactor = Math.round(real.getValue()/apparent.getValue()*1000000.0)/1000000.0;
        String jsonReadings =
                "{\"Time\":\""+readingTime.toString()+"\","+
                "\"Readings\":{"+
                "\"Voltage\":"+ voltage.getValue().toString()+","+
                "\"Real\":"+ real.getValue().toString()+","+
                "\"Apparent\":"+ apparent.getValue().toString()+","+
                "\"Reactive\":"+ reactive.getValue().toString()+","+
                "\"Current\":"+ current.getValue().toString()+","+
                "\"PowerFactor\":"+ powerFactor.toString()+
                "}}";
        mqttHandler.publishToBroker(subTopic,jsonReadings);

    }
    private void publishMetric(String subTopic, MetricReading metricReading)
    {
        metricReading.suppressNoise();
        mqttHandler.publishToBroker(subTopic,metricReading.toString());
    }

    //
    // Runnable implementation
    //

    /**
     * run  The main publishers loop
     */
    @Override
    public void run()
    {
        long startTime;
        try
        {
            // wait for first readings to be ready
            Thread.sleep(2010);
        } catch (InterruptedException ignored){}
        while (!Thread.interrupted())
        {
            startTime = System.currentTimeMillis();
            //rawMetricsBuffer.printMetricsBuffer();

            for (Circuit circuit : circuitMap.keySet())
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
                } catch (InterruptedException ignore){}
            }
        }
        loggingQ.add("CircuitCollector: Interrupted, exiting");
        System.exit(0);
    }
}
