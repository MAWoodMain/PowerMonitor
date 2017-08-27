package me.mawood.powerMonitor.metrics.monitors;

import me.mawood.powerMonitor.metrics.Metric;
import me.mawood.powerMonitor.packets.Packet;
import me.mawood.powerMonitor.packets.PacketEventListener;

import java.time.Instant;
import java.util.*;

public abstract class Monitor<E extends Metric> implements PacketEventListener
{
    protected final CircularArrayList<E> ringBuffer;

    protected Monitor(int bufferSize)
    {
        ringBuffer = new CircularArrayList<>(bufferSize);
    }

    @Override
    public void handleNewPackets(Collection<Packet> newPackets)
    {
        for(Packet packet:newPackets) ringBuffer.insert(processPacket(packet));
    }

    public E getLatestMetric()
    {
        return ringBuffer.getNewest();
    }

    public E getOldestMetric()
    {
        return ringBuffer.getOldest();
    }

    public List<E> getMetricsFrom(Instant startTime)
    {
        return getMetricsBetween(startTime,Instant.now().plusSeconds(120));
    }

    public List<E> getMetricsBetween(Instant startTime, Instant endTime)
    {
        List<E> output = new ArrayList<>();
        Iterator<E> it = ringBuffer.iterator();
        E value;
        while(it.hasNext())
        {
            value = it.next();
            if(value.getTimestamp().isBefore(endTime) && value.getTimestamp().isAfter(startTime)) output.add(value);
        }
        Collections.sort(output);
        return output;
    }


    protected abstract E processPacket(Packet packet);
}
