package org.ladbury.powerMonitor.currentClamps;

public class Clamp
{
    private String clampName;
    private int maxCurrent;
    private  double samplingResistor;
    private  int turnsFactor;
    private  double calibrationFactor;
    private  double calibrationOffset;

    // Constructor
    Clamp(String clampName, int maxCurrent, double samplingResistor, int turnsFactor, double calibrationFactor, double calibrationOffset)
    {
        this.clampName = clampName;
        this.maxCurrent = maxCurrent;
        this.samplingResistor = samplingResistor;
        this.turnsFactor = turnsFactor;
        this.calibrationFactor = calibrationFactor;
        this.calibrationOffset = calibrationOffset;
    }

    // Getters
    public String getClampName() {return this.clampName;}
    public int getMaxCurrent() {return this.maxCurrent;}
    public double getSamplingResistor() {return this.samplingResistor;}
    public int getTurnsFactor() {return turnsFactor;}
    public double scaleValue(double value)
    {
        return calibrationFactor*(turnsFactor/samplingResistor)*value;
    }
    public double offsetValue(double value)
    {
        return value + calibrationOffset;
    }

    // Setters, Other fields are read only
    public void setCalibrationFactor(double calibrationFactor) {this.calibrationFactor = calibrationFactor;}
    public void setCalibrationOffset(double calibrationOffset) {this.calibrationOffset = calibrationOffset;}
}
