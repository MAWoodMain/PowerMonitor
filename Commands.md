# PowerMonitor Commands
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
  4. circuitdata    - Multiple readings for a circuit
## Keys
The following key values are permitted
  1. for a circuit: the channel number of the circuit as a string, the circuit tag (the name with spaces replaced by underscores)
  2. for a clamp: the clamp name
  3. for a metric reading: the same keys as for circuit (but see data section)
  4. circuitdata: the same keys as for circuit
## Data
### For Get Commands
Only get metric <circuit> optionally requires information in the data field, if "" is specified the "Amps" will be used. possible values are : volts, milli_volts, amps, milli_amps, watt_hours, kilowatt_hours, va, var, kilowatt, powerfactor.
### For Set Commands
For set commands a pair of values are required within the data string
#### Set Clamp <key>
  "offset <floating point number (up to double precision)>" - added to the reading to correct offset errors
  "scale <floating point number (up to double precision)>"  - multiplied by the reading to correct scaling errors
#### Set Circuit <key>
  "displayname <new display name (may contain spaces)" - essentially renaming the circuit
  "clampname <clampname>" - changing the type of clamp associated with the circuit
  "monitor <true or false>" - changing if data is collected for the circuit anythign that is not true is considered as false
## Responses
Successful Get and Set commands will respond with the current value of the subject identified.
## Errors
Errors will result in a reponse record wich has the following fields
  1. command - the full command that generated the response.
  2. result - normally "Error"
  3. detail - normally further information about the error
  4. location - where the response was generated from (normally a method name)
  
