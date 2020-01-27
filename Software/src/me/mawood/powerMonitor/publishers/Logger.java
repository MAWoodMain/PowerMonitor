package me.mawood.powerMonitor.publishers;

import me.mawood.powerMonitor.Main;

import java.time.Instant;
import java.util.Queue;

import static me.mawood.powerMonitor.Main.getPowerDataMQTTPublisher;

public class Logger extends Thread
{
    String msg;
    Queue<String> loggingQ ;

    public Logger()
    {
        this.loggingQ =  Main.getLoggingQ();
    }
    //
    // Runnable implementation
    //
    @Override
    public void run()
    {
        PowerDataMQTTPublisher publisher = getPowerDataMQTTPublisher();
        String json;
        boolean exit = false;
        while (!(interrupted() || exit)) {
            msg = loggingQ.poll();
            if (msg.equalsIgnoreCase("exit")){exit=true;} //poison pill
            json =  "{\"Time\":" +
                    "\""+ Instant.now().toString()+ "\"," +
                    "\"LogMsg\":" +
                    "\""+ msg+ "\"}";
            publisher.logToBroker(json);
        }
    }
}
