package org.ladbury.powerMonitor.control;

import com.google.gson.Gson;
import org.ladbury.powerMonitor.Main;
import org.ladbury.powerMonitor.circuits.Circuit;
import org.ladbury.powerMonitor.circuits.CircuitData;
import org.ladbury.powerMonitor.circuits.Circuits;
import org.ladbury.powerMonitor.currentClamps.Clamp;
import org.ladbury.powerMonitor.metrics.Metric;
import org.ladbury.powerMonitor.metrics.MetricReading;
import org.ladbury.powerMonitor.publishers.MQTTHandler;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class CommandProcessor extends Thread
{
    final LinkedBlockingQueue<String> commandQ;
    final LinkedBlockingQueue<String> loggingQ;
    final Commands commands;
    final MQTTHandler mqqtHandler;
    final Gson gson;

    public CommandProcessor(LinkedBlockingQueue<String> commandQ, LinkedBlockingQueue<String> logQ)
    {
        this.commandQ = commandQ;
        this.loggingQ = logQ;
        this.commands = new Commands(this);
        this.mqqtHandler = Main.getMqttHandler();
        this.gson = new Gson();
    }

    /**
     * run  The main Command Processor loop
     */
    /*
    private void processSetCommand(String[] params)
    {
        loggingQ.add("Set Command Received: " + Arrays.toString(params));
    }
    */
    String getCircuit(Command command)
    {
        Circuit circuit;
        int channel = Main.getCircuits().getChannelFromInput(command.getKey());
        if (Circuits.validChannel(channel)) {
            circuit = Main.getCircuits().getCircuit(channel);
            return gson.toJson(circuit);
        }
        String error = "getCircuit: failed with param-  " + command.toString();
        loggingQ.add(error);
        return error;
    }

    String getClamp(Command command)
    {
        Clamp clamp;
        clamp = Main.getClamps().getClamp(command.getKey());
        if (clamp != null) return gson.toJson(clamp);
        String error = "getClamp: failed command "+ command.toString();
        loggingQ.add(error);
        return error;
    }
    String setClamp(Command command){return "";}
    String getMetricReading(Command command)
    {
        Circuit circuit;
        int channel = Main.getCircuits().getChannelFromInput(command.getKey());
        Metric metric = Metric.AMPS;
        MetricReading metricReading;
        if (Circuits.validChannel(channel)) {
            circuit = Main.getCircuits().getCircuit(channel);
            if (command.getData()=="") {
                //no additional parameter stating which metric, use default
                metricReading = Main.getCircuitCollector().getLatestMetricReading(circuit, metric);
            } else {
                for (Metric m : Metric.values()) {
                    if (m.toString().equalsIgnoreCase(command.getData())) {
                        metric = m;
                        break;
                    }
                }
                metricReading = Main.getCircuitCollector().getLatestMetricReading(circuit, metric);
            }
            return gson.toJson(metricReading);
        }
        String error = "getMetricReading: failed command "+ command.toString();
        loggingQ.add(error);
        return error;
    }

    String getCircuitData(Command command)
    {
        Circuit circuit;
        CircuitData circuitData;
        int channel = Main.getCircuits().getChannelFromInput(command.getKey());
        if (Circuits.validChannel(channel)) {
            circuit = Main.getCircuits().getCircuit(channel);
            circuitData = Main.getCircuitCollector().getCircuitData(circuit);
            if (circuitData != null) {
                return gson.toJson(circuitData);
            }
        }
        String error = "getCircuitData: failed command "+command.toString();
        loggingQ.add(error);
        return error;
    }
    String setCircuitData(Command command){return "";}
    /*
    private void processGetCommand(String[] params)
    {
        //loggingQ.add("Get Command Received: " + Arrays.toString(params));
        String subject = params[0];
        String[] keys = Arrays.copyOfRange(params, 1, params.length);
        String json;
        Command command = commands.getCommand("get", subject);
        if (command == null) {
            loggingQ.add("Get Command subject not found");
            return;
        }
        switch (subject) {
            case "circuit": {
                json = getCircuit(keys);
                if (json != null) {
                    mqqtHandler.publishToBroker(mqqtHandler.getResponseTopic(), json);
                } else loggingQ.add("failed to get json for circuit");
                break;
            }
            case "clamp": {
                json = getClamp(keys);
                if (json != null) {
                    mqqtHandler.publishToBroker(mqqtHandler.getResponseTopic(), json);
                } else loggingQ.add("failed to get json for clamp");
                break;
            }
            case "metricreading": {
                json = getMetricReading(keys);
                if (json != null) {
                    mqqtHandler.publishToBroker(mqqtHandler.getResponseTopic(), json);
                } else loggingQ.add("failed to get json for metricreading");
                break;
            }
            case "circuitdata": {
                json = getCircuitData(keys);
                if (json != null) {
                    mqqtHandler.publishToBroker(mqqtHandler.getResponseTopic(), json);
                } else loggingQ.add("failed to get json for getCircuitData");
                break;
            }
            default:
                loggingQ.add("Get Command " + subject + " not handled");
        }
    }
    */
    /*
    private boolean processCommandString(String commandString)
    {
        String[] commandElements;
        boolean exit = false;
        commandElements = commandString.toLowerCase().split(" ");
        if (commandElements.length >= 1) { //ignore if no elements
            switch (commandElements[0]) {
                case "set": {
                    if (commandElements.length > 1) {
                        processSetCommand(Arrays.copyOfRange(commandElements, 1, commandElements.length));
                    } //else not enough arguments
                    break;
                }
                case "get": {
                    if (commandElements.length > 1) {
                        processGetCommand(Arrays.copyOfRange(commandElements, 1, commandElements.length));
                    } //else not enough arguments
                    break;
                }
                case "exit": {
                    exit = true;
                    break;
                }
                default: {
                    loggingQ.add("CommandProcessor: unknown command <" + commandString + ">");
                }
            }
        }
        return exit;
    }
    */

    private boolean processJSONCommandString(String commandString)
    {
        Command command;
        String json;
        command = gson.fromJson(commandString,Command.class);
        if(     command.getCommand() == null ||
                command.getSubject() == null ||
                command.getKey() == null ||
                command.getData() == null)
        {
            loggingQ.add("CommandProcessor: processJSON - contained nulls");
            return false;
        }

        loggingQ.add("CommandProcessor: processJSON " + command.toString());
        json = commands.callCommand(command);
        mqqtHandler.publishToBroker(mqqtHandler.getResponseTopic(), json);
        return false;
    }

        //
    // Runnable implementation
    //

    @Override
    public void run()
    {
        String commandString;
        boolean exit = false;
        try {
            while (!(interrupted() || exit)) {
                commandString = commandQ.take();
                loggingQ.add("CommandProcessor: <" + commandString + "> arrived");
                //exit = processCommandString(commandString);
                exit = processJSONCommandString(commandString);
                Thread.sleep(10);
            }
            loggingQ.add("CommandProcessor: Exiting");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}