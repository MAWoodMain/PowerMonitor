package me.mawood.powerMonitor.circuits;

public class EnergyStore
{
    private final long[] accumulationCount;
    private final Double[] energyAccumulator;

    public EnergyStore()
    {
        int circuitCount = HomeCircuits.values().length+1;
        accumulationCount = new long[circuitCount];
        energyAccumulator = new Double[circuitCount];
        for (int i=0; i<circuitCount; i++)
        {
            accumulationCount[i] = 0;
            this.energyAccumulator[i]  = 0.0;

        }

    }
    public void resetEnergyAccumulation(Circuit circuit)
    {
        accumulationCount[circuit.getChannelNumber()] = 0;
        energyAccumulator[circuit.getChannelNumber()]  = 0.0;
    }

    public Double getEnergyAccumulator(Circuit circuit)
    {
        return energyAccumulator[circuit.getChannelNumber()];
    }

    public long getAccumulationCount(Circuit circuit)
    {
        return accumulationCount[circuit.getChannelNumber()];
    }

    public void accumulate(Circuit circuit, double power)
    {
        accumulationCount[circuit.getChannelNumber()] += 1;
        energyAccumulator[circuit.getChannelNumber()]  += power;
    }

}
