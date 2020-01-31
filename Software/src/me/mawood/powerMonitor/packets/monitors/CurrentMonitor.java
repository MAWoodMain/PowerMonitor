package me.mawood.powerMonitor.packets.monitors;

import me.mawood.powerMonitor.metrics.MetricDefinition;
import me.mawood.powerMonitor.metrics.MetricReading;
import me.mawood.powerMonitor.packets.Packet;
import me.mawood.powerMonitor.packets.PacketCollector;
import me.mawood.powerMonitor.packets.monitors.configs.CurrentClampConfig;

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
                config.offsetValue(config.scaleValue(packet.getIRms(channelNumber))), packet.getTimestamp(), MetricDefinition.AMPS);
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
