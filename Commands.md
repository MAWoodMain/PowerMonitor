# PowerMonitor Commands
## Command line parameters
Some basic configuration can be set via the command line, some of these are required for communication so can only be supplied by this method.
  1. -h to get help
  2. -m <ip address> to set the MQQT server address
  3. -c <client name> to set a client name (also used in MQTT topics)
  4. -i <number of minutes> to set the accumulation interval for energy collection and output
The parameters would normally be set in the PowerMonitor.service file, as the PowerMonitor should be run as a service. see example below.
```
ExecStart=/bin/sh -c "exec sudo java -jar /opt/PowerMonitor/Software/deployment/PowerMonitor.jar -m 10.0.128.2 -i 5 -c PMon10"
```
## Basics
PowerMonitor can receive commands via MQTT, the commands need to be sent in JSON format on the topic /emon/device name/cmnd. Replies will be sent on the topic /emon/device name/response. NB currently data changes are not persisent through restarts of the service.
NB All fields must be completed, as "" if not required.
The command structure is:
```
  {
    "command":"<command>",
    "subject":"<subject>",
    "key":"<key>",
    "data":"<data>"
  } 
```
## Commands
The following commands are available -
  1. Get - gets some data from the PowerMonitor
  2. Set - Sets some data on the PowerMonitor
## Subjects
The following subects are available
  1. circuit        - parameters for circuits
  2. clamp          - parameters for current clamps
  3. metricreading  - A metric reading
  4. powerdata      - Multiple basic and power latest readings for a circuit
  5. energydata     - Latest energy and cumulative energy readings for a circuit
## Keys
The following key values are permitted
  1. for a circuit: the channel number of the circuit as a string, the circuit tag (the name with spaces replaced by underscores)
  2. for a clamp: the clamp name
  3. for a metric reading: the same keys as for circuit (but see data section)
  4. for powerdata: the same keys as for circuit
  5. for energydata: the same keys as a circuit
## Data
### For Get Commands
Only get metric <circuit> optionally requires information in the data field, if "" is specified the "Amps" will be used. possible values are : volts, milli_volts, amps, milli_amps, watt_hours, kilowatt_hours, va, var, kilowatt, powerfactor.
### For Set Commands
For set commands a pair of values are required within the data string
#### Set Clamp <key>
  * "offset <floating point number (up to double precision)>" - added to the reading to correct offset errors
  * "scale <floating point number (up to double precision)>"  - multiplied by the reading to correct scaling errors
#### Set Circuit <key>
  * "displayname <new display name (may contain spaces)" - essentially renaming the circuit
  * "clampname <clampname>" - changing the type of clamp associated with the circuit
  * "monitor <true or false>" - changing if data is collected for the circuit anything that is not true is considered as false
  * "publishpower <true or false>" - if not true basic & power data is collected without being streamed
  * "publishenergy <true or false>" - if not true energy data is collected without being streamed
## Responses
Successful Get and Set commands will respond with the current value of the subject identified.
## Errors
Errors will result in a reponse record wich has the following fields
  1. command - the full command that generated the response.
  2. result - normally "Error", could be "Info" if the request was valid but can't be satisfied
  3. detail - normally further information about the result
  4. location - where the response was generated from (normally a method name)
  
