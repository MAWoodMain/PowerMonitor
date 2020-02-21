package org.ladbury.powerMonitor.control;

import com.google.gson.Gson;
import org.ladbury.powerMonitor.Main;
import org.ladbury.powerMonitor.circuits.Circuit;
import org.ladbury.powerMonitor.circuits.Circuits;
import org.ladbury.powerMonitor.currentClamps.Clamp;
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
        this.commands = new Commands();
        this.mqqtHandler = Main.getMqttHandler();
        this.gson = new Gson();
    }

    /**
     * run  The main Command Processor loop
     */
    private void processSetCommand(String[] params)
    {
        loggingQ.add("Set Command Received: " + Arrays.toString(params));
    }

    private String getCircuit(String[] keys)
    {
        int channel;
        Circuit circuit;
        try {
            channel = Integer.parseInt(keys[0]);
        }
        catch (NumberFormatException e)
        {
            channel = -1;
        }
        if (channel != -1)
        {
            if (Circuits.validChannel(channel))
            {
                circuit = Main.getCircuits().getCircuit(channel);
                return gson.toJson(circuit);
            }
        }
        else
        {
            //assume we have a circuit name
            channel = Main.getCircuits().getChannelByName(keys[0]);
            if (Circuits.validChannel(channel)) {
                circuit = Main.getCircuits().getCircuit(channel);
                return gson.toJson(circuit);
            }
        }
        loggingQ.add("getCircuit: failed with param - " + Arrays.toString(keys));
        return null;
    }
    private String getClamp(String[] keys)
    {
        Clamp clamp;
        clamp = Main.getClamps().getClamp(keys[0]);
        if (clamp != null)return gson.toJson(clamp);
        loggingQ.add("getClamp: failed with param - " + Arrays.toString(keys));
        return null;
    }

    private void processGetCommand(String[] params)
    {
        loggingQ.add("Get Command Received: " + Arrays.toString(params));
        String subject = params[0];
        String[] keys = Arrays.copyOfRange(params, 1, params.length);
        String json;
        Command command = commands.getCommand("get",subject);
        if (command == null) {
            loggingQ.add("Get Command subject not found");
            return;
        }
        switch (subject)
        {
            case "circuit": {
                loggingQ.add("Get circuit");
                json = getCircuit(keys);
                if (json != null) {
                    mqqtHandler.publishToBroker(mqqtHandler.getResponseTopic(), json);
                } else loggingQ.add("failed to get json for circuit");
                break;
            }
            case "clamp": {
                loggingQ.add("Get clamp");
                json = getClamp(keys);
                if (json != null) {
                    mqqtHandler.publishToBroker(mqqtHandler.getResponseTopic(), json);
                } else loggingQ.add("failed to get json for clamp");
                break;
            }
            case "metricreading": {
                loggingQ.add("Get metric reading not implemented");
                break;
            }
            case "circuitdata": {
                loggingQ.add("Get circuit data not implemented");
                break;
            }
            default: loggingQ.add("Get Command subject not handled");
        }
    }
    //
    // Runnable implementation
    //

    @Override
    public void run()
    {
        String commandString;
        String[] commandElements;

        boolean exit = false;
        try {
            while (!(interrupted() || exit)) {
                commandString = commandQ.take().toLowerCase();
                loggingQ.add("CommandProcessor: <"+ commandString +"> arrived");
                commandElements = commandString.split(" ");
                if (commandElements.length >=1) { //ignore if no elements
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
                            loggingQ.add("CommandProcessor: unknown command <"+ commandString +">");
                        }
                    }
                }
                Thread.sleep(10);
            }
            loggingQ.add("CommandProcessor: Exiting");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}