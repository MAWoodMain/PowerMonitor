package me.mawood.powerMonitor.packets.monitors;

import me.mawood.powerMonitor.metrics.Reading;
import me.mawood.powerMonitor.packets.monitors.configs.VoltageSenseConfig;
import me.mawood.powerMonitor.metrics.units.Voltage;
import me.mawood.powerMonitor.packets.Packet;
import me.mawood.powerMonitor.packets.PacketCollector;

public class VoltageMonitor extends Monitor<Reading>
{
    private final VoltageSenseConfig config;

    public VoltageMonitor(int bufferSize, VoltageSenseConfig config, PacketCollector packetCollector)
    {
        super(bufferSize);
        this.config = config;
        packetCollector.addPacketEventListener(this);
    }

    @Override
    protected Reading processPacket(Packet packet)
    {
        return new Reading(
                config.offsetValue(config.scaleValue(packet.getVRms())), packet.getTimestamp(), Voltage.VOLTS);
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
