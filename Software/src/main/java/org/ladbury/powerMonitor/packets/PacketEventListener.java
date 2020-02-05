package org.ladbury.powerMonitor.packets;

import java.util.Collection;

public interface PacketEventListener
{
    void handleNewPackets(Collection<Packet> newPackets);
}
