package me.mawood.powerMonitor.packets.monitors;

import me.mawood.powerMonitor.metrics.MetricReading;
import me.mawood.powerMonitor.packets.monitors.configs.CurrentClampConfig;
import me.mawood.powerMonitor.metrics.units.Current;
import me.mawood.powerMonitor.packets.Packet;
import me.mawood.powerMonitor.packets.PacketCollector;

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
                config.offsetValue(config.scaleValue(packet.getIRms(channelNumber))), packet.getTimestamp(), Current.AMPS);
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
