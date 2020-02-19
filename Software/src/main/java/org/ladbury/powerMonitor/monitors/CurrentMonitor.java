package org.ladbury.powerMonitor.monitors;

import org.ladbury.powerMonitor.metrics.Metric;
import org.ladbury.powerMonitor.metrics.MetricReading;
import org.ladbury.powerMonitor.packets.Packet;
import org.ladbury.powerMonitor.packets.PacketCollector;
import org.ladbury.powerMonitor.monitors.configs.CurrentClampConfig;

public class CurrentMonitor extends Monitor<MetricReading>
{
    private final byte channelNumber;
    private final CurrentClampConfig config;

    public CurrentMonitor(int bufferSize, CurrentClampConfig config, int channelNumber, PacketCollector packetCollector)
    {
        super(bufferSize);
        this.channelNumber = (byte)channelNumber;
        this.config = config;
        packetCollector.addPacketEventListener(this);
    }

    @Override
    protected MetricReading processPacket(Packet packet)
    {
        return new MetricReading(
                config.offsetValue(config.scaleValue(packet.getIRms(channelNumber))), packet.getTimestamp(), Metric.AMPS);
    }

    public CurrentClampConfig getConfig()
    {
        return config;
    }

    public byte getChannelNumber()
    {
        return channelNumber;
    }

    @Override
    public String toString()
    {
        return String.format("Current Monitor: {Channel: %d, IRms: {%s}}",
                channelNumber,ringBuffer.getNewest());
    }
}
