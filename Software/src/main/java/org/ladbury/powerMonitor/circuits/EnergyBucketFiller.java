package org.ladbury.powerMonitor.circuits;

import org.ladbury.powerMonitor.Main;
import org.ladbury.powerMonitor.publishers.PMLogger;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.logging.Level;

import static java.time.LocalDateTime.now;
import static java.util.concurrent.TimeUnit.SECONDS;

class EnergyBucketFiller
{
    private long intervalInMins;
    private int bucketToFill;
    private final PMLogger logger;
    private final ScheduledExecutorService bucketFillScheduler = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService dailyReset = Executors.newScheduledThreadPool(1);
    private final CircuitCollector circuitCollector;
    private final Runnable filler;
    private final Runnable resetter;

    EnergyBucketFiller(int intervalInMins, CircuitCollector circuitCollector)
    {
        this.intervalInMins = intervalInMins;
        this.bucketToFill = 0;
        this.logger = Main.getLogger();
        this.circuitCollector = circuitCollector;
        // Define functions to be called by timers
        filler = () -> {
            //fill buckets now
            this.circuitCollector.fillAllEnergyBuckets(bucketToFill);
            this.circuitCollector.publishEnergyMetricsForCircuits(); // publishing decided on a per circuit basis
            //loggingQ.add("EnergyBucketFiller: bucket(s) " + bucketToFill.toString() + " filled ");
            bucketToFill += 1;
        };
        resetter = () -> {
            //fill buckets now
            //Runtime.getRuntime().gc();
            logger.add("#"+ Main.getMemoryMonitor().getCurrentHeapSize()+"#Heap used before reset", Level.INFO, this.getClass().getName());
            circuitCollector.resetAllEnergyBuckets();
            bucketToFill = 0;
            logger.add("Buckets reset", Level.INFO, this.getClass().getName());
            Runtime.getRuntime().gc(); //once per day garbage collection
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
        logger.add("Start", Level.INFO, this.getClass().getName());
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
            logger.add("Both tasks scheduled", Level.FINE, this.getClass().getName());
        } catch (Exception e) {
            logger.add("Exception - " + Arrays.toString(e.getStackTrace()), Level.SEVERE, this.getClass().getName());
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
        logger.add("Stopped bucket fill scheduler", Level.INFO, this.getClass().getName());
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
            logger.add("Bucket filling rescheduled every "+intervalInMins+ "Mins", Level.CONFIG, this.getClass().getName());
        } catch (Exception e) {
            logger.add("Exception - " + Arrays.toString(e.getStackTrace()), Level.SEVERE, this.getClass().getName());
        }
    }
}