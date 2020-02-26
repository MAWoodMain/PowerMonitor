# PowerMonitor
This is a tool for monitoring household electrical power using multi-channel  clamp based hardware, it defaults to monitoring at a 1 second rate. Although it could run faster than this, it hasn't been tested at higher rates.

The orignal concept was to make energy disagregation easier by providing a capability to monitor the main meter feed to a house, and all of the individual circuits. So the hardware allows for one 100 Amp current clamp (with burden resitor on the board), and 8 other clamps without burden resistors for use with lower power clamps which include thier own burden resistors. Currently only the main meter feed (whole house) is being used, but the other circuits do operate (with appropriate configuration), but haven't been exaustively tested. 

## Software
The software has been developed in Java. Java language level 8 is used because of limited availability of a Java run time for the RPI Zero W above 8. Originally the software was developed to use an API interface for updating a database, but all of the more recent deveopment has focused on using an MQTT interface with JSON encoded messages. This is to fit in with the MING stack (MQTT, Influx, Node-RED, Grafana) for transmitting, storing processing and displaying the data.

## Firmware
The firmware written in C is based on that produced by the [EmonPi project](https://github.com/openenergymonitor/emonpi) but has been modified to read multiple circuits, and operates with a shorter cycle time to help with disgregation. As such it needs a A/C to A/C transofrmer to provide a waveform plus a power supply for the RPi

## Hardware
The hardware was developed by [Matt Wood](https://github.com/MAWoodMain) at an early stage of his experimentation with hardware, from my perspective it does a fine job, but he now sees ways it can be done better. Future improvements may include noise reduction and higher accuracy.

## Metrics
The following Metrics are collected
### Basic Metrics
  1. Voltage (Volts)
  2. Current (Amps)
### Power Metrics (calculated from basic metrics)
  1. Real Power (Watts)
  2. Apparent Power (VA - Volt Amps)
  3. Reactive Power (VAR - Volt Amps Reactive)
  4. Power Factor (Real / Apparent)
### Energy Metrics (calculated from power metrics)
  1. Energy over accumulation interval (Watt Hours)
  2. Cumulative Energy today (Kilowatt Hours)

## Current Release
The current release 0.3-alpha has the following improvements over the last release
### Code Structure
  * The code has been fundamentally restructured to allow for commands to alter data, enums have been replaced by classes.
  * The command processor and associated supporting classes have been added
  * All commands and responses are now sent using JSON rather than arbitary text
  * Much of the code has been restructured as a result of the above items
### Commands via MQTT
A number of get and set commands and their responses have been implemented via JSON over MQTT
the full list off the commands and their syntax can be found here [Commands](https://github.com/gjwo/PowerMonitor/blob/master/Commands.md)
. If there are errors in the commands an error response will be sent in most cases.
### Transmitted Data structure changes
The data sent via streaming or polling is now structured as json object reflecting java data structures shown below
```$xslt
    String time;
    String device;
    int channel;
    String circuitTag;
    Circuit<type>Readings readings; for <type> see below
Power Readings
    Double voltage;
    Double current;
    Double realPower;
    Double reactivePower;
    Double apparentPower;
    Double powerFactor;
Energy Readings
    Double energy;
    Double cumulativeEnergy; 
```
Each data structure transmitted now contains the name of the device and circuit, to make interpretation easier in receiving systems such as NodeRED.
### Streaming or Polling suported
By default the PowerMonitor will still stream data, but it is possible to turn streaming off,
using the Circuit publishPower & Publish Energy settings, setting these false will stop
streaming for that type of data.
NB the data is still collected internally and be be retrieved via the get powerdata and 
get energydata commands i.e. polling. These commands currently return the latest data only.

It is also possible to not monitor circuits at at all by setting the monitor setting 
to false for the circuit.

##Future Plans
I am open to suggestions, but things I am thinking about include:
  * Retrieval of data for a specific period
  * Making some data persistant (energy for example)
  * Logging improvements including using a more formal logging system and being able to turn
   logging on for a specific level remotely, and also log to file locally.
   
The focus may shift to doing things with the data collected for the next little while, as this
release does most of what I need it to.
  
  