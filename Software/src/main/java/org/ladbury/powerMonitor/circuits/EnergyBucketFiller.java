package org.ladbury.powerMonitor.circuits;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.*;

import static java.time.LocalDateTime.now;
import static java.util.concurrent.TimeUnit.SECONDS;

public class EnergyBucketFiller
{
    private final long intervalInMins;
    private Integer bucketToFill;
    private final LinkedBlockingQueue<String> loggingQ;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService dailyReset = Executors.newScheduledThreadPool(1);
    private final boolean publishEnergy;
    private final CircuitCollector circuitCollector;

    public EnergyBucketFiller(long intervalInMins,
                              boolean publishEnergy,
                              CircuitCollector circuitCollector,
                              LinkedBlockingQueue<String> loggingQ)
    {
        this.intervalInMins = intervalInMins;
        this.bucketToFill = 0;
        this.publishEnergy = publishEnergy;
        this.loggingQ = loggingQ;
        this.circuitCollector = circuitCollector;
    }

    public void start()
    {
        loggingQ.add("EnergyBucketFiller: start");
        try {
            //
            // Define functions to be called by timers
            //
            final Runnable filler = () -> {
                //fill buckets now
                circuitCollector.fillAllEnergyBuckets(bucketToFill);
                if (publishEnergy) {
                    circuitCollector.publishEnergyMetricsForCircuits();
                }
                //loggingQ.add("EnergyBucketFiller: bucket(s) " + bucketToFill.toString() + " filled ");
                bucketToFill += 1;
            };

            final Runnable resetter = () -> {
                //fill buckets now
                circuitCollector.resetAllEnergyBuckets();
                bucketToFill = 0;
                loggingQ.add("EnergyBucketFiller: buckets reset");
            };

            //work out when to start
            LocalDateTime localNow = now(ZoneId.of("Europe/London"));
            LocalDateTime todayMidnight = localNow.truncatedTo(ChronoUnit.DAYS); //start of today
            LocalDateTime nextCall = todayMidnight;
            while (nextCall.isBefore(localNow)) {
                nextCall = nextCall.plusMinutes(intervalInMins);
                bucketToFill += 1;
            }

            //loggingQ.add("EnergyBucketFiller: schedule bucketToFill = " + bucketToFill.toString() + "First call " + nextCall.toString());

            //schedule the bucket filler
            scheduler.scheduleAtFixedRate(
                    filler,
                    localNow.until(nextCall, ChronoUnit.SECONDS),
                    intervalInMins * 60, //convert to seconds
                    SECONDS);

            //Work out when to reset
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
            //e.printStackTrace();
        }
    }
}