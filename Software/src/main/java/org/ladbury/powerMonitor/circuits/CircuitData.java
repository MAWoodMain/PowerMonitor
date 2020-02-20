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
    private String circuitName;
    private Instant timestamp;
    private Double voltage;
    private Double current;
    private Double realPower;
    private Double reactivePower;
    private Double apparentPower;
    private Double Energy;
}
