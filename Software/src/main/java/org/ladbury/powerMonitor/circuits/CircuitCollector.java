package org.ladbury.powerMonitor.circuits;

import org.ladbury.powerMonitor.metrics.*;
import org.ladbury.powerMonitor.publishers.MQTTHandler;

import javax.naming.OperationNotSupportedException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;


public class CircuitCollector extends Thread
{
    final MQTTHandler mqttHandler;

    // run control variables
    private final Map<Circuit, PowerMetricCalculator> circuitMap;
    private final LinkedBlockingQueue<String> loggingQ;
    private final HashMap<Circuit, CircuitEnergyStore> storeMap = new HashMap<>();
    private final int bucketIntervalMins;

    /**
     * MQTTHandler   Constructor
     */
    public CircuitCollector(Map<Circuit, PowerMetricCalculator> circuitMap,
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

    public MetricReading getLatestMetricReading(Circuit circuit, Metric metric)
    {
        try {
            return circuitMap.get(circuit).getLatestMetric(metric);
        } catch (OperationNotSupportedException e) {
            System.out.println("getLatestMetric not supported for " + metric.toString());
        }
        return new MetricReading(0.0, Instant.now(),metric);
    }
    public CircuitData getCircuitData(Circuit circuit)
    {
        CircuitData circuitData = new CircuitData();
        Instant readingTime = Instant.now().minusSeconds(1);
        Instant readingTimeMinusInterval = readingTime.minusSeconds(1);
        circuitData.channel = circuit.getChannelNumber();
        circuitData.circuitName = circuit.getDisplayName();
        circuitData.time = readingTime.toString();
        try {
            circuitData.voltage = circuitMap.get(circuit).getAverageBetween(Metric.VOLTS, readingTimeMinusInterval, readingTime).getValue();
            circuitData.current = circuitMap.get(circuit).getAverageBetween(Metric.AMPS, readingTimeMinusInterval, readingTime).getValue();
            circuitData.realPower = circuitMap.get(circuit).getAverageBetween(Metric.WATTS, readingTimeMinusInterval, readingTime).getValue();
            circuitData.apparentPower = circuitMap.get(circuit).getAverageBetween(Metric.VA, readingTimeMinusInterval, readingTime).getValue();
            circuitData.reactivePower = circuitMap.get(circuit).getAverageBetween(Metric.VAR, readingTimeMinusInterval, readingTime).getValue();
            circuitData.powerFactor = circuitMap.get(circuit).getAverageBetween(Metric.POWERFACTOR, readingTimeMinusInterval, readingTime).getValue();

            CircuitEnergyStore circuitEnergyStore = storeMap.get(circuit);
            if (circuitEnergyStore != null) {
                circuitData.energy = circuitEnergyStore.getLatestEnergyMetric().getValue(); //TODO select for time period
            } else loggingQ.add("CircuitCollector: null circuitEnergyStore for - " + circuit.getDisplayName());

            return circuitData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return circuitData; //this may be partially complete if some data wash't available
    }

    private void publishCircuitToBroker(Circuit circuit) throws InvalidDataException, OperationNotSupportedException
    {
        String subTopic = mqttHandler.getTopic() + "/" + circuit.getTag();
        Instant readingTime = Instant.now().minusSeconds(1);
        Instant readingTimeMinusInterval = readingTime.minusSeconds(1);
        MetricReading voltage = circuitMap.get(circuit).getAverageBetween(Metric.VOLTS, readingTimeMinusInterval, readingTime);
        MetricReading apparent = circuitMap.get(circuit).getAverageBetween(Metric.VA, readingTimeMinusInterval, readingTime);
        MetricReading real = circuitMap.get(circuit).getAverageBetween(Metric.WATTS, readingTimeMinusInterval, readingTime);
        MetricReading reactive = circuitMap.get(circuit).getAverageBetween(Metric.VAR, readingTimeMinusInterval, readingTime);
        MetricReading current = circuitMap.get(circuit).getAverageBetween(Metric.AMPS, readingTimeMinusInterval, readingTime);
        MetricReading powerfactor = circuitMap.get(circuit).getAverageBetween(Metric.POWERFACTOR, readingTimeMinusInterval, readingTime);

        CircuitEnergyStore circuitEnergyStore = storeMap.get(circuit);
        if (circuitEnergyStore != null) {
            circuitEnergyStore.accumulate(real.getValue());
        } else loggingQ.add("CircuitCollector: null circuitEnergyStore for - " + circuit.getDisplayName());

        String jsonReadings =
                "{\"Time\":\"" + readingTime.toString() + "\"," +
                        "\"Readings\":{" +
                        "\"" + voltage.getMetric().getMetricName() + "\":" + voltage.getValue().toString() + "," +
                        "\"" + real.getMetric().getMetricName() + "\":" + real.getValue().toString() + "," +
                        "\"" + apparent.getMetric().getMetricName() + "\":" + apparent.getValue().toString() + "," +
                        "\"" + reactive.getMetric().getMetricName() + "\":" + reactive.getValue().toString() + "," +
                        "\"" + current.getMetric().getMetricName() + "\":" + current.getValue().toString() + "," +
                        "\""+ powerfactor.getMetric().getMetricName()+"\":"+ current.getValue().toString()+","+
                        "}}";
        mqttHandler.publishToBroker(subTopic, jsonReadings);
    }

    public void publishEnergyMetricsForCircuits()
    {
        MetricReading energy;
        MetricReading cumulativeEnergy;
        for (Circuit circuit : circuitMap.keySet()) {
            String subTopic = mqttHandler.getTopic() + "/" + circuit.getDisplayName().replace(" ", "_");
            energy = storeMap.get(circuit).getLatestEnergyMetric();  //TODO select for time period?
            cumulativeEnergy = storeMap.get(circuit).getCumulativeEnergyForToday();
            String jsonReadings =
                    "{\"Time\":\"" + energy.getTimestamp().toString() + "\"," +
                            "\"Readings\":{" +
                            "\"Energy\":" + energy.getValue().toString() + "," +
                            "\"CumulativeEnergy\":" + cumulativeEnergy.getValue().toString() +
                            "}}";
            mqttHandler.publishToBroker(subTopic, jsonReadings);
        }
    }

    public void fillAllEnergyBuckets(int bucketToFill)
    {
        //loggingQ.add("CircuitCollector: fill buckets storeMap - " + storeMap.toString());
        for (Circuit circuit : circuitMap.keySet()) {
            //loggingQ.add("CircuitCollector: updateEnergyBucket for cct - "+ circuitData.getDisplayName());
            storeMap.get(circuit).updateEnergyBucket(bucketToFill);
        }
    }

    public void resetAllEnergyBuckets()
    {
        for (Circuit circuit : circuitMap.keySet()) {
            storeMap.get(circuit).resetAllEnergyData();
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
        boolean firstNoDataReport = true;
        long startTime;
        try {
            // wait for first readings to be ready
            Thread.sleep(2010);
        } catch (InterruptedException ignored) {
        }
        for (Circuit circuit : circuitMap.keySet()) {
            this.storeMap.put(circuit, new CircuitEnergyStore(circuit, bucketIntervalMins, loggingQ));
        }
        //loggingQ.add("CircuitCollector: storeMap - " + storeMap.toString());

        while (!Thread.interrupted()) {
            startTime = System.currentTimeMillis();
            //rawMetricsBuffer.printMetricsBuffer();

            for (Circuit circuit : circuitMap.keySet()) {
                try {
                    publishCircuitToBroker(circuit);
                } catch (InvalidDataException | OperationNotSupportedException e) {
                    if (firstNoDataReport) {
                        loggingQ.add("no data for circuitData: " +
                                circuit.getDisplayName() +
                                Arrays.toString(e.getStackTrace())
                        );
                        System.out.println("no data for circuitData: " +
                                circuit.getDisplayName() +
                                Arrays.toString(e.getStackTrace())
                        );
                        firstNoDataReport = false; //prevent jabbering
                    }
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
