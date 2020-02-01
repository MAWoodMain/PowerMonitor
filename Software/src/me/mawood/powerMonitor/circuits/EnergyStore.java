package me.mawood.powerMonitor.circuits;

public class EnergyStore
{
    private final long[] accumulationCount;
    private final Double[] energyAccumulator;
    private final Double[][] energyBuckets;
    private int bucketIntervalMins;

    public EnergyStore(int nbrCircuits, int bucketIntervalMins)
    {
        accumulationCount = new long[nbrCircuits+1];//channel number is > max circuit number
        energyAccumulator = new Double[nbrCircuits+1];
        int bucketsPerDay;
        bucketsPerDay = 60*24/bucketIntervalMins;
        this.bucketIntervalMins = bucketIntervalMins;
        energyBuckets = new Double[nbrCircuits][bucketsPerDay+1];
        for (int i=0; i<nbrCircuits; i++)
        {
            accumulationCount[i] = 0;
            this.energyAccumulator[i]  = 0.0;
            for (int j=0; j<bucketsPerDay; j++)
            {
                energyBuckets[i][j] = 0.0;
            }
        }
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

    public synchronized void updateEnergyBucket( Circuit circuit, int bucketIndex, double wattHours)
    {
        energyBuckets[circuit.getChannelNumber()][bucketIndex]= wattHours;
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
        return energyBuckets[circuit.getChannelNumber()][bucketIndex];
    }
}
