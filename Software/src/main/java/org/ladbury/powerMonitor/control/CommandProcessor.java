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

import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Double.parseDouble;

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

    String setCircuitData(Command command)
    {
        String error = "setCircuit: failed with param-  " + command.toString();
        loggingQ.add(error);
        return error;
    }

    String getClamp(Command command)
    {
        Clamp clamp;
        clamp = Main.getClamps().getClamp(command.getKey());
        if (clamp != null) return gson.toJson(clamp);
        String error = "getClamp: failed command " + command.toString();
        loggingQ.add(error);
        return error;
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
                    loggingQ.add(response.toString());
                    return  gson.toJson(response);
                }
                Clamp clamp;
                clamp = Main.getClamps().getClamp(command.getKey());
                if (clamp != null) {
                    if (elements[0].equalsIgnoreCase("offset")) {
                        clamp.setOffset(value);
                        Main.getClamps().setClamp(command.getKey(),clamp);
                        return gson.toJson(clamp);
                    } else {
                        if (elements[0].equalsIgnoreCase("scale")) {
                            clamp.setScale(value);
                            Main.getClamps().setClamp(command.getKey(), clamp);
                            return gson.toJson(clamp);
                        }
                    }
                }
            }
        }
        response = new CommandResponse(command, "Error", "Bad parameters", "setClamp");
        loggingQ.add(response.toString());
        return  gson.toJson(response);
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
        String error = "getMetricReading: failed command " + command.toString();
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
        String error = "getCircuitData: failed command " + command.toString();
        loggingQ.add(error);
        return error;
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