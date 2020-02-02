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
    long intervalInMins;
    int currentBucket;
    EnergyStore energyStore;
    LinkedBlockingQueue<String> loggingQ;



    public EnergyBucketFiller(EnergyStore energyStore, long intervalInMins,LinkedBlockingQueue<String> loggingQ)
    {
        this.intervalInMins = intervalInMins;
        this.energyStore = energyStore;
        energyStore.resetAllEnergyAccumulation();
        this.currentBucket = 0;
        this.loggingQ = loggingQ;
    }

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    private final ScheduledExecutorService dailyReset =
            Executors.newScheduledThreadPool(1);

    public void start()
    {
        final Runnable filler = new Runnable()
        {
            public void run()
            {
                //fill buckets now
                energyStore.fillAllEnergyBuckets(currentBucket);
                loggingQ.add("EnergyBucketFiller: buckets filled "+ currentBucket);
                currentBucket +=1;
            }
        };

        //work out when to start
        LocalDateTime lastMidnight = now().truncatedTo(ChronoUnit.DAYS);
        LocalDateTime nextCall = lastMidnight;
        while( nextCall.isBefore(now()))
        {
            nextCall.plusMinutes(intervalInMins);
            currentBucket +=1;
        }
        Duration duration = Duration.between(nextCall, Instant.now());

        //schedule the bucket filler
        final ScheduledFuture<?> fillerHandle =
                scheduler.scheduleAtFixedRate(filler, duration.getSeconds(), intervalInMins*60, SECONDS);

        final Runnable resetter = new Runnable()
        {
            public void run()
            {
                //fill buckets now
                energyStore.resetAllEnergyAccumulation();
                currentBucket = 0;
                loggingQ.add("EnergyBucketFiller: buckets reset");
            }
        };

        //reset at midnight
        //not dealt with time changes
        Long timeToMidnight = LocalDateTime.now().until(LocalDate.now().plusDays(1).atStartOfDay(), ChronoUnit.SECONDS);

        dailyReset.scheduleAtFixedRate(resetter, timeToMidnight, TimeUnit.DAYS.toSeconds(1), SECONDS);
        loggingQ.add("EnergyBucketFiller: tasks scheduled");
    }
}
