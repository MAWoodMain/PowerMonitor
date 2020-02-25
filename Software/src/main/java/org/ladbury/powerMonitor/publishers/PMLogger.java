package org.ladbury.powerMonitor.publishers;

import com.google.gson.Gson;
import org.ladbury.powerMonitor.Main;

import java.time.Instant;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public class PMLogger extends Thread
{
    String msg;
    final LinkedBlockingQueue<String> loggingQ;
    final Gson gson ;

    public PMLogger(LinkedBlockingQueue<String> logQ)
    {
        this.loggingQ = logQ;
        this.gson = new Gson();
    }

    private static class LogMsg {
        String time;
        String level;
        String logMsg;
        String location;


        LogMsg(String logMsg){
            this.time = Instant.now().toString();
            this.logMsg = logMsg;
            this.location = "";
            this.level = Level.INFO.getName();
        }
        LogMsg(String logMsg, Level level, String location){
            this.time = Instant.now().toString();
            this.logMsg = logMsg;
            this.location = location;
            this.level = level.getName();
        }

        @Override
        public String toString()
        {
            return "LogMsg{"+ time + " " + level + " " + logMsg + " from: " + location  + "}";
        }
    }
    public void add( String msg, Level level, String location){
        loggingQ.add(new LogMsg(msg, level, location).toString());
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
        LogMsg logmsg;
        boolean exit = false;
        try {
            while (!(interrupted() || exit)) {
                msg = loggingQ.take();
                if (msg.equalsIgnoreCase("exit")) {
                    exit = true;
                } //poison pill
                logmsg = new LogMsg(msg);
                json =  gson.toJson(logmsg);
                publisher.logToBroker(json);
                Thread.sleep(10);
            }
        } catch (InterruptedException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
