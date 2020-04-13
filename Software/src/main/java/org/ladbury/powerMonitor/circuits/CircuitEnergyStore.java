package org.ladbury.powerMonitor.circuits;

import org.ladbury.powerMonitor.metrics.Metric;
import org.ladbury.powerMonitor.metrics.MetricReading;

import java.util.concurrent.LinkedBlockingQueue;

import static java.time.Instant.now;

class CircuitEnergyStore
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

    CircuitEnergyStore(Circuit circuit, int bucketIntervalMins, LinkedBlockingQueue<String> loggingQ)
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

    synchronized void resetAllEnergyData()
    {
        try {
            for (int j = 0; j < bucketsPerDay; j++) {
                energyBuckets[j] = 0.0;
                energyMetrics[j] = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        accumulationCount = 0;
        energyAccumulator = 0;
        latestBucketFilled = -1;
    }

    synchronized void resetEnergyAccumulation()
    {
        accumulationCount = 0;
        energyAccumulator = 0.0;
    }

    synchronized void accumulate(double power)
    {
        accumulationCount += 1;
        energyAccumulator += power;
    }

    void updateEnergyBucket(int bucketIndex)
    {
        try {
            energyBuckets[bucketIndex] = (accumulationCount>0) ? energyAccumulator / accumulationCount : 0.0; //average avoiding divide by zero
            //double lastBucketValue = (bucketIndex >= 1) ? energyBuckets[bucketIndex - 1] : 0.0; // avoid issue with first bucket (index 0)
            //double currentBucketValue = energyBuckets[bucketIndex];
            double wattHours = energyBuckets[bucketIndex] * ((double)bucketIntervalMins) / 60.0;
            energyMetrics[bucketIndex] = new MetricReading(wattHours, now(), Metric.WATT_HOURS);
            latestBucketFilled = bucketIndex;
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        loggingQ.add("CircuitEnergyStore: updated EnergyBucket "+ bucketIndex +
                " for circuitData "+ circuit.getDisplayName() +
                "Value "+ wattHours );*/
        resetEnergyAccumulation();
    }

    MetricReading getLatestEnergyMetric()
    {
        return (energyMetrics[latestBucketFilled]);
    }

    MetricReading getCumulativeEnergyForToday()
    {
        double total = 0.0;
        try {
            for (int bucketIndex = 0; bucketIndex <= latestBucketFilled; bucketIndex++) {
                if (energyMetrics[bucketIndex] != null)
                    if (energyMetrics[bucketIndex].getValue() > 0.0) total += energyMetrics[bucketIndex].getValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new MetricReading(total / 1000, now(), Metric.KILOWATT_HOURS);
    }
}
