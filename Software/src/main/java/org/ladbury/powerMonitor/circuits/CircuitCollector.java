package org.ladbury.powerMonitor.circuits;

import com.google.gson.Gson;
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
    private final HashMap<Circuit, CircuitPowerData> circuitDataMap = new HashMap<>();
    private final HashMap<Circuit, Boolean> publishPowerMap = new HashMap<>();
    private final HashMap<Circuit, Boolean> publishEnergyMap = new HashMap<>();
    private final int bucketIntervalMins;
    private final Gson gson;

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
        this.gson = new Gson();
        //this.energyStore = energyStore;
    }

    // Getters and Setters
    public void setPowerPublishing(Circuit circuit, boolean publish)
    {
        publishPowerMap.put(circuit, publish);
    }

    public boolean getPowerPublishing(Circuit circuit)
    {
        return publishPowerMap.get(circuit);
    }

    public void setEnergyPublishing(Circuit circuit, boolean publish)
    {
        publishEnergyMap.put(circuit, publish);
    }

    public boolean getEnergyPublishing(Circuit circuit)
    {
        return publishEnergyMap.get(circuit);
    }

    public MetricReading getLatestMetricReading(Circuit circuit, Metric metric)
    {
        try {
            return circuitMap.get(circuit).getLatestMetric(metric);
        } catch (OperationNotSupportedException e) {
            System.out.println("getLatestMetric not supported for " + metric.toString());
        }
        return new MetricReading(0.0, Instant.now(), metric);
    }

    //
    // Power handling Section
    //

    private CircuitPowerData collectCircuitData(Circuit circuit) throws InvalidDataException, OperationNotSupportedException
    {
        CircuitPowerData circuitPowerData = new CircuitPowerData();
        Instant readingTime = Instant.now().minusSeconds(1);
        Instant readingTimeMinusInterval = readingTime.minusSeconds(1);
        circuitPowerData.channel = circuit.getChannelNumber();
        circuitPowerData.circuitName = circuit.getDisplayName();
        circuitPowerData.time = readingTime.toString();
        circuitPowerData.readings.voltage = circuitMap.get(circuit).getAverageBetween(Metric.VOLTS, readingTimeMinusInterval, readingTime).getValue();
        circuitPowerData.readings.current = circuitMap.get(circuit).getAverageBetween(Metric.AMPS, readingTimeMinusInterval, readingTime).getValue();
        circuitPowerData.readings.realPower = circuitMap.get(circuit).getAverageBetween(Metric.WATTS, readingTimeMinusInterval, readingTime).getValue();
        // Accumulate power into energy store
        CircuitEnergyStore circuitEnergyStore = storeMap.get(circuit);
        if (circuitEnergyStore != null) {
            circuitEnergyStore.accumulate(circuitPowerData.readings.realPower);
        } else loggingQ.add("CircuitCollector: null circuitEnergyStore for - " + circuit.getDisplayName());
        // These not needed to accumulate energy so leave them until after accumulation in case of exceptions
        circuitPowerData.readings.apparentPower = circuitMap.get(circuit).getAverageBetween(Metric.VA, readingTimeMinusInterval, readingTime).getValue();
        circuitPowerData.readings.reactivePower = circuitMap.get(circuit).getAverageBetween(Metric.VAR, readingTimeMinusInterval, readingTime).getValue();
        circuitPowerData.readings.powerFactor = circuitMap.get(circuit).getAverageBetween(Metric.POWERFACTOR, readingTimeMinusInterval, readingTime).getValue();
        return circuitPowerData;
    }

    private void publishCircuitToBroker(Circuit circuit)
    {
        String subTopic = mqttHandler.getTopic() + "/" + circuit.getTag();
        mqttHandler.publishToBroker(subTopic, gson.toJson(circuitDataMap.get(circuit)));
    }

    public CircuitPowerData getLatestCircuitPowerData(Circuit circuit)
    {
        return circuitDataMap.get(circuit);
    }

    //
    // Energy handling section
    //

    //TODO getCircuitEnergyForPeriod?

    public CircuitEnergyData getCircuitEnergy(Circuit circuit)
    {
        CircuitEnergyData circuitEnergyData = new CircuitEnergyData();
        circuitEnergyData.circuitName = circuit.getDisplayName();
        circuitEnergyData.channel = circuit.getChannelNumber();
        circuitEnergyData.time = Instant.now().toString(); // change to time of reading later
        circuitEnergyData.readings = new CircuitEnergyReadings();
        circuitEnergyData.readings.energy = 0.0; // in case data not available
        circuitEnergyData.readings.cumulativeEnergy = 0.0; // in case data not available
        CircuitEnergyStore circuitEnergyStore = storeMap.get(circuit);
        MetricReading energy;

        if (circuitEnergyStore != null) {
            try {
                energy = circuitEnergyStore.getLatestEnergyMetric();
            } catch (Exception e) {
                e.printStackTrace();
                return circuitEnergyData;
            }
            circuitEnergyData.time = circuitEnergyStore.getLatestEnergyMetric().getTimestamp().toString();
            circuitEnergyData.readings.energy = energy.getValue();
            circuitEnergyData.readings.cumulativeEnergy = circuitEnergyStore.getCumulativeEnergyForToday().getValue();
        } else {
            loggingQ.add("CircuitCollector: null circuitEnergyStore for - " + circuit.getDisplayName());
        }
        return circuitEnergyData;
    }

    public void publishEnergyMetricsForCircuits()
    {
        // called whenever energy buckets have been filled by EnergyBucketFiller
        for (Circuit circuit : circuitMap.keySet()) {
            if (publishEnergyMap.get(circuit)) {
                String subTopic = mqttHandler.getTopic() + "/" + circuit.getDisplayName().replace(" ", "_");
                String jsonReadings = gson.toJson(getCircuitEnergy(circuit));
                mqttHandler.publishToBroker(subTopic, jsonReadings);
            }
        }
    }

    public void fillAllEnergyBuckets(int bucketToFill)
    {
        for (Circuit circuit : circuitMap.keySet()) {
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
            this.circuitDataMap.put(circuit,new CircuitPowerData(circuit));
        }
        //loggingQ.add("CircuitCollector: storeMap - " + storeMap.toString());

        while (!Thread.interrupted()) {
            startTime = System.currentTimeMillis();
            //rawMetricsBuffer.printMetricsBuffer();

            for (Circuit circuit : circuitMap.keySet()) {
                try {
                    // Always collect data for enabled circuits
                    circuitDataMap.put(circuit, collectCircuitData(circuit));
                    //only publish if required
                    if (publishPowerMap.get(circuit)) {
                        publishCircuitToBroker(circuit);
                    }
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
