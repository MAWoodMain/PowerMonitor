package org.ladbury.powerMonitor.publishers;

import org.ladbury.powerMonitor.Main;

import java.time.Instant;
import java.util.concurrent.LinkedBlockingQueue;

public class PMLogger extends Thread
{
    String msg;
    final LinkedBlockingQueue<String> loggingQ;

    public PMLogger(LinkedBlockingQueue<String> logQ)
    {
        this.loggingQ = logQ;
    }

    //
    // Runnable implementation
    //
    @Override
    public void run()
    {
        try {
            Thread.sleep(1000); // wait for publisher to be set up
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MQTTHandler publisher = Main.getMqttHandler();
        String json;
        boolean exit = false;
        try {
            while (!(interrupted() || exit)) {
                msg = loggingQ.take();
                if (msg.equalsIgnoreCase("exit")) {
                    exit = true;
                } //poison pill
                json = "{\"Time\":" +
                        "\"" + Instant.now().toString() + "\"," +
                        "\"LogMsg\":" +
                        "\"" + msg + "\"}";
                publisher.logToBroker(json);
                Thread.sleep(10);
            }
        } catch (InterruptedException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
