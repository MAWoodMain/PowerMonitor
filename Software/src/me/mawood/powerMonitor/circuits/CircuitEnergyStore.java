package me.mawood.powerMonitor.circuits;

import me.mawood.powerMonitor.metrics.Metric;
import me.mawood.powerMonitor.metrics.MetricReading;

import java.util.concurrent.LinkedBlockingQueue;

import static java.time.Instant.now;

public class CircuitEnergyStore
{
    private long accumulationCount;
    private Double energyAccumulator;
    private final Double[] energyBuckets;
    private MetricReading[] energyMetrics;
    private int bucketIntervalMins;
    private int bucketsPerDay;
    private final LinkedBlockingQueue<String> loggingQ;
    private final Circuit circuit;

    private int latestBucketFilled;

    public CircuitEnergyStore(Circuit circuit, int bucketIntervalMins, LinkedBlockingQueue<String> loggingQ)
    {
        this.circuit = circuit;
        this.loggingQ = loggingQ;
        this.accumulationCount = 0;
        this.energyAccumulator = new Double(0);
        this.bucketsPerDay = 60 * 24 / bucketIntervalMins;
        this.bucketIntervalMins = bucketIntervalMins;
        this.energyBuckets = new Double[bucketsPerDay + 1];
        this.energyMetrics = new MetricReading[bucketsPerDay + 1];
        this.latestBucketFilled = -1;
        resetEnergyAccumulation();
    }

    public void resetAllEnergyData()
    {
        resetEnergyAccumulation();
        for (int j = 0; j < bucketsPerDay; j++) {
            updateEnergyBucket(j);
        }
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

    public synchronized void updateEnergyBucket(int bucketIndex)
    {
        energyBuckets[bucketIndex] = energyAccumulator / (bucketIntervalMins * 60); //average
        Double lastBucketValue = (bucketIndex >= 1) ? energyBuckets[bucketIndex - 1] : 0.0;
        Double currentBucketValue = energyBuckets[bucketIndex];
        Double wattHours = ((currentBucketValue - lastBucketValue) * bucketIntervalMins) / 60;
        energyMetrics[bucketIndex] = new MetricReading(wattHours, now(), Metric.WATT_HOURS);
        latestBucketFilled = bucketIndex;
    }

    public MetricReading getLatestEnergyMetric()
    {
        return (energyMetrics[latestBucketFilled]);
    }

    public MetricReading getCumulativeEnergyForToday()
    {
        Double total = new Double(0.0);
        for (int bucketIndex = 0; bucketIndex <= latestBucketFilled; bucketIndex++) {
            if (energyMetrics[bucketIndex] != null)
                if (energyMetrics[bucketIndex].getValue() > 0.0) total = +energyMetrics[bucketIndex].getValue();
        }
        return new MetricReading(total / 1000, now(), Metric.KILOWATT_HOURS);
    }
/*
    public Double getEnergyAccumulator(){return energyAccumulator;}
    public long getAccumulationCount(){return accumulationCount;}
    public int getBucketIntervalMins(){return bucketIntervalMins;}
    public Double getEnergyBucket(int bucketIndex)
    {
        if (bucketIndex < 0) return -1.0;
        return energyBuckets[bucketIndex];
    }
    public Double getLatestFilledBucket()
    {
        if (latestBucketFilled < 0) return -1.0;
        return energyBuckets[latestBucketFilled];
    }
*/

}
