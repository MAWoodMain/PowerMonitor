package me.mawood.powerMonitor;

import java.util.Collection;

public interface PacketEventListener
{
    void handleNewPackets(Collection<PowerMonitorPacket> newPackets);
}
