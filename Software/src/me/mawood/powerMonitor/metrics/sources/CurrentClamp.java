package me.mawood.powerMonitor.metrics.sources;

import me.mawood.powerMonitor.metrics.Metric;
import me.mawood.powerMonitor.metrics.sources.configs.CurrentClampConfig;
import me.mawood.powerMonitor.metrics.units.Current;
import me.mawood.powerMonitor.packets.PacketCollector;
import me.mawood.powerMonitor.packets.PacketEventListener;
import me.mawood.powerMonitor.packets.Packet;

import java.util.ArrayList;
import java.util.Collection;

public class CurrentClamp implements PacketEventListener
{
    private final Collection<Metric<Current>> iRmsMetrics;

    private final byte channelNumber;
    private final CurrentClampConfig config;

    public CurrentClamp(byte channelNumber, CurrentClampConfig config, PacketCollector packetCollector)
    {
        this(channelNumber,config);
        packetCollector.addPacketEventListener(this);
    }

    public CurrentClamp(byte channelNumber,CurrentClampConfig config)
    {
        this.channelNumber = channelNumber;
        this.config = config;
        iRmsMetrics = new ArrayList<>();
    }

    public Collection<Metric<Current>> getIRmsMetrics()
    {
        return iRmsMetrics;
    }


    public Metric<Current> getRunningAverageIRmsMetric()
    {
        return new Metric<>(iRmsMetrics.stream().mapToDouble(Metric::getValue).sum()/ iRmsMetrics.size(), Current.AMPS);
    }

    public Metric<Current> getRunningAverageIRmsMetricAndClear()
    {
        Metric<Current> metric = getRunningAverageIRmsMetric();
        iRmsMetrics.clear();
        return metric;
    }

    public void clearMetrics()
    {
        iRmsMetrics.clear();
    }

    @Override
    public void handleNewPackets(Collection<Packet> newPackets)
    {
        for(Packet packet:newPackets) processPacket(packet);
    }

    private void processPacket(Packet packet)
    {
        if(packet.hasChannel(channelNumber))
        {
            // TODO: implement compensation/calibration for current
            iRmsMetrics.add(new Metric<>(
                    (getScaleFactor()*packet.getIRms(channelNumber))+getOffset(), Current.AMPS));
        }
    }

    private double getScaleFactor()
    {
        return ((double)config.getTurnsFactor())/config.getSamplingResistor();
    }

    private double getOffset()
    {
        // TODO: add cal
        return 0;
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
        return String.format("Current Clamp: {Channel: %d, IRms: {%s}}",
                channelNumber,getRunningAverageIRmsMetric());
    }
}
