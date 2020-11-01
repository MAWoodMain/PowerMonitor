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
import org.ladbury.powerMonitor.publishers.PMLogger;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;


public class CircuitCollector extends Thread
{
    private static final int MONITOR_BUFFER_SIZE = 1000;
    private final MQTTHandler mqttHandler;
    private final HashMap<Integer, CircuitEnergyStore> channelStoreMap = new HashMap<>();
    private final HashMap<Integer, CircuitPowerData> channelPowerDataMap = new HashMap<>();
    private final HashMap<Integer, PowerMetricCalculator> channelMap = new HashMap<>();
    private final PMLogger logger;
    private final EnergyBucketFiller bucketFiller;
    private final Gson gson;
    private final STM8PacketCollector packetCollector;
    private final VoltageMonitor vm;
    private final Circuits circuits;

    private int bucketIntervalMins;
    private long samplingIntervalMilliSeconds;

    /**
     * CircuitCollector   Constructor
     */
    public CircuitCollector(long samplingIntervalMilliSeconds,
                            int energyAccumulationIntervalMins
    )
    {
        this.logger = Main.getLogger();
        this.mqttHandler = Main.getMqttHandler();
        this.circuits = Main.getCircuits();
        this.bucketIntervalMins = energyAccumulationIntervalMins;
        this.gson = new Gson();
        this.samplingIntervalMilliSeconds = samplingIntervalMilliSeconds;
        bucketFiller = new EnergyBucketFiller(getEnergyAccumulationIntervalMins(),this);
        packetCollector = new STM8PacketCollector(getSamplingIntervalMilliSeconds());
        vm = new VoltageMonitor(MONITOR_BUFFER_SIZE, VoltageSenseConfig.UK9V, packetCollector);
    }

    // Getters and Setters
    public boolean isMonitoring(Circuit circuit){return channelMap.containsKey(circuit.getChannelNumber());}

    public void enableCollection(Circuit circuit)
    {
        int channel = circuit.getChannelNumber();
        Main.getCircuits().setMonitoring(channel,true);
        channelMap.put(
                channel,
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
        logger.add("Monitoring circuitData " + circuit.getDisplayName(), Level.CONFIG,this.getClass().getName());
    }

    public void disableCollection(Circuit circuit)
    {
        int channel =  circuit.getChannelNumber();
        circuits.setMonitoring(channel,false);
        channelMap.remove(channel);
        logger.add("Not monitoring circuitData " + circuit.getDisplayName(), Level.CONFIG,this.getClass().getName());
    }

    public int getEnergyAccumulationIntervalMins()
    {
        return bucketIntervalMins;
    }

    public void setEnergyAccumulationIntervalMins(int bucketIntervalMins)
    {
        this.bucketIntervalMins = bucketIntervalMins;
        if (bucketIntervalMins!=bucketFiller.getIntervalInMins())
        {
            bucketFiller.stopBucketFillScheduler();
            bucketFiller.rescheduleBucketFiller(bucketIntervalMins);
        } // else it's already OK
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
            return channelMap.get(circuit.getChannelNumber()).getLatestMetric(metric);
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
        int channel = circuit.getChannelNumber();
        CircuitPowerData circuitPowerData = new CircuitPowerData(circuit);
        Instant readingTime = Instant.now().minusSeconds(1);
        Instant readingTimeMinusInterval = readingTime.minusSeconds(1);
        circuitPowerData.time = readingTime.toString();
        circuitPowerData.readings.voltage = channelMap.get(channel).getAverageBetween(Metric.VOLTS, readingTimeMinusInterval, readingTime).getValue();
        circuitPowerData.readings.current = channelMap.get(channel).getAverageBetween(Metric.AMPS, readingTimeMinusInterval, readingTime).getValue();
        circuitPowerData.readings.realPower = channelMap.get(channel).getAverageBetween(Metric.WATTS, readingTimeMinusInterval, readingTime).getValue();
        // Accumulate power into energy store
        CircuitEnergyStore circuitEnergyStore = channelStoreMap.get(channel);
        if (circuitEnergyStore != null) {
            circuitEnergyStore.accumulate(circuitPowerData.readings.realPower);
        } else logger.add("null circuitEnergyStore for - " + circuit.getDisplayName(), Level.WARNING,this.getClass().getName());
        // These not needed to accumulate energy so leave them until after accumulation in case of exceptions
        circuitPowerData.readings.apparentPower = channelMap.get(channel).getAverageBetween(Metric.VA, readingTimeMinusInterval, readingTime).getValue();
        circuitPowerData.readings.reactivePower = channelMap.get(channel).getAverageBetween(Metric.VAR, readingTimeMinusInterval, readingTime).getValue();
        circuitPowerData.readings.powerFactor = channelMap.get(channel).getAverageBetween(Metric.POWERFACTOR, readingTimeMinusInterval, readingTime).getValue();
        return circuitPowerData;
    }

    private void publishPowerDataToBroker(Circuit circuit)
    {
        String subTopic = mqttHandler.getTelemetryTopic() + "/" + circuit.getTag();
        mqttHandler.publishToBroker(subTopic, gson.toJson(channelPowerDataMap.get(circuit.getChannelNumber())));
    }

    public CircuitPowerData getLatestCircuitPowerData(Circuit circuit)
    {
        return channelPowerDataMap.get(circuit.getChannelNumber());
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
        CircuitEnergyStore circuitEnergyStore = channelStoreMap.get(circuit.getChannelNumber());
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
            logger.add("CircuitCollector: null circuitEnergyStore for - " + circuit.getDisplayName(), Level.WARNING,this.getClass().getName());
        }
        return circuitEnergyData;
    }

    void publishEnergyMetricsForCircuits()
    {
        // called whenever energy buckets have been filled by EnergyBucketFiller
        Circuit circuit;
        for (Integer channel : channelMap.keySet()) {
            circuit = circuits.getCircuit(channel);
            if (circuit.isPublishingEnergy()) {
                String subTopic = mqttHandler.getTelemetryTopic() + "/" + circuit.getTag();
                String jsonReadings = gson.toJson(getCircuitEnergy(circuit));
                mqttHandler.publishToBroker(subTopic, jsonReadings);
            }
        }
    }

    void fillAllEnergyBuckets(int bucketToFill)
    {
        for (Integer channel : channelMap.keySet()) {
            channelStoreMap.get(channel).updateEnergyBucket(bucketToFill);
        }
    }

    void resetAllEnergyBuckets()
    {
        for (Integer channel : channelMap.keySet()) {
            channelStoreMap.get(channel).resetAllEnergyData();
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
        // Start packet collection
        logger.add("Enabling PacketCollector", Level.INFO,this.getClass().getName());
        packetCollector.start();
        //packetCollector.addPacketEventListener(System.out::println);
        logger.add("Enabling VoltageMonitor ", Level.INFO,this.getClass().getName());
        // Enable interpretation for required circuits
        Circuit circuit;
        for (Integer channel : channelMap.keySet()) {
            circuit = circuits.getCircuit(channel);
            this.channelStoreMap.put(channel, new CircuitEnergyStore(circuit, getEnergyAccumulationIntervalMins()));
            this.channelPowerDataMap.put(channel,new CircuitPowerData(circuit));
        }
        logger.add("CircuitCollector: storeMap - " + channelStoreMap.toString(), Level.FINE, this.getClass().getName());
         bucketFiller.startScheduledTasks();

        while (!Thread.interrupted()) {
            startTime = System.currentTimeMillis();
            //rawMetricsBuffer.printMetricsBuffer();

            for (Integer channel : channelMap.keySet()) {
                circuit = circuits.getCircuit(channel);
                try {
                    // Always collect data for enabled circuits
                    channelPowerDataMap.put(channel, collectCircuitData(circuit));
                    //only publish if required
                    if (circuits.getCircuit(channel).isPublishingPower()) {
                        publishPowerDataToBroker(circuit);
                    }
                } catch (InvalidDataException | OperationNotSupportedException e) {
                    if (firstNoDataReport) {
                        logger.add("no data for circuitData: " +
                                circuit.getDisplayName() +
                                Arrays.toString(e.getStackTrace()), Level.INFO,this.getClass().getName()
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
        logger.add("Interrupted, exiting", Level.INFO,this.getClass().getName());
        try {
            packetCollector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}