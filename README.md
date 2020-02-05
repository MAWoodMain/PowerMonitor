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
The current release 0.1-alpha works for me, but has all parameters hard coded, so you would need to change things like the MQTT server address in the code to make it work for you.

## Future Plans
Things on my to do list include
* Improve the build to use Maven dependencies
* Add command line parameters for MQTT and API configuration
* Add command line parameter for Energy acccumulation interval (current default 5 minutes)
* Interpret commands sent to the RPi over MQTT
* Introduce polling for metrics as an alternative to streaming
