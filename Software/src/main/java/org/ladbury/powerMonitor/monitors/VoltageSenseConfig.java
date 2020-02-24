package org.ladbury.powerMonitor.monitors;

@SuppressWarnings({"SameParameterValue", "SameReturnValue"})
public enum VoltageSenseConfig
{
    // (230V x 13) / (9V x 1.2) = 276.9
    UK9V(242,11.22); // measured voltage

    private final int mainsRms;
    private final double transformerRms;

    // Constructor
    VoltageSenseConfig(int mainsRms, double transformerRms)
    {
        this.mainsRms = mainsRms;
        this.transformerRms = transformerRms;
    }
    // getters
    public int getMainsRms()
    {
        return mainsRms;
    }
    public double getTransformerRms()
    {
        return transformerRms;
    }
    public static double getMagicMainsConstant()
    {
        return 12;
    }
    public static double getMagicTransformerConstant()
    {
        return 1.195;
    }

    public double scaleValue(double value)
    {
        return ((mainsRms*12)/ (transformerRms*1.195))*value;
    }
    public double offsetValue(double value)
    {
        return value;
    }
}

