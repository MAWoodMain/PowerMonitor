package me.mawood.powerMonitor;

import me.mawood.powerMonitor.metrics.InvalidDataException;
import me.mawood.powerMonitor.metrics.PowerMetricCalculator;
import me.mawood.powerMonitor.metrics.monitors.CurrentMonitor;
import me.mawood.powerMonitor.metrics.monitors.RealPowerMonitor;
import me.mawood.powerMonitor.metrics.monitors.VoltageMonitor;
import me.mawood.powerMonitor.metrics.monitors.configs.CurrentClampConfig;
import me.mawood.powerMonitor.metrics.monitors.configs.VoltageSenseConfig;
import me.mawood.powerMonitor.metrics.units.Power;
import me.mawood.powerMonitor.metrics.units.Unit;
import me.mawood.powerMonitor.metrics.units.UnitType;
import me.mawood.powerMonitor.packets.STM8PacketCollector;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.time.Instant;

public class Main
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        STM8PacketCollector packetCollector = new STM8PacketCollector(1000);
        VoltageMonitor vm = new VoltageMonitor(100000, VoltageSenseConfig.UK9V, packetCollector);
        CurrentMonitor cm = new CurrentMonitor(100000, CurrentClampConfig.SCT013_20A1V, (byte)1, packetCollector);
        RealPowerMonitor pm = new RealPowerMonitor(100000, VoltageSenseConfig.UK9V, CurrentClampConfig.SCT013_20A1V, (byte)1, packetCollector);
        PowerMetricCalculator pmc = new PowerMetricCalculator(vm,cm,pm);

        packetCollector.addPacketEventListener(event -> {
            try
            {
                System.out.println(pmc.getAverageBetween(Power.WATTS, Instant.now().minusMillis(1000), Instant.now()));
            } catch (OperationNotSupportedException | InvalidDataException e)
            {
                e.printStackTrace();
            }
        });
        /*packetCollector.addPacketEventListener(e -> {
            for(Packet packet:e) System.out.println(packet.toCSV());
        });*/
        Thread.sleep(60*1000);
        packetCollector.close();
        System.exit(1);
    }
}
