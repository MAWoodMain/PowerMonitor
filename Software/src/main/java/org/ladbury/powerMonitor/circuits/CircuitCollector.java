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
    final MQTTHandler mqttHandler;

    // run control variables
    private final Map<Circuit, PowerMetricCalculator> circuitMap;
    private final LinkedBlockingQueue<String> loggingQ;
    private final HashMap<Circuit,CircuitEnergyStore> storeMap = new HashMap<>();
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

    public CircuitData getCircuitData(Circuit circuit)
    {
        try {
            CircuitData circuitData = new CircuitData();
            Instant readingTime = Instant.now().minusSeconds(1);
            Instant readingTimeMinusInterval = readingTime.minusSeconds(1);
            circuitData.channel = circuit.getChannelNumber();
            circuitData.circuitName = circuit.getDisplayName();
            circuitData.timestamp = readingTime;
            MetricReading voltage = circuitMap.get(circuit).getAverageBetween(Metric.VOLTS, readingTimeMinusInterval, readingTime);
            MetricReading apparent = circuitMap.get(circuit).getAverageBetween(Metric.VA, readingTimeMinusInterval, readingTime);
            MetricReading real = circuitMap.get(circuit).getAverageBetween(Metric.WATTS, readingTimeMinusInterval, readingTime);

            CircuitEnergyStore circuitEnergyStore = storeMap.get(circuit);

            MetricReading reactive = circuitMap.get(circuit).getAverageBetween(Metric.VAR, readingTimeMinusInterval, readingTime);
            MetricReading current = circuitMap.get(circuit).getAverageBetween(Metric.AMPS, readingTimeMinusInterval, readingTime);
            //MetricReading powerfactor = circuitMap.get(circuitData).getAverageBetween(Metric.POWERFACTOR, readingTimeMinusInterval, readingTime);
            Double powerFactor = Math.round(real.getValue() / apparent.getValue() * 1000000.0) / 1000000.0;
            circuitData.voltage = voltage.getValue();
            circuitData.realPower = real.getValue() ;
            circuitData.apparentPower = apparent.getValue() ;
            circuitData.reactivePower = reactive.getValue();
            circuitData.current = current.getValue();
            circuitData.powerFactor = powerFactor;
            if (circuitEnergyStore != null) {
                circuitData.energy = circuitEnergyStore.getLatestEnergyMetric().getValue();
            } else loggingQ.add("CircuitCollector: null circuitEnergyStore for - " + circuit.getDisplayName());
            return circuitData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void publishCircuitToBroker(Circuit circuit) throws InvalidDataException, OperationNotSupportedException
    {
        String subTopic = mqttHandler.getTopic() + "/" + circuit.getTag();
        Instant readingTime = Instant.now().minusSeconds(1);
        Instant readingTimeMinusInterval = readingTime.minusSeconds(1);
        MetricReading voltage = circuitMap.get(circuit).getAverageBetween(Metric.VOLTS, readingTimeMinusInterval, readingTime);
        MetricReading apparent = circuitMap.get(circuit).getAverageBetween(Metric.VA, readingTimeMinusInterval, readingTime);
        MetricReading real = circuitMap.get(circuit).getAverageBetween(Metric.WATTS, readingTimeMinusInterval, readingTime);

        CircuitEnergyStore circuitEnergyStore = storeMap.get(circuit);
        if (circuitEnergyStore!=null) {circuitEnergyStore.accumulate(real.getValue());}
        else loggingQ.add("CircuitCollector: null circuitEnergyStore for - "+ circuit.getDisplayName());

        MetricReading reactive = circuitMap.get(circuit).getAverageBetween(Metric.VAR, readingTimeMinusInterval, readingTime);
        MetricReading current = circuitMap.get(circuit).getAverageBetween(Metric.AMPS, readingTimeMinusInterval, readingTime);
        //MetricReading powerfactor = circuitMap.get(circuitData).getAverageBetween(Metric.POWERFACTOR, readingTimeMinusInterval, readingTime);
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
        for (Circuit circuit : circuitMap.keySet()) {
            String subTopic = mqttHandler.getTopic() + "/" + circuit.getDisplayName().replace(" ", "_");
            energy = storeMap.get(circuit).getLatestEnergyMetric();
            cumulativeEnergy = storeMap.get(circuit).getCumulativeEnergyForToday();
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
        long startTime;
        try {
            // wait for first readings to be ready
            Thread.sleep(2010);
        } catch (InterruptedException ignored) {
        }
        for (Circuit circuit : circuitMap.keySet()) {
            this.storeMap.put(circuit,new CircuitEnergyStore(circuit,bucketIntervalMins,loggingQ));
        }
        //loggingQ.add("CircuitCollector: storeMap - " + storeMap.toString());

        while (!Thread.interrupted()) {
            startTime = System.currentTimeMillis();
            //rawMetricsBuffer.printMetricsBuffer();

            for (Circuit circuit : circuitMap.keySet()) {
                try {
                    publishCircuitToBroker(circuit);
                } catch (InvalidDataException | OperationNotSupportedException e) {
                    loggingQ.add("no data for circuitData: " +
                            circuit.getDisplayName() +
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
