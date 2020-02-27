package org.ladbury.powerMonitor.currentClamps;

public class Clamp
{
    private final String clampName;
    private final int maxCurrent;
    private final double samplingResistor;
    private final int turnsFactor;
    private  double scale;
    private  double offset;

    // Constructor
    Clamp(String clampName, int maxCurrent, double samplingResistor, int turnsFactor, double scale, double offset)
    {
        this.clampName = clampName;
        this.maxCurrent = maxCurrent;
        this.samplingResistor = samplingResistor;
        this.turnsFactor = turnsFactor;
        this.scale = scale;
        this.offset = offset;
    }

    // Getters
    public String getClampName() {return this.clampName;}
    public int getMaxCurrent() {return this.maxCurrent;}
    public double getSamplingResistor() {return this.samplingResistor;}
    public int getTurnsFactor() {return turnsFactor;}
    public double scaleValue(double value)
    {
        return scale *(turnsFactor/samplingResistor)*value;
    }
    public double offsetValue(double value)
    {
        return value + offset;
    }

    // Setters, Other fields are read only
    public void setScale(double scale) {this.scale = scale;}
    public void setOffset(double offset) {this.offset = offset;}
}
