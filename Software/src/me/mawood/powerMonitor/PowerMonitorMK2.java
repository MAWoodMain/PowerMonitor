package me.mawood.powerMonitor;

import com.pi4j.io.serial.*;
import me.mawood.powerMonitor.metrics.sources.CurrentClamp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class PowerMonitorMK2 extends Thread implements SerialDataEventListener, PowerMonitor
{
    private static final byte[] START_SEQUENCE;
    private static final int PACKET_LENGTH;
    private final Collection<Byte> incomingBytes;
    private final Collection<Byte> bytes;
    private final Collection<PowerMonitorPacket> packets;

    private final Collection<PacketEventListener> listeners;

    private long extractionPeriod;

    private final Serial serial;


    static
    {
        final char[] startString = "PM_START".toCharArray();
        START_SEQUENCE = new byte[startString.length];
        for (int i = 0; i < startString.length; i++)
            START_SEQUENCE[i] = (byte)startString[i];
        PACKET_LENGTH = START_SEQUENCE.length+10+1+9*2*10;
    }


    public PowerMonitorMK2() throws IOException, InterruptedException
    {
        this.extractionPeriod = 3000;
        this.bytes = new ArrayList<>();
        this.incomingBytes = new ArrayList<>();
        this.packets = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.serial = SerialFactory.createInstance();

        SerialConfig config = new SerialConfig();
        config.device(SerialPort.getDefaultPort())
                .baud(Baud._230400)
                .dataBits(DataBits._8)
                .stopBits(StopBits._1)
                .parity(Parity.NONE)
                .flowControl(FlowControl.NONE);
        serial.addListener(this);
        serial.open(config);

        this.start();
    }
    @Override
    public void run()
    {
        long time;
        Collection<PowerMonitorPacket> newPackets;
        super.run();
        while(!Thread.interrupted())
        {
            time = System.currentTimeMillis();
            if(incomingBytes.size() > 0)
            {
                synchronized (incomingBytes)
                {
                    bytes.addAll(incomingBytes);
                    incomingBytes.clear();
                }
                newPackets = new ArrayList<>();
                for(byte[] packet:extractPackets(bytes))
                {
                    try
                    {
                        newPackets.add(new PowerMonitorPacket(packet));
                    } catch (IllegalArgumentException | UnsupportedEncodingException ignored) {
                        System.out.println(ignored);
                    }
                }
                packets.addAll(newPackets);
                alertPacketListeners(newPackets);

            }
            while(time + extractionPeriod > System.currentTimeMillis())
            {
                // wait half the remaining time
                try
                {
                    Thread.sleep(Math.max(0,((time + extractionPeriod)-System.currentTimeMillis())/2));
                } catch (InterruptedException ignored) {}
            }
        }
    }
    private void alertPacketListeners(Collection<PowerMonitorPacket> newPackets)
    {
        for(PacketEventListener listener:listeners) listener.handleNewPackets(newPackets);
    }

    private Collection<byte[]> extractPackets(Collection<Byte> rawData)
    {
        Iterator<Byte> it = rawData.iterator();
        byte item;
        int tracker = 0;
        boolean found = false;
        byte[] packet = new byte[PACKET_LENGTH];
        Collection<byte[]> packets = new ArrayList<>();
        while(it.hasNext())
        {
            item = it.next();
            if(found)
            {
                if (tracker >= PACKET_LENGTH)
                {
                    // reached end of packet
                    tracker = 0;
                    found = false;
                    packets.add(packet.clone());
                } else
                {
                    packet[tracker] = item;
                    // remove consumed characters
                    it.remove();
                    tracker++;
                }
            } else
            {
                if(tracker >= START_SEQUENCE.length)
                {
                    // end of start sequence start of data
                    found = true;
                    tracker = 0;
                    packet[tracker] = item;
                    tracker++;
                    // remove consumed characters
                    it.remove();
                } else if(item == START_SEQUENCE[tracker])
                {
                    tracker++;
                    it.remove();
                }
            }

        }
        return packets;
    }

    public void addPacketEventListener(PacketEventListener listener)
    {
        this.listeners.add(listener);
    }

    @Override
    public void dataReceived(SerialDataEvent serialDataEvent)
    {
        try
        {
            for(byte b:serialDataEvent.getBytes()) incomingBytes.add(b);
        } catch (IOException ignored) {}
    }

    public void close() throws IOException
    {
        this.interrupt();
        serial.close();
    }

    public static void main(String[] args) throws IOException, InterruptedException
    {
        PowerMonitorMK2 powerMonitor = new PowerMonitorMK2();
        CurrentClamp clamp0 = new CurrentClamp((byte)0,CurrentClampConfig.SCT013_5A1V,powerMonitor);
        CurrentClamp clamp1 = new CurrentClamp((byte)1,CurrentClampConfig.SCT013_5A1V,powerMonitor);
        powerMonitor.addPacketEventListener(e -> {
            System.out.println(clamp0);
            System.out.println(clamp1);
        });
        Thread.sleep(60000);
        powerMonitor.close();
        System.exit(1);
    }
}
