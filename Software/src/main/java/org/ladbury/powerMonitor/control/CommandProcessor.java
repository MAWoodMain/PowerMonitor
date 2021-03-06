package org.ladbury.powerMonitor.control;

import com.google.gson.Gson;
import org.ladbury.powerMonitor.Main;
import org.ladbury.powerMonitor.circuits.Circuit;
import org.ladbury.powerMonitor.circuits.CircuitEnergyData;
import org.ladbury.powerMonitor.circuits.CircuitPowerData;
import org.ladbury.powerMonitor.circuits.Circuits;
import org.ladbury.powerMonitor.currentClamps.Clamp;
import org.ladbury.powerMonitor.metrics.Metric;
import org.ladbury.powerMonitor.metrics.MetricReading;
import org.ladbury.powerMonitor.publishers.MQTTHandler;
import org.ladbury.powerMonitor.publishers.PMLogger;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import static java.lang.Double.parseDouble;

public class CommandProcessor extends Thread
{
    final LinkedBlockingQueue<String> commandQ;
    final PMLogger logger;
    final Commands commands;
    final MQTTHandler mqttHandler;
    final Gson gson;

    // Constructor
    public CommandProcessor(LinkedBlockingQueue<String> commandQ)
    {
        this.commandQ = commandQ;
        this.logger = Main.getLogger();
        this.commands = new Commands(this);
        this.mqttHandler = Main.getMqttHandler();
        this.gson = new Gson();
    }

    String getCircuit(Command command)
    {
        Circuit circuit;
        int channel = Main.getCircuits().getChannelFromInput(command.getKey());
        if (Circuits.validChannel(channel)) {
            circuit = Main.getCircuits().getCircuit(channel);
            return gson.toJson(circuit);
        }
        CommandResponse response = new CommandResponse(command, "Error", "invalid key", "getCircuit");
        return gson.toJson(response);
    }

    @SuppressWarnings("SpellCheckingInspection")
    String setCircuit(Command command)
    {
        Circuit circuit;
        CommandResponse response;
        String data = command.getData();
        String[] elements;
        Clamp clamp;
        int channel = Main.getCircuits().getChannelFromInput(command.getKey());
        if (!Circuits.validChannel(channel)) {
            response = new CommandResponse(command, "Error", "invalid key", "setCircuit");
            return gson.toJson(response);
        }
        if (data == null) {
            response = new CommandResponse(command, "Error", "null data", "setCircuit");
            return gson.toJson(response);

        }
        elements = data.split(" ");
        if (elements.length < 2) {
            response = new CommandResponse(command, "Error", "insufficient data", "setCircuit");
            return gson.toJson(response);
        }
        // required parameters all present
        circuit = Main.getCircuits().getCircuit(channel);
        switch (elements[0].toLowerCase()) {
            //noinspection SpellCheckingInspection
            case "displayname": {
                if (elements[1].equalsIgnoreCase("")) {
                    response = new CommandResponse(command, "Error", "Invalid name", "setCircuit");
                    return gson.toJson(response);
                } else //TODO ought to check name doesn't exist already
                {
                    String newName = elements[1];
                    for (int i = 2; i < elements.length; i++) {
                        newName = newName + " " + elements[i];
                    }
                    Main.getCircuits().setCircuitName(channel, newName);
                }
                break;
            }
            case "clampname": {
                clamp = Main.getClamps().getClamp(elements[1]);
                if (clamp != null) {
                    Main.getCircuits().setClampName(channel, clamp.getClampName());
                } else {
                    response = new CommandResponse(command, "Error", "Invalid Clamp", "setCircuit");
                    return gson.toJson(response);
                }
                break;
            }
            case "monitor": {
                boolean monitor = elements[1].equalsIgnoreCase("true");
                circuit.setMonitoring(monitor);
                if (monitor != Main.getCircuitCollector().isMonitoring(circuit))
                {
                    if (monitor){
                         Main.getCircuitCollector().enableCollection(circuit);
                    }
                    else{
                        Main.getCircuitCollector().disableCollection(circuit);
                    }
                } // else nothing more to do.
                break;
            }
            case "publishpower": {
                circuit.setPublishPower(elements[1].equalsIgnoreCase("true"));
                break;
            }
            case "publishenergy": {
                circuit.setPublishEnergy(elements[1].equalsIgnoreCase("true"));
                break;
            }
            default: {
                response = new CommandResponse(command, "Error", "not supported", "setCircuit");
                return gson.toJson(response);
            }
        }
        return gson.toJson(circuit);
    } //also insufficient data drop through


    String getClamp(Command command)
    {
        Clamp clamp;
        clamp = Main.getClamps().getClamp(command.getKey());
        if (clamp != null) return gson.toJson(clamp);
        CommandResponse response = new CommandResponse(command, "Error", "invalid key", "getClamp");
        return gson.toJson(response);
    }

    String setClamp(Command command)
    {
        String data = command.getData();
        String[] elements;
        double value;
        CommandResponse response;
        if (data != null) {
            elements = data.split(" ");
            if (elements.length == 2) {
                try {
                    value = parseDouble(elements[1]);
                } catch (NumberFormatException e) {
                    response = new CommandResponse(command, "Error", "invalid value", "setClamp");
                    return gson.toJson(response);
                }
                Clamp clamp;
                clamp = Main.getClamps().getClamp(command.getKey());
                if (clamp != null) {
                    if (elements[0].equalsIgnoreCase("offset")) {
                        clamp.setOffset(value);
                        //Main.getClamps().setClamp(command.getKey(), clamp);
                        return gson.toJson(clamp);
                    } else {
                        if (elements[0].equalsIgnoreCase("scale")) {
                            clamp.setScale(value);
                            //Main.getClamps().setClamp(command.getKey(), clamp);
                            return gson.toJson(clamp);
                        }
                    }
                }
            }
        }
        response = new CommandResponse(command, "Error", "Bad parameters", "setClamp");
        return gson.toJson(response);
    }

    String getMetricReading(Command command)
    {
        Circuit circuit;
        int channel = Main.getCircuits().getChannelFromInput(command.getKey());
        Metric metric = Metric.AMPS; //set default
        MetricReading metricReading;
        if (Circuits.validChannel(channel)) {
            circuit = Main.getCircuits().getCircuit(channel);
            if (!command.getData().equals("")) {
                for (Metric m : Metric.values()) {
                    if (m.toString().equalsIgnoreCase(command.getData())) {
                        metric = m;
                        break;
                    }
                }
            }
            //no additional parameter stating which metric, use default (already set)
            metricReading = Main.getCircuitCollector().getLatestMetricReading(circuit, metric);
            return gson.toJson(metricReading);
        }
        CommandResponse response = new CommandResponse(command, "Error", "invalid key", "getMetricReading");
        return gson.toJson(response);
    }

    String getCircuitPowerData(Command command)
    {
        Circuit circuit;
        CircuitPowerData circuitPowerData;
        CommandResponse response;
        int channel = Main.getCircuits().getChannelFromInput(command.getKey());
        if (Circuits.validChannel(channel)) {
            circuit = Main.getCircuits().getCircuit(channel);
            circuitPowerData = Main.getCircuitCollector().getLatestCircuitPowerData(circuit);
            if (circuitPowerData != null) {
                return gson.toJson(circuitPowerData);
            } else {
                response = new CommandResponse(command, "Info", "No data available", "getCircuitPowerData");
                return gson.toJson(response);

            }
        }
        response = new CommandResponse(command, "Error", "invalid key", "getCircuitPowerData");
        return gson.toJson(response);
    }

    String getCircuitEnergyData(Command command)
    {
        Circuit circuit;
        CircuitEnergyData circuitEnergyData;
        CommandResponse response;
        int channel = Main.getCircuits().getChannelFromInput(command.getKey());
        if (Circuits.validChannel(channel)) {
            circuit = Main.getCircuits().getCircuit(channel);
            circuitEnergyData = Main.getCircuitCollector().getCircuitEnergy(circuit);
            if (circuitEnergyData != null) {
                return gson.toJson(circuitEnergyData);
            } else {
                response = new CommandResponse(command, "Info", "No data available", "getCircuitEnergyData");
                return gson.toJson(response);
            }
        }
        response = new CommandResponse(command, "Error", "invalid key", "getCircuitEnergyData");
        return gson.toJson(response);
    }

    private boolean processJSONCommandString(String commandString)
    {
        Command command;
        String json;
        command = gson.fromJson(commandString, Command.class);
        if (command.getCommand() == null ||
                command.getSubject() == null ||
                command.getKey() == null ||
                command.getData() == null) {
            logger.add("CommandProcessor: processJSON - contained nulls", Level.WARNING, this.getClass().getName());
            return false;
        }
        logger.add("CommandProcessor: processing " + command.toString(),Level.INFO, this.getClass().getName());
        json = commands.callCommand(command);
        mqttHandler.publishToBroker(mqttHandler.getResponseTopic(), json);
        return false;
    }

    //
    // Runnable implementation
    //run  The main Command Processor loop
    //
    @Override
    public void run()
    {
        String commandString;
        boolean exit = false;
        try {
            while (!(interrupted() || exit)) {
                commandString = commandQ.take();
                logger.add("CommandProcessor: <" + commandString + "> arrived",Level.INFO, this.getClass().getName());
                //exit = processCommandString(commandString);
                exit = processJSONCommandString(commandString);
                Thread.sleep(10);
            }
            logger.add("CommandProcessor: Exiting", Level.INFO, this.getClass().getName());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}