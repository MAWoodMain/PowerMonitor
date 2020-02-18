package org.ladbury.powerMonitor.control;

public enum CommandParameters
{
    // Where set is allowed the first parameter is the key
    CircuitStatus("CircuitData Status",2), //get & set
        // CircuitData name, get only
        // enabled boolean
    CircuitMap("CircuitData Map",2), //get & set
        //"CircuitName": "String",
        //"ChannelNumber": 5;
    ChannelMap("Channel Map",2),//get & set
        //"ChannelNumber: int
        //"circuitName": "Whole_House",
    ChannelParams("Channel Parameters",7), //get & set
        //"ChannelNumber: int
        //"clamp":"SCT013_5A1V",
        //"maxCurrent":5,
        //"samplingResistor":372.0,
        //"turnsFactor":1800,
        //"calibrationFactor":1.0913432471,
        //" calibrationOffset":-0.0069045
    CircuitData("CircuitData Data",7), //get only
        //"circuit": "Whole_House", get only  ...rest as object
        //"voltage":float,
        //"current":float,
        //"realpower":float,
        //"reactivepower":float,
        //"apparentpower":float,
        //"energy":float;
    CircuitNames("CircuitData names",1); //get & set

    final String displayName;
    final int numberOfValues;
    CommandParameters(String displayName, int numberOfValues)
    {
        this.displayName=displayName;
        this.numberOfValues = numberOfValues;
    }
}
