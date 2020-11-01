package org.ladbury.powerMonitor.monitors;

import org.ladbury.powerMonitor.metrics.Metric;
import org.ladbury.powerMonitor.metrics.MetricReading;
import org.ladbury.powerMonitor.packets.Packet;
import org.ladbury.powerMonitor.packets.PacketCollector;

public class VoltageMonitor extends Monitor<MetricReading>
{
    private final VoltageSenseConfig config;

    public VoltageMonitor(int bufferSize, VoltageSenseConfig config, PacketCollector packetCollector)
    {
        super(bufferSize);
        this.config = config;
        packetCollector.addPacketEventListener(this);
    }

    @Override
    protected MetricReading processPacket(Packet packet)
    {
        return new MetricReading(
                config.offsetValue(config.scaleValue(packet.getVRms())), packet.getTimestamp(), Metric.VOLTS);
    }

    public VoltageSenseConfig getConfig()
    {
        return config;
    }

    @Override
    public String toString()
    {
        return String.format("Voltage Monitor: {VRms: {%s}}",
                ringBuffer.getNewest());
    }
}
