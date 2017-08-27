package me.mawood.powerMonitor;

import me.mawood.powerMonitor.metrics.monitors.CurrentMonitor;
import me.mawood.powerMonitor.metrics.monitors.RealPowerMonitor;
import me.mawood.powerMonitor.metrics.monitors.VoltageMonitor;
import me.mawood.powerMonitor.metrics.monitors.configs.CurrentClampConfig;
import me.mawood.powerMonitor.metrics.monitors.configs.VoltageSenseConfig;
import me.mawood.powerMonitor.packets.STM8PacketCollector;

import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        STM8PacketCollector packetCollector = new STM8PacketCollector(1000);
        VoltageMonitor vm = new VoltageMonitor(100000, VoltageSenseConfig.UK9V, packetCollector);
        CurrentMonitor cm = new CurrentMonitor(100000, CurrentClampConfig.SCT013_20A1V, (byte)1, packetCollector);
        RealPowerMonitor pm = new RealPowerMonitor(100000, VoltageSenseConfig.UK9V, CurrentClampConfig.SCT013_20A1V, (byte)1, packetCollector);
        packetCollector.addPacketEventListener(e -> {
            System.out.println(vm);
            System.out.println(cm);
            System.out.println(pm);
            System.out.println();
        });
        /*packetCollector.addPacketEventListener(e -> {
            for(Packet packet:e) System.out.println(packet.toCSV());
        });*/
        Thread.sleep(60*1000);
        packetCollector.close();
        System.exit(1);
    }
}
