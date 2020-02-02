package me.mawood.powerMonitor.circuits;

import me.mawood.powerMonitor.metrics.Metric;
import me.mawood.powerMonitor.metrics.MetricReading;

import static java.time.Instant.now;

public class EnergyStore
{
    private final long[] accumulationCount;
    private final Double[] energyAccumulator;
    private final Double[][] energyBuckets;
    private MetricReading[][] energyMetrics;
    private int bucketIntervalMins;
    private int bucketsPerDay;
    private int nbrCircuits;
    private int latestBucketFilled;

    public EnergyStore(int nbrCircuits, int bucketIntervalMins)
    {
        this.accumulationCount = new long[nbrCircuits+1];//channel number is > max circuit number
        this.energyAccumulator = new Double[nbrCircuits+1];
        this. bucketsPerDay = 60*24/bucketIntervalMins;
        this.bucketIntervalMins = bucketIntervalMins;
        this.nbrCircuits = nbrCircuits;
        this.energyBuckets = new Double[nbrCircuits][bucketsPerDay+1];
        this.energyMetrics = new MetricReading[nbrCircuits][bucketsPerDay+1];
        this.latestBucketFilled = -1;
        resetAllEnergyAccumulation();
    }

    public void resetAllEnergyAccumulation()
    {
        for(Circuit circuit: HomeCircuits.values())
        {
            resetEnergyAccumulation(circuit);
            for (int j=0; j<bucketsPerDay; j++)
            {
                updateEnergyBucket(circuit,j,0.0);
            }
        }
        latestBucketFilled = -1;
    }
    public void fillAllEnergyBuckets(int bucketNumber)
    {
        for(Circuit circuit: HomeCircuits.values())
        {
            updateEnergyBucket(circuit, bucketNumber,energyAccumulator[circuit.getChannelNumber()]);
        }
        latestBucketFilled = bucketNumber;
    }

    public synchronized void resetEnergyAccumulation(Circuit circuit)
    {
        accumulationCount[circuit.getChannelNumber()] = 0;
        energyAccumulator[circuit.getChannelNumber()]  = 0.0;
    }

    public synchronized void accumulate(Circuit circuit, double power)
    {
        accumulationCount[circuit.getChannelNumber()] += 1;
        energyAccumulator[circuit.getChannelNumber()]  += power;
    }

    public synchronized void updateEnergyBucket( Circuit circuit, int bucketIndex, double accumulation)
    {
        energyBuckets[circuit.getChannelNumber()][bucketIndex]= accumulation;
        double lastbucketValue;
        if (bucketIndex>=1)
            lastbucketValue =  energyBuckets[circuit.getChannelNumber()][bucketIndex-1];
        else lastbucketValue = 0.0;
        double wattHours = (energyBuckets[circuit.getChannelNumber()][bucketIndex]-lastbucketValue)*bucketIntervalMins/60;
        energyMetrics[circuit.getChannelNumber()][bucketIndex]= new MetricReading(wattHours, now(), Metric.WATT_HOURS);
    }

    public Double getEnergyAccumulator(Circuit circuit)
    {
        return energyAccumulator[circuit.getChannelNumber()];
    }

    public long getAccumulationCount(Circuit circuit)
    {
        return accumulationCount[circuit.getChannelNumber()];
    }

    public int getBucketIntervalMins()
    {
        return bucketIntervalMins;
    }

    public Double getEnergyBucket(Circuit circuit, int bucketIndex)
    {
        if (bucketIndex < 0) return -1.0;
        return energyBuckets[circuit.getChannelNumber()][bucketIndex];
    }
    public Double getLatestFilledBucket(Circuit circuit)
    {
        if (latestBucketFilled < 0) return -1.0;
        return energyBuckets[circuit.getChannelNumber()][latestBucketFilled];
    }
    public MetricReading getLatestEnergyMetric(Circuit circuit)
    {
        return (energyMetrics[circuit.getChannelNumber()][latestBucketFilled]);
    }
}
