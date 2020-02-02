package me.mawood.powerMonitor.circuits;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;
import static java.time.LocalDateTime.now;
import static java.util.concurrent.TimeUnit.SECONDS;

public class EnergyBucketFiller
{
    private long intervalInMins;
    private int bucketToFill;
    private final EnergyStore energyStore;
    private final LinkedBlockingQueue<String> loggingQ;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService dailyReset = Executors.newScheduledThreadPool(1);
    private boolean publishEnergy;
    private final CircuitCollector circuitCollector;

    public EnergyBucketFiller(EnergyStore energyStore,
                              long intervalInMins,
                              boolean publishEnergy,
                              CircuitCollector circuitCollector,
                              LinkedBlockingQueue<String> loggingQ)
    {
        this.intervalInMins = intervalInMins;
        this.energyStore = energyStore;
        energyStore.resetAllEnergyAccumulation();
        this.bucketToFill = 0;
        this.publishEnergy =publishEnergy;
        this.loggingQ = loggingQ;
        this.circuitCollector = circuitCollector;
    }

    public void start()
    {
        final Runnable filler = () -> {
            //fill buckets now
            energyStore.fillAllEnergyBuckets(bucketToFill);
            if(publishEnergy) {circuitCollector.publishEnergyMetricsForCircuits();}
            loggingQ.add("EnergyBucketFiller: buckets filled "+ bucketToFill);
            bucketToFill +=1;
        };

        //work out when to start
        LocalDateTime nextCall = now().truncatedTo(ChronoUnit.DAYS);
        while( nextCall.isBefore(now()))
        {
            nextCall.plusMinutes(intervalInMins);
            bucketToFill +=1;
        }
        Duration duration = Duration.between(nextCall, Instant.now());

        //schedule the bucket filler
        final ScheduledFuture<?> fillerHandle =
                scheduler.scheduleAtFixedRate(filler, duration.getSeconds(), intervalInMins*60, SECONDS);

        final Runnable resetter = () -> {
            //fill buckets now
            energyStore.resetAllEnergyAccumulation();
            bucketToFill = 0;
            loggingQ.add("EnergyBucketFiller: buckets reset");
        };

        //reset at midnight
        //not dealt with time changes
        Long timeToMidnight = LocalDateTime.now().until(LocalDate.now().plusDays(1).atStartOfDay(), ChronoUnit.SECONDS);

        dailyReset.scheduleAtFixedRate(resetter, timeToMidnight, TimeUnit.DAYS.toSeconds(1), SECONDS);
        loggingQ.add("EnergyBucketFiller: tasks scheduled");
    }
    int lastFilledBucket()
    {
        if(bucketToFill>0) return bucketToFill-1; else  return -1;
    }
}