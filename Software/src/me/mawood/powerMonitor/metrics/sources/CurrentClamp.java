package me.mawood.powerMonitor.metrics.sources;

import me.mawood.powerMonitor.*;
import me.mawood.powerMonitor.metrics.Metric;
import me.mawood.powerMonitor.metrics.units.Power;

import java.util.ArrayList;
import java.util.Collection;

public class CurrentClamp implements PacketEventListener
{
    private final Collection<Metric<Power>> apparentMetric;
    private final Collection<Metric<Power>> realMetric;

    private final byte channelNumber;
    private final CurrentClampConfig config;

    public CurrentClamp(byte channelNumber, CurrentClampConfig config, PowerMonitor powerMonitor)
    {
        this(channelNumber,config);
        powerMonitor.addPacketEventListener(this);
    }

    public CurrentClamp(byte channelNumber,CurrentClampConfig config)
    {
        this.channelNumber = channelNumber;
        this.config = config;
        realMetric = new ArrayList<>();
        apparentMetric = new ArrayList<>();
    }

    public Collection<Metric<Power>> getApparentMetric()
    {
        return apparentMetric;
    }

    public Collection<Metric<Power>> getRealMetric()
    {
        return realMetric;
    }

    public Metric<Power> getRunningAverageApparentMetric()
    {
        return new Metric<>(apparentMetric.stream().mapToDouble(Metric::getValue).sum()/apparentMetric.size(), Power.WATTS);
    }

    public Metric<Power> getRunningAverageApparentMetricAndClear()
    {
        Metric<Power> metric = getRunningAverageApparentMetric();
        apparentMetric.clear();
        return metric;
    }

    public Metric<Power> getRunningAverageRealMetric()
    {
        return new Metric<>(realMetric.stream().mapToDouble(Metric::getValue).sum()/realMetric.size(), Power.WATTS);
    }

    public Metric<Power> getRunningAverageRealMetricAndClear()
    {
        Metric<Power> metric = getRunningAverageRealMetric();
        realMetric.clear();
        return metric;
    }

    public void clearMetrics()
    {
        apparentMetric.clear();
        realMetric.clear();
    }

    @Override
    public void handleNewPackets(Collection<PowerMonitorPacket> newPackets)
    {
        for(PowerMonitorPacket packet:newPackets) processPacket(packet);
    }

    private void processPacket(PowerMonitorPacket packet)
    {
        if(packet.hasChannel(channelNumber))
        {
            apparentMetric.add(new Metric<>(
                    (getScaleFactor()*packet.getApparentPower(channelNumber))+getOffset(), Power.WATTS));
            realMetric.add(new Metric<>(
                    (getScaleFactor()*packet.getRealPower(channelNumber))+getOffset(), Power.WATTS));
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

    @Override
    public String toString()
    {
        return String.format("Current Clamp: {Channel: %d, Apparent: %s, Real: %s}",
                channelNumber,getRunningAverageApparentMetricAndClear(),getRunningAverageRealMetricAndClear());
    }
}
