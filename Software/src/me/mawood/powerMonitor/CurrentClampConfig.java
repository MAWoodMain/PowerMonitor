package me.mawood.powerMonitor;

public enum CurrentClampConfig
{
    SCT013_5A1V (5,372.0, 1800),
    SCT013_10A1V (10,186.0, 1800),
    SCT013_15A1V (15,124.0, 1800),
    SCT013_20A1V (20,93.0, 1800),
    SCT013_25A1V (25,74.4, 1800),
    SCT013_30A1V (30,62.0, 1800),
    SCT013_50A1V (50,37.2, 1800),
    SCT013_60A1V (60,31.0, 1800),
    SCT013_70A1V (70,26.6, 1800),
    SCT013_100A1V (100,22.0, 1800);

    private final int maxCurrent;
    private final double samplingResistor;
    private final int turnsFactor;

    CurrentClampConfig(int maxCurrent, double samplingResistor, int turnsFactor)
    {
        this.maxCurrent = maxCurrent;
        this.samplingResistor = samplingResistor;
        this.turnsFactor = turnsFactor;
    }
    public int getMaxCurrent() {return this.maxCurrent;}
    public double getSamplingResistor() {return this.samplingResistor;}
    public int getTurnsFactor() {return turnsFactor;}
}
