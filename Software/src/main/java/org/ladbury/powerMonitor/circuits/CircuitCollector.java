package org.ladbury.powerMonitor.circuits;

import org.ladbury.powerMonitor.metrics.InvalidDataException;
import org.ladbury.powerMonitor.metrics.Metric;
import org.ladbury.powerMonitor.metrics.MetricReading;
import org.ladbury.powerMonitor.metrics.PowerMetricCalculator;
import org.ladbury.powerMonitor.publishers.MQTTHandler;

import javax.naming.OperationNotSupportedException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;


public class CircuitCollector extends Thread
{
    MQTTHandler mqttHandler;

    // run control variables
    private final Map<CircuitData, PowerMetricCalculator> circuitMap;
    private final LinkedBlockingQueue<String> loggingQ;
    private final HashMap<CircuitData,CircuitEnergyStore> storeMap = new HashMap<>();
    private int bucketIntervalMins;

    /**
     * MQTTHandler   Constructor
     */
    public CircuitCollector(Map<CircuitData, PowerMetricCalculator> circuitMap,
                            MQTTHandler publisher,
                            int bucketIntervalMins,
                            LinkedBlockingQueue<String> loggingQ
    )
    {
        this.circuitMap = circuitMap;
        this.loggingQ = loggingQ;
        this.mqttHandler = publisher;
        this.bucketIntervalMins = bucketIntervalMins;
        //this.energyStore = energyStore;
    }

    private void publishCircuitToBroker(CircuitData circuitData) throws InvalidDataException, OperationNotSupportedException
    {
        String subTopic = MQTTHandler.getTopic() + "/" + circuitData.getDisplayName().replace(" ", "_");
        Instant readingTime = Instant.now().minusSeconds(1);
        MetricReading voltage = circuitMap.get(HomeCircuits.CH9).getAverageBetween(Metric.VOLTS, Instant.now().minusSeconds(2), Instant.now().minusSeconds(1));
        MetricReading apparent = circuitMap.get(circuitData).getAverageBetween(Metric.VA, Instant.now().minusSeconds(2), readingTime);
        MetricReading real = circuitMap.get(circuitData).getAverageBetween(Metric.WATTS, Instant.now().minusSeconds(2), readingTime);

        CircuitEnergyStore circuitEnergyStore = storeMap.get(circuitData);
        if (circuitEnergyStore!=null) {circuitEnergyStore.accumulate(real.getValue());}
        else /*loggingQ.add("CircuitCollector: null circuitEnergyStore for - "+ circuitData.getDisplayName())*/;

        MetricReading reactive = circuitMap.get(circuitData).getAverageBetween(Metric.VAR, Instant.now().minusSeconds(2), readingTime);
        MetricReading current = circuitMap.get(circuitData).getAverageBetween(Metric.AMPS, Instant.now().minusSeconds(2), readingTime);
        //MetricReading powerfactor = circuitMap.get(circuitData).getAverageBetween(Metric.POWERFACTOR, Instant.now().minusSeconds(2), readingTime);
        Double powerFactor = Math.round(real.getValue()/apparent.getValue()*1000000.0)/1000000.0;
        String jsonReadings =
                "{\"Time\":\""+readingTime.toString()+"\","+
                "\"Readings\":{"+
                "\""+ voltage.getMetric().getMetricName()+"\":"+ voltage.getValue().toString()+","+
                "\""+ real.getMetric().getMetricName()+"\":"+ real.getValue().toString()+","+
                "\""+ apparent.getMetric().getMetricName()+"\":"+ apparent.getValue().toString()+","+
                "\""+ reactive.getMetric().getMetricName()+"\":"+ reactive.getValue().toString()+","+
                "\""+ current.getMetric().getMetricName()+"\":"+ current.getValue().toString()+","+
                //"\""+ powerfactor.getMetric().getMetricName()+"\":"+ current.getValue().toString()+","+
                "\"PowerFactor\":"+ powerFactor.toString()+
                "}}";
        mqttHandler.publishToBroker(subTopic,jsonReadings);
    }

    private void publishMetric(String subTopic, MetricReading metricReading)
    {
        metricReading.suppressNoise();
        mqttHandler.publishToBroker(subTopic, metricReading.toString());
    }

    public void publishEnergyMetricsForCircuits()
    {
        MetricReading energy;
        MetricReading cumulativeEnergy;
        for (CircuitData circuitData : circuitMap.keySet()) {
            String subTopic = MQTTHandler.getTopic() + "/" + circuitData.getDisplayName().replace(" ", "_");
            energy = storeMap.get(circuitData).getLatestEnergyMetric();
            cumulativeEnergy = storeMap.get(circuitData).getCumulativeEnergyForToday();
            String jsonReadings =
                    "{\"Time\":\"" + energy.getTimestamp().toString() + "\"," +
                            "\"Readings\":{" +
                            "\"Energy\":" + energy.getValue().toString() + ","+
                            "\"CumulativeEnergy\":" + cumulativeEnergy.getValue().toString()  +
                            "}}";
            mqttHandler.publishToBroker(subTopic, jsonReadings);
        }
    }
    public void fillAllEnergyBuckets(int bucketToFill)
    {
        //loggingQ.add("CircuitCollector: fill buckets storemap - " + storeMap.toString());
        for (CircuitData circuitData : circuitMap.keySet()) {
            //loggingQ.add("CircuitCollector: updateEnergyBucket for cct - "+ circuitData.getDisplayName());
            storeMap.get(circuitData).updateEnergyBucket(bucketToFill);
        }
    }
    public void resetAllEnergyBuckets()
    {
        for (CircuitData circuitData : circuitMap.keySet()) {
            storeMap.get(circuitData).resetAllEnergyData();
        }
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
        try {
            // wait for first readings to be ready
            Thread.sleep(2010);
        } catch (InterruptedException ignored) {
        }
        for (CircuitData circuitData : circuitMap.keySet()) {
            this.storeMap.put(circuitData,new CircuitEnergyStore(circuitData,bucketIntervalMins,loggingQ));
        }
        //loggingQ.add("CircuitCollector: storemap - " + storeMap.toString());

        while (!Thread.interrupted()) {
            startTime = System.currentTimeMillis();
            //rawMetricsBuffer.printMetricsBuffer();

            for (CircuitData circuitData : circuitMap.keySet()) {
                try {
                    publishCircuitToBroker(circuitData);
                } catch (InvalidDataException | OperationNotSupportedException e) {
                    loggingQ.add("no data for circuitData: " +
                            circuitData.getDisplayName() +
                            Arrays.toString(e.getStackTrace())
                    );
                }
            }

            //Frequency
            while (startTime + 1000 > System.currentTimeMillis()) {
                // wait half the remaining time
                try {
                    Thread.sleep(Math.max(0, ((startTime + 1000) - System.currentTimeMillis()) / 2));
                } catch (InterruptedException ignore) {
                }
            }
        }
        loggingQ.add("CircuitCollector: Interrupted, exiting");
        System.exit(0);
    }
}
