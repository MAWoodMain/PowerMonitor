package me.mawood.powerMonitor.metrics.monitors;

import me.mawood.powerMonitor.metrics.Metric;
import me.mawood.powerMonitor.metrics.monitors.configs.CurrentClampConfig;
import me.mawood.powerMonitor.metrics.units.Current;
import me.mawood.powerMonitor.packets.Packet;
import me.mawood.powerMonitor.packets.PacketCollector;

public class CurrentMonitor extends Monitor<Metric<Current>>
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
    protected Metric<Current> processPacket(Packet packet)
    {
        return new Metric<>(
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
