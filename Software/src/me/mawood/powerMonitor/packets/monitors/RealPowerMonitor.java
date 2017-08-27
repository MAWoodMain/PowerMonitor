package me.mawood.powerMonitor.packets.monitors;

import me.mawood.powerMonitor.metrics.Metric;
import me.mawood.powerMonitor.packets.monitors.configs.CurrentClampConfig;
import me.mawood.powerMonitor.packets.monitors.configs.VoltageSenseConfig;
import me.mawood.powerMonitor.metrics.units.Power;
import me.mawood.powerMonitor.packets.Packet;
import me.mawood.powerMonitor.packets.PacketCollector;

public class RealPowerMonitor extends Monitor<Metric>
{

    private final byte channelNumber;
    private final CurrentClampConfig currentConfig;
    private final VoltageSenseConfig voltageConfig;

    public RealPowerMonitor(int bufferSize, VoltageSenseConfig voltageConfig, CurrentClampConfig currentConfig, int channelNumber, PacketCollector packetCollector)
    {
        super(bufferSize);
        this.channelNumber = (byte)channelNumber;
        this.currentConfig = currentConfig;
        this.voltageConfig = voltageConfig;
        packetCollector.addPacketEventListener(this);
    }

    @Override
    protected Metric processPacket(Packet packet)
    {
        double value = packet.getRealPower(channelNumber);
        // Scale
        value = currentConfig.scaleValue(value);
        value = voltageConfig.scaleValue(value);
        // Offset
        value = currentConfig.offsetValue(value);
        value = voltageConfig.offsetValue(value);

        return new Metric(value,packet.getTimestamp(), Power.WATTS);
    }

    @Override
    public String toString()
    {
        return String.format("Power Monitor: {Real Power: {%s}}",
                ringBuffer.getNewest());
    }
}
