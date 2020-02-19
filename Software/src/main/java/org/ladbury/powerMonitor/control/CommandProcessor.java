package org.ladbury.powerMonitor.control;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class CommandProcessor extends Thread
{
    String command;
    final LinkedBlockingQueue<String> commandQ;
    final LinkedBlockingQueue<String> loggingQ;

    public CommandProcessor(LinkedBlockingQueue<String> commandQ, LinkedBlockingQueue<String> logQ)
    {
        this.commandQ = commandQ;
        this.loggingQ = logQ;
    }

    /**
     * run  The main Command Processor loop
     */
    public void processSetCommand(String[] params)
    {
        loggingQ.add("Set Command Received: " + Arrays.toString(params));
    }

    public void processGetCommand(String[] params)
    {
        loggingQ.add("Get Command Received: " + Arrays.toString(params));
    }
    //
    // Runnable implementation
    //

    @Override
    public void run()
    {

        String[] commandElements;
        boolean exit = false;
        try {
            while (!(interrupted() || exit)) {
                command = commandQ.take().toLowerCase();
                loggingQ.add("CommandProcessor: <"+command+"> arrived");
                commandElements = command.split(" ");
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
                            loggingQ.add("CommandProcessor: unknown command <"+command+">");
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