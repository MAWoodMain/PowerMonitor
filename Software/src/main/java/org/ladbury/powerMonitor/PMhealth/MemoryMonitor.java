package org.ladbury.powerMonitor.PMhealth;

import com.google.gson.Gson;
import org.ladbury.powerMonitor.Main;
import org.ladbury.powerMonitor.publishers.MQTTHandler;
import org.ladbury.powerMonitor.publishers.PMLogger;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

import static java.util.concurrent.TimeUnit.SECONDS;

public class MemoryMonitor
{
    private long intervalInMins;
    private final PMLogger logger;
    private final ScheduledExecutorService memoryCheckScheduler = Executors.newScheduledThreadPool(1);
    private final Runnable memoryChecker;
    private final MQTTHandler publisher = Main.getMqttHandler();
    private final Gson gson;

    // Constructor
    public MemoryMonitor(int intervalInMins)
    {
        this.intervalInMins = intervalInMins;
        this.logger = Main.getLogger();
        this.gson = new Gson();
        // Define functions to be called by timers
        memoryChecker = () -> {
            logger.add("#"+ getCurrentHeapSize()+"#Current heap size", Level.INFO,this.getClass().getName());
            publisher.publishToBroker(publisher.getTelemetryTopic()+ "/" + "internal",gson.toJson(new MemoryData()));
        };
        startScheduledTasks();
    }

    public long getTotalHeapSize() {return Runtime.getRuntime().totalMemory();}
    public long getCurrentHeapSize() {return Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();}
    public long getIntervalInMins() {return intervalInMins;}

    void startScheduledTasks()
    {
        logger.add("Start", Level.INFO,this.getClass().getName());
        try {
            //schedule the bucket filler
            memoryCheckScheduler.scheduleAtFixedRate(
                    memoryChecker,
                    2,
                    getIntervalInMins() * 60, //convert to seconds
                    SECONDS);
        } catch (Exception e) {
            logger.add("Exception - " + Arrays.toString(e.getStackTrace()), Level.SEVERE,this.getClass().getName());
        }
    }
    void stopMemoryMonitorScheduler()
    {
        memoryCheckScheduler.shutdown();
        boolean shutdown = false;
        while (!shutdown){
            try {
                shutdown = memoryCheckScheduler.awaitTermination(10, SECONDS);
            } catch (InterruptedException e) {
                shutdown = true;
            }
        }
        logger.add("Stopped memory monitoring", Level.INFO,this.getClass().getName());
    }
    void rescheduleMemoryMonitor(long intervalInMins)
    {
        this.intervalInMins = intervalInMins;
        stopMemoryMonitorScheduler();
        try {
            //schedule the memory monitor
            memoryCheckScheduler.scheduleAtFixedRate(
                    memoryChecker,
                    2,
                    intervalInMins * 60, //convert to seconds
                    SECONDS);
            logger.add("Memory monitoring rescheduled every "+intervalInMins+" Mins", Level.CONFIG,this.getClass().getName());
        } catch (Exception e) {
            logger.add("Exception - " + Arrays.toString(e.getStackTrace()), Level.SEVERE,this.getClass().getName());
        }
    }
}
