package me.mawood.powerMonitor.circuits;

import me.mawood.powerMonitor.metrics.Metric;
import me.mawood.powerMonitor.metrics.MetricReading;

import java.util.concurrent.LinkedBlockingQueue;

import static java.time.Instant.now;

public class CircuitEnergyStore
{
    private long accumulationCount;
    private double energyAccumulator;
    private final double[] energyBuckets;
    private final MetricReading[] energyMetrics;
    private final int bucketIntervalMins;
    private final int bucketsPerDay;
    private final LinkedBlockingQueue<String> loggingQ;
    private final Circuit circuit;

    private int latestBucketFilled;

    public CircuitEnergyStore(Circuit circuit, int bucketIntervalMins, LinkedBlockingQueue<String> loggingQ)
    {
        this.circuit = circuit;
        this.loggingQ = loggingQ;
        this.accumulationCount = 0;
        this.energyAccumulator = 0.0;
        this.bucketsPerDay = 60 * 24 / bucketIntervalMins;
        this.bucketIntervalMins = bucketIntervalMins;
        this.energyBuckets = new double[bucketsPerDay + 1];
        this.energyMetrics = new MetricReading[bucketsPerDay + 1];
        this.latestBucketFilled = -1;
        resetEnergyAccumulation();
    }

    public synchronized void resetAllEnergyData()
    {
        for (int j = 0; j < bucketsPerDay; j++) {
            energyBuckets[j] = 0.0;
            energyMetrics[j] = null;
        }
        accumulationCount = 0;
        energyAccumulator = 0;
        latestBucketFilled = -1;
    }

    public synchronized void resetEnergyAccumulation()
    {
        accumulationCount = 0;
        energyAccumulator = 0.0;
    }

    public synchronized void accumulate(double power)
    {
        accumulationCount += 1;
        energyAccumulator += power;
    }

    public void updateEnergyBucket(int bucketIndex)
    {
        energyBuckets[bucketIndex] = (accumulationCount>0) ? energyAccumulator / accumulationCount : 0.0; //average
        double lastBucketValue = (bucketIndex >= 1) ? energyBuckets[bucketIndex - 1] : 0.0;
        double currentBucketValue = energyBuckets[bucketIndex];
        double wattHours = ((currentBucketValue - lastBucketValue) * bucketIntervalMins) / 60;
        energyMetrics[bucketIndex] = new MetricReading(wattHours, now(), Metric.WATT_HOURS);
        latestBucketFilled = bucketIndex;
        loggingQ.add("CircuitEnergyStore: updated EnergyBucket "+ bucketIndex +
                " for circuit "+ circuit.getDisplayName() + "Value "+ wattHours );
    }

    public MetricReading getLatestEnergyMetric()
    {
        return (energyMetrics[latestBucketFilled]);
    }

    public MetricReading getCumulativeEnergyForToday()
    {
        double total = 0.0;
        for (int bucketIndex = 0; bucketIndex <= latestBucketFilled; bucketIndex++) {
            if (energyMetrics[bucketIndex] != null)
                if (energyMetrics[bucketIndex].getValue() > 0.0) total = +energyMetrics[bucketIndex].getValue();
        }
        return new MetricReading(total / 1000, now(), Metric.KILOWATT_HOURS);
    }
}
