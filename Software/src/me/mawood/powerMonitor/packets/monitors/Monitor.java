package me.mawood.powerMonitor.packets.monitors;

import me.mawood.powerMonitor.metrics.MetricReading;
import me.mawood.powerMonitor.packets.Packet;
import me.mawood.powerMonitor.packets.PacketEventListener;

import java.time.Instant;
import java.util.*;

public abstract class Monitor<E extends MetricReading> implements PacketEventListener
{
    final CircularArrayList<E> ringBuffer;

    Monitor(int bufferSize)
    {
        ringBuffer = new CircularArrayList<>(bufferSize);
    }

    @Override
    public void handleNewPackets(Collection<Packet> newPackets)
    {
        E value;
        for(Packet packet:newPackets)
        {
            value = processPacket(packet);
            if(value != null) ringBuffer.insert(value);
        }
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
            if(value == null) continue;
            if(value.getTimestamp().isBefore(endTime) && value.getTimestamp().isAfter(startTime)) output.add(value);
        }
        Collections.sort(output);
        return output;
    }


    protected abstract E processPacket(Packet packet);
}
