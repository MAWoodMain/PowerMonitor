package me.mawood.powerMonitor.metrics.sources;

import me.mawood.powerMonitor.metrics.Metric;
import me.mawood.powerMonitor.metrics.sources.configs.VoltageSenseConfig;
import me.mawood.powerMonitor.metrics.units.Voltage;
import me.mawood.powerMonitor.packets.Packet;
import me.mawood.powerMonitor.packets.PacketCollector;
import me.mawood.powerMonitor.packets.PacketEventListener;

import java.util.ArrayList;
import java.util.Collection;

public class VoltageSensor implements PacketEventListener
{
    private final Collection<Metric<Voltage>> vRmsMetrics;
    private final VoltageSenseConfig config;

    public VoltageSensor(VoltageSenseConfig config, PacketCollector packetCollector)
    {
        this(config);
        packetCollector.addPacketEventListener(this);
    }

    public VoltageSensor(VoltageSenseConfig config)
    {
        this.config = config;
        vRmsMetrics = new ArrayList<>();
    }

    public Collection<Metric<Voltage>> getVRmsMetrics()
    {
        return vRmsMetrics;
    }

    public Metric<Voltage> getRunningAverageVRmsMetric()
    {
        return new Metric<>(vRmsMetrics.stream().mapToDouble(Metric::getValue).sum()/ vRmsMetrics.size(), Voltage.VOLTS);
    }

    public Metric<Voltage> getRunningAverageVRmsMetricAndClear()
    {
        Metric<Voltage> metric = getRunningAverageVRmsMetric();
        vRmsMetrics.clear();
        return metric;
    }

    public void clearMetrics()
    {
        vRmsMetrics.clear();
    }

    @Override
    public void handleNewPackets(Collection<Packet> newPackets)
    {
        for(Packet packet:newPackets) processPacket(packet);
    }

    private void processPacket(Packet packet)
    {
        vRmsMetrics.add(new Metric<>(
                (getScaleFactor()*packet.getVRms())+getOffset(), Voltage.VOLTS));
    }
    private double getOffset()
    {
        return 0;
    }

    public double getScaleFactor()
    {
        return (config.getMainsRms()*VoltageSenseConfig.getMagicMainsConstant())/
                (config.getTransformerRms()*VoltageSenseConfig.getMagicTransformerConstant());
    }

    public VoltageSenseConfig getConfig()
    {
        return config;
    }

    @Override
    public String toString()
    {
        return String.format("Voltage Sensor: {VRms: {%s}}",
                getRunningAverageVRmsMetric());
    }
}
