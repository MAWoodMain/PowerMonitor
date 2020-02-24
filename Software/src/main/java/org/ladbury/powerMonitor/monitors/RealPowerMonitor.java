package org.ladbury.powerMonitor.monitors;

import org.ladbury.powerMonitor.currentClamps.Clamp;
import org.ladbury.powerMonitor.metrics.Metric;
import org.ladbury.powerMonitor.metrics.MetricReading;
import org.ladbury.powerMonitor.packets.Packet;
import org.ladbury.powerMonitor.packets.PacketCollector;

public class RealPowerMonitor extends Monitor<MetricReading>
{

    private final byte channelNumber;
    private final Clamp currentConfig;
    private final VoltageSenseConfig voltageConfig;

    public RealPowerMonitor(int bufferSize, VoltageSenseConfig voltageConfig, Clamp currentConfig, int channelNumber, PacketCollector packetCollector)
    {
        super(bufferSize);
        this.channelNumber = (byte)channelNumber;
        this.currentConfig = currentConfig;
        this.voltageConfig = voltageConfig;
        packetCollector.addPacketEventListener(this);
    }

    @Override
    protected MetricReading processPacket(Packet packet)
    {
        double value = packet.getRealPower(channelNumber);
        // Scale
        value = currentConfig.scaleValue(value);
        value = voltageConfig.scaleValue(value);
        // Offset
        value = currentConfig.offsetValue(value);
        value = voltageConfig.offsetValue(value);

        return new MetricReading(value,packet.getTimestamp(), Metric.WATTS);
    }

    @Override
    public String toString()
    {
        return String.format("Power Monitor: {Real Power: {%s}}",
                ringBuffer.getNewest());
    }
}
