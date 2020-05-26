package org.ladbury.powerMonitor.PMhealth;

import com.google.gson.Gson;
import org.ladbury.powerMonitor.Main;
import org.ladbury.powerMonitor.publishers.MQTTHandler;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MemoryMonitor
{
    private long intervalInMins;
    private final LinkedBlockingQueue<String> loggingQ;
    private final ScheduledExecutorService memoryCheckScheduler = Executors.newScheduledThreadPool(1);
    private final Runnable memoryChecker;
    private final MQTTHandler publisher = Main.getMqttHandler();
    private final Gson gson;

    // Constructor
    public MemoryMonitor(int intervalInMins)
    {
        this.intervalInMins = intervalInMins;
        this.loggingQ = Main.getLoggingQ();
        this.gson = new Gson();
        // Define functions to be called by timers
        memoryChecker = () -> {
            loggingQ.add("#"+ getCurrentHeapSize()+"#Current heap size");
            publisher.publishToBroker(publisher.getTelemetryTopic(),gson.toJson(new MemoryData()));
        };
        startScheduledTasks();
    }

    public long getTotalHeapSize() {return Runtime.getRuntime().totalMemory();}
    public long getCurrentHeapSize() {return Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();}
    public long getIntervalInMins() {return intervalInMins;}

    void startScheduledTasks()
    {
        loggingQ.add("MemoryMonitor: start");
        try {
            //schedule the bucket filler
            memoryCheckScheduler.scheduleAtFixedRate(
                    memoryChecker,
                    2,
                    getIntervalInMins() * 60, //convert to seconds
                    SECONDS);
        } catch (Exception e) {
            loggingQ.add("MemoryMonitor: Exception - " + Arrays.toString(e.getStackTrace()));
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
        loggingQ.add("MemoryMonitor: stopped memory monitoring");
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
            loggingQ.add("MemoryMonitor: Memory monitoring rescheduled");
        } catch (Exception e) {
            loggingQ.add("MemoryMonitor: Exception - " + Arrays.toString(e.getStackTrace()));
        }
    }
}
