package me.mawood.powerMonitor.packets.monitors;

import me.mawood.powerMonitor.metrics.Metric;
import me.mawood.powerMonitor.metrics.MetricReading;
import me.mawood.powerMonitor.packets.Packet;
import me.mawood.powerMonitor.packets.PacketCollector;
import me.mawood.powerMonitor.packets.monitors.configs.VoltageSenseConfig;

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
