package org.ladbury.powerMonitor.circuits;

import java.time.Instant;

public class CircuitData
{
        /*
    CircuitData("Circuit Data",7), //get only
        //"circuit": "Whole_House", get only  ...rest as object
        //"voltage":float,
        //"current":float,
        //"realpower":float,
        //"reactivepower":float,
        //"apparentpower":float,
        //"energy":float;
    */
    public int channel;
    public String circuitName;
    public Instant timestamp;
    public Double voltage;
    public Double current;
    public Double realPower;
    public Double reactivePower;
    public Double apparentPower;
    public Double powerFactor;
    public Double energy;
    public CircuitData ()
    {
        channel = -1;
        circuitName = "";
        timestamp = Instant.now();
        voltage = 0.0;
        current = 0.0;
        realPower = 0.0;
        reactivePower = 0.0;
        apparentPower = 0.0;
        powerFactor = 0.0;
        energy = 0.0;
    }
}
