package me.mawood.powerMonitor.circuits;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MINUTES;

public class EnergyBucketFiller
{
    int intervalInMins;

    EnergyBucketFiller(EnergyStore energyStore, Instant startTime, Instant resetTime, int intervalInMins)
    {
        this.intervalInMins = intervalInMins;
        energyStore.resetAllEnergyAccumulation();
    }

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public void FillBucketAtInterval()
    {
        final Runnable filler = new Runnable()
        {
            public void run()
            {
                //fill buckets now
            }
        };

        final ScheduledFuture<?> fillerHandle =
                scheduler.scheduleAtFixedRate(filler, 10, intervalInMins, MINUTES);

        /*
        scheduler.schedule(new Runnable()
        {
            public void run()
            {
                fillerHandle.cancel(true);
            }
        }, 60 * 60, SECONDS);
        */
    }
}
