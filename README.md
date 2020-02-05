# PowerMonitor
This is a tool for monitoring household electrical power using multi-channel  clamp based hardware.

The orignal concept was to make energy disagregation easier by providing a capability to monitor the main meter feed to a house, and all of the individual circuits. So the hardware allows for oen 100 Amp current clamp (with burden resitor on the board), and 8 other clamps without burden resistors for use with lower power clamps which include thier own burden resistors.

Originally the software was developed to use an API interface for updating a database, but all of the more recent deveopment has focused on using an MQTT interface with JSON encoded messages. This is to fit in with the MING stack (MQTT, Influx, Node-RED, Grafana) for transmitting, storing processing and displaying the data.
