package me.mawood.powerMonitor.metrics.sources.configs;

public enum VoltageSenseConfig
{
    // (230V x 13) / (9V x 1.2) = 276.9
    UK9V(242,11.22); // measured voltage

    private final int mainsRms;
    private final double transformerRms;

    VoltageSenseConfig(int mainsRms, double transformerRms)
    {
        this.mainsRms = mainsRms;
        this.transformerRms = transformerRms;
    }

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
}
