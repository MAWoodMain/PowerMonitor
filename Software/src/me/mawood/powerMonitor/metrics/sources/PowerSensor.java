package me.mawood.powerMonitor.metrics.sources;

import me.mawood.powerMonitor.metrics.Metric;
import me.mawood.powerMonitor.metrics.sources.configs.VoltageSenseConfig;
import me.mawood.powerMonitor.metrics.units.Power;
import me.mawood.powerMonitor.packets.Packet;
import me.mawood.powerMonitor.packets.PacketCollector;
import me.mawood.powerMonitor.packets.PacketEventListener;

import java.util.ArrayList;
import java.util.Collection;

public class PowerSensor implements PacketEventListener
{
    private final Collection<Metric<Power>> realPowerMetrics;

    private final VoltageSensor voltageSensor;
    private final CurrentClamp currentClamp;


    public PowerSensor(VoltageSensor voltageSensor, CurrentClamp currentClamp, PacketCollector packetCollector)
    {
        this(voltageSensor,currentClamp);
        packetCollector.addPacketEventListener(this);
    }

    public PowerSensor(VoltageSensor voltageSensor, CurrentClamp currentClamp)
    {
        realPowerMetrics = new ArrayList<>();
        this.voltageSensor = voltageSensor;
        this.currentClamp = currentClamp;
    }

    public Collection<Metric<Power>> getRealPowerMetrics()
    {
        return realPowerMetrics;
    }

    public Metric<Power> getRunningAverageRealPowerMetric()
    {
        return new Metric<>(realPowerMetrics.stream().mapToDouble(Metric::getValue).sum()/ realPowerMetrics.size(), Power.WATTS);
    }

    public Metric<Power> getRunningAverageRealPowerMetricAndClear()
    {
        Metric<Power> metric = getRunningAverageRealPowerMetric();
        realPowerMetrics.clear();
        return metric;
    }

    public void clearMetrics()
    {
        realPowerMetrics.clear();
    }

    @Override
    public void handleNewPackets(Collection<Packet> newPackets)
    {
        for(Packet packet:newPackets) processPacket(packet);
    }

    private void processPacket(Packet packet)
    {
        if(packet.hasChannel(currentClamp.getChannelNumber()))
        {
            realPowerMetrics.add(new Metric<>(
                    (getScaleFactor()*packet.getRealPower(currentClamp.getChannelNumber()))+getOffset(), Power.WATTS));
        }
    }
    private double getOffset()
    {
        return 0;
    }

    public double getScaleFactor()
    {
        return (currentClamp.getConfig().getTurnsFactor() / currentClamp.getConfig().getSamplingResistor())*
                ((voltageSensor.getConfig().getMainsRms()* VoltageSenseConfig.getMagicMainsConstant())/
                        (voltageSensor.getConfig().getTransformerRms()*VoltageSenseConfig.getMagicTransformerConstant()));
    }

    @Override
    public String toString()
    {
        return String.format("Power Sensor: {Real Power: {%s}}",
                getRunningAverageRealPowerMetric());
    }
}
