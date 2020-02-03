package me.mawood.powerMonitor.circuits;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.*;

import static java.time.LocalDateTime.now;
import static java.util.concurrent.TimeUnit.SECONDS;

public class EnergyBucketFiller
{
    private long intervalInMins;
    private Integer bucketToFill;
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
        this.publishEnergy = publishEnergy;
        this.loggingQ = loggingQ;
        this.circuitCollector = circuitCollector;
    }

    public void start()
    {
        loggingQ.add("EnergyBucketFiller: start");
        try {
            final Runnable filler = () -> {
                //fill buckets now
                energyStore.fillAllEnergyBuckets(bucketToFill);
                if (publishEnergy) {
                    circuitCollector.publishEnergyMetricsForCircuits();
                }
                loggingQ.add("EnergyBucketFiller: buckets filled " + ((Integer) bucketToFill).toString());
                bucketToFill += 1;
            };

            //work out when to start
            LocalDateTime nextCall = now(ZoneId.of("Europe/London")).truncatedTo(ChronoUnit.DAYS);
            while (nextCall.isBefore(now(ZoneId.of("Europe/London")))) {
                nextCall = nextCall.plusMinutes(intervalInMins);
                bucketToFill += 1;
            }
            loggingQ.add("EnergyBucketFiller: bucketToFill = " + bucketToFill.toString() + "First call "+ nextCall.toString());

            //schedule the bucket filler
            final ScheduledFuture<?> fillerHandle =
                    scheduler.scheduleAtFixedRate(
                            filler,
                            LocalDateTime.now().until(nextCall, ChronoUnit.SECONDS),
                            intervalInMins * 60,
                            SECONDS);

            final Runnable resetter = () -> {
                //fill buckets now
                energyStore.resetAllEnergyAccumulation();
                bucketToFill = 0;
                loggingQ.add("EnergyBucketFiller: buckets reset");
            };

            LocalTime midnight = LocalTime.MIDNIGHT;
            LocalDate today = LocalDate.now(ZoneId.of("Europe/London"));
            LocalDateTime todayMidnight = LocalDateTime.of(today, midnight); //start of today
            LocalDateTime tomorrowMidnight = todayMidnight.plusDays(1); //start of tomorrow

            //reset at midnight
            dailyReset.scheduleAtFixedRate(
                    resetter,
                    LocalDateTime.now().until(tomorrowMidnight, ChronoUnit.SECONDS),
                    TimeUnit.DAYS.toSeconds(1),
                    SECONDS);
            loggingQ.add("EnergyBucketFiller: tasks scheduled");
        } catch (Exception e) {
            loggingQ.add("EnergyBucketFiller: Exception - " + Arrays.toString(e.getStackTrace()));
            //e.printStackTrace();
        }
    }

    public int lastFilledBucket()
    {
        return (bucketToFill>0)? (bucketToFill-1) : -1;
    }
}