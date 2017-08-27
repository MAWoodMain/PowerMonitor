package me.mawood.powerMonitor.metrics.monitors;

import me.mawood.powerMonitor.metrics.Metric;
import me.mawood.powerMonitor.packets.Packet;
import me.mawood.powerMonitor.packets.PacketEventListener;

import java.util.Collection;

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

    protected abstract E processPacket(Packet packet);
}
