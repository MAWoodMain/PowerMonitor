package org.ladbury.powerMonitor.circuits;

import com.google.gson.Gson;
import org.ladbury.powerMonitor.Main;
import org.ladbury.powerMonitor.metrics.*;
import org.ladbury.powerMonitor.monitors.CurrentMonitor;
import org.ladbury.powerMonitor.monitors.RealPowerMonitor;
import org.ladbury.powerMonitor.monitors.VoltageMonitor;
import org.ladbury.powerMonitor.monitors.VoltageSenseConfig;
import org.ladbury.powerMonitor.packets.STM8PacketCollector;
import org.ladbury.powerMonitor.publishers.MQTTHandler;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;


public class CircuitCollector extends Thread
{
    private static final int MONITOR_BUFFER_SIZE = 1000;
    private final MQTTHandler mqttHandler;
    private final HashMap<Circuit, CircuitEnergyStore> storeMap = new HashMap<>();
    private final HashMap<Circuit, CircuitPowerData> circuitPowerDataMap = new HashMap<>();
    private final HashMap<Circuit, Boolean> publishPowerMap = new HashMap<>();
    private final HashMap<Circuit, Boolean> publishEnergyMap = new HashMap<>();
    private final HashMap<Circuit, PowerMetricCalculator> circuitMap = new HashMap<>();
    private final LinkedBlockingQueue<String> loggingQ;
    private final EnergyBucketFiller bucketFiller;
    private int bucketIntervalMins;
    private long samplingIntervalMilliSeconds;
    private final Gson gson;
    private  STM8PacketCollector packetCollector;
    private  VoltageMonitor vm;

    /**
     * CircuitCollector   Constructor
     */
    public CircuitCollector(long samplingIntervalMilliSeconds,
                            int energyAccumulationIntervalMins,
                            MQTTHandler publisher,
                            LinkedBlockingQueue<String> loggingQ
    )
    {
        this.loggingQ = loggingQ;
        this.mqttHandler = publisher;
        this.bucketIntervalMins = energyAccumulationIntervalMins;
        this.gson = new Gson();
        this.samplingIntervalMilliSeconds = 1000;
        bucketFiller = new EnergyBucketFiller(getEnergyAccumulationIntervalMins(), true, this, loggingQ);

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

    public void enableCollection(Circuit circuit)
    {
        Main.getCircuits().setMonitoring(circuit.getChannelNumber(),true);
        circuitMap.put(
                circuit,
                new PowerMetricCalculator(vm,
                        new CurrentMonitor(MONITOR_BUFFER_SIZE,
                                Main.getClamps().getClamp(circuit.getClampName()),
                                circuit.getChannelNumber(),
                                packetCollector),
                        new RealPowerMonitor(MONITOR_BUFFER_SIZE,
                                VoltageSenseConfig.UK9V,
                                Main.getClamps().getClamp(circuit.getClampName()),
                                circuit.getChannelNumber(),
                                packetCollector)));
        loggingQ.add("Monitoring circuitData " + circuit.getDisplayName());
    }

    public void disableCollection(Circuit circuit)
    {
        Main.getCircuits().setMonitoring(circuit.getChannelNumber(),false);
        circuitMap.remove(circuit);
        loggingQ.add("Not monitoring circuitData " + circuit.getDisplayName());
    }

    public synchronized int getEnergyAccumulationIntervalMins()
    {
        return bucketIntervalMins;
    }

    public synchronized void setEnergyAccumulationIntervalMins(int bucketIntervalMins)
    {
        this.bucketIntervalMins = bucketIntervalMins;
    }

    public synchronized long getSamplingIntervalMilliSeconds()
    {
        return samplingIntervalMilliSeconds;
    }

    public synchronized void setSamplingIntervalMilliSeconds(long samplingIntervalMilliSeconds)
    {
        this.samplingIntervalMilliSeconds = samplingIntervalMilliSeconds;
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
        CircuitPowerData circuitPowerData = new CircuitPowerData(circuit);
        Instant readingTime = Instant.now().minusSeconds(1);
        Instant readingTimeMinusInterval = readingTime.minusSeconds(1);
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

    private void publishPowerDataToBroker(Circuit circuit)
    {
        String subTopic = mqttHandler.getTelemetryTopic() + "/" + circuit.getTag();
        mqttHandler.publishToBroker(subTopic, gson.toJson(circuitPowerDataMap.get(circuit)));
    }

    public CircuitPowerData getLatestCircuitPowerData(Circuit circuit)
    {
        return circuitPowerDataMap.get(circuit);
    }

    //
    // Energy handling section
    //

    //TODO getCircuitEnergyForPeriod?

    public CircuitEnergyData getCircuitEnergy(Circuit circuit)
    {
        CircuitEnergyData circuitEnergyData = new CircuitEnergyData(circuit);
        circuitEnergyData.readings.energy = 0.0; // in case data not available
        circuitEnergyData.readings.cumulativeEnergy = 0.0; // in case data not available
        CircuitEnergyStore circuitEnergyStore = storeMap.get(circuit);
        MetricReading energy;

        // Start packet collection
        loggingQ.add("Enabling PacketCollector");
        try {
            packetCollector = new STM8PacketCollector(getSamplingIntervalMilliSeconds());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(5);
        }
        //packetCollector.addPacketEventListener(System.out::println);
        loggingQ.add("Enabling VoltageMonitor ");
        vm = new VoltageMonitor(MONITOR_BUFFER_SIZE, VoltageSenseConfig.UK9V, packetCollector);
        // Enable interpretation for required circuits

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

    void publishEnergyMetricsForCircuits()
    {
        // called whenever energy buckets have been filled by EnergyBucketFiller
        for (Circuit circuit : circuitMap.keySet()) {
            if (publishEnergyMap.get(circuit)) {
                String subTopic = mqttHandler.getTelemetryTopic() + "/" + circuit.getTag();
                String jsonReadings = gson.toJson(getCircuitEnergy(circuit));
                mqttHandler.publishToBroker(subTopic, jsonReadings);
            }
        }
    }

    void fillAllEnergyBuckets(int bucketToFill)
    {
        for (Circuit circuit : circuitMap.keySet()) {
            storeMap.get(circuit).updateEnergyBucket(bucketToFill);
        }
    }

    void resetAllEnergyBuckets()
    {
        for (Circuit circuit : circuitMap.keySet()) {
            storeMap.get(circuit).resetAllEnergyData();
        }
    }

    //
    // Runnable implementation
    //

    /**
     * run  The main collection loop
     */
    @Override
    public void run()
    {
        boolean firstNoDataReport = true;
        long startTime;

        try {
            // wait for first readings to be ready
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
        for (Circuit circuit : circuitMap.keySet()) {
            this.storeMap.put(circuit, new CircuitEnergyStore(circuit, getEnergyAccumulationIntervalMins(), loggingQ));
            this.circuitPowerDataMap.put(circuit,new CircuitPowerData(circuit));
        }
        //loggingQ.add("CircuitCollector: storeMap - " + storeMap.toString());
         bucketFiller.start();

        while (!Thread.interrupted()) {
            startTime = System.currentTimeMillis();
            //rawMetricsBuffer.printMetricsBuffer();

            for (Circuit circuit : circuitMap.keySet()) {
                try {
                    // Always collect data for enabled circuits
                    circuitPowerDataMap.put(circuit, collectCircuitData(circuit));
                    //only publish if required
                    if (publishPowerMap.get(circuit)) {
                        publishPowerDataToBroker(circuit);
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
            while (startTime + getSamplingIntervalMilliSeconds() > System.currentTimeMillis()) {
                // wait half the remaining time
                try {
                    Thread.sleep(Math.max(0, ((startTime + getSamplingIntervalMilliSeconds()) - System.currentTimeMillis()) / 2));
                } catch (InterruptedException ignore) {
                }
            }
        }
        loggingQ.add("CircuitCollector: Interrupted, exiting");
    }
}