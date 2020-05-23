package org.ladbury.powerMonitor.circuits;

import org.ladbury.powerMonitor.Main;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.*;

import static java.time.LocalDateTime.now;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ladbury.powerMonitor.Main.getHeapGrowth;

class EnergyBucketFiller
{
    private long intervalInMins;
    private int bucketToFill;
    private final LinkedBlockingQueue<String> loggingQ;
    private final ScheduledExecutorService bucketFillScheduler = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService dailyReset = Executors.newScheduledThreadPool(1);
    private final CircuitCollector circuitCollector;
    private final Runnable filler;
    private final Runnable resetter;

    EnergyBucketFiller(int intervalInMins, CircuitCollector circuitCollector)
    {
        this.intervalInMins = intervalInMins;
        this.bucketToFill = 0;
        this.loggingQ = Main.getLoggingQ();
        this.circuitCollector = circuitCollector;
        // Define functions to be called by timers
        filler = () -> {
            loggingQ.add("Heap growth before fill = "+ getHeapGrowth());
            //fill buckets now
            this.circuitCollector.fillAllEnergyBuckets(bucketToFill);
            this.circuitCollector.publishEnergyMetricsForCircuits(); // publishing decided on a per circuit basis
            //loggingQ.add("EnergyBucketFiller: bucket(s) " + bucketToFill.toString() + " filled ");
            bucketToFill += 1;
            loggingQ.add("Heap growth after fill = "+ getHeapGrowth());
        };
        resetter = () -> {
            //fill buckets now
            loggingQ.add("Heap growth before reset = "+ getHeapGrowth());
            circuitCollector.resetAllEnergyBuckets();
            bucketToFill = 0;
            loggingQ.add("EnergyBucketFiller: buckets reset");
            loggingQ.add("Heap growth after reset = "+ getHeapGrowth());
        };
    }
    public long getIntervalInMins(){return intervalInMins;}

    private long calculateInitialDelayAndSetBucket()
    {
        //work out when to start
        LocalDateTime localNow = now(ZoneId.of("Europe/London"));
        LocalDateTime nextCall = localNow.truncatedTo(ChronoUnit.DAYS); //start of today
        while (nextCall.isBefore(localNow)) {
            nextCall = nextCall.plusMinutes(intervalInMins);
            bucketToFill += 1;
        }
        return localNow.until(nextCall, ChronoUnit.SECONDS);
    }
    void startScheduledTasks()
    {
        loggingQ.add("EnergyBucketFiller: start");
        try {
            //schedule the bucket filler
            bucketFillScheduler.scheduleAtFixedRate(
                    filler,
                    calculateInitialDelayAndSetBucket(),
                    intervalInMins * 60, //convert to seconds
                    SECONDS);

            //Work out when to reset
            LocalDateTime localNow = now(ZoneId.of("Europe/London"));
            LocalDateTime todayMidnight = localNow.truncatedTo(ChronoUnit.DAYS); //start of today
            LocalDateTime tomorrowMidnight = todayMidnight.plusDays(1); //start of tomorrow
            //schedule reset at tomorrow midnight
            dailyReset.scheduleAtFixedRate(
                    resetter,
                    localNow.until(tomorrowMidnight, ChronoUnit.SECONDS),
                    TimeUnit.DAYS.toSeconds(1),
                    SECONDS);
            loggingQ.add("EnergyBucketFiller: both tasks scheduled");
        } catch (Exception e) {
            loggingQ.add("EnergyBucketFiller: Exception - " + Arrays.toString(e.getStackTrace()));
        }
    }
    void stopBucketFillScheduler()
    {
        bucketFillScheduler.shutdown();
        boolean shutdown = false;
        while (!shutdown){
            try {
                shutdown = bucketFillScheduler.awaitTermination(10, SECONDS);
            } catch (InterruptedException e) {
                shutdown = true;
            }
        }
        loggingQ.add("EnergyBucketFiller: stopped bucket fill scheduler");
    }
    void rescheduleBucketFiller(long intervalInMins)
    {
        this.intervalInMins = intervalInMins;
        stopBucketFillScheduler();
        try {
            //schedule the bucket filler
            bucketFillScheduler.scheduleAtFixedRate(
                    filler,
                    calculateInitialDelayAndSetBucket(),
                    intervalInMins * 60, //convert to seconds
                    SECONDS);
            loggingQ.add("EnergyBucketFiller: bucket filling rescheduled");
        } catch (Exception e) {
            loggingQ.add("EnergyBucketFiller: Exception - " + Arrays.toString(e.getStackTrace()));
        }
    }
}