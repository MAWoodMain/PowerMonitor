package me.mawood.powerMonitor.packets;

import com.pi4j.io.serial.*;
import me.mawood.powerMonitor.metrics.sources.PowerSensor;
import me.mawood.powerMonitor.metrics.sources.VoltageSensor;
import me.mawood.powerMonitor.metrics.sources.configs.CurrentClampConfig;
import me.mawood.powerMonitor.metrics.sources.CurrentClamp;
import me.mawood.powerMonitor.metrics.sources.configs.VoltageSenseConfig;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class STM8PacketCollector extends Thread implements SerialDataEventListener, PacketCollector
{
    private static final byte[] START_SEQUENCE;
    private static final int PACKET_LENGTH;
    private final Collection<Byte> incomingBytes;
    private final Collection<Byte> bytes;

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


    public STM8PacketCollector() throws IOException, InterruptedException
    {
        this(3000);
    }

    public STM8PacketCollector(long extractionPeriod) throws IOException, InterruptedException
    {
        this.extractionPeriod = extractionPeriod;
        this.bytes = new ArrayList<>();
        this.incomingBytes = new ArrayList<>();
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
        Collection<Packet> newPackets;
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
                        newPackets.add(new Packet(packet));
                    } catch (IllegalArgumentException ignored) {}
                }
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
    private void alertPacketListeners(Collection<Packet> newPackets)
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
        STM8PacketCollector packetCollector = new STM8PacketCollector(1000);
        VoltageSensor vs = new VoltageSensor(VoltageSenseConfig.UK9V,packetCollector);
        HashMap<Integer, CurrentClamp> clamps = new HashMap<>();
        for (int i = 0; i < 9; i++)
            clamps.put(i,new CurrentClamp((byte)i, CurrentClampConfig.SCT013_20A1V,packetCollector));
        PowerSensor ps = new PowerSensor(vs,clamps.get(8),packetCollector);
        packetCollector.addPacketEventListener(e -> {
            System.out.println(vs);
            System.out.println(clamps.get(8));
            System.out.println(ps);
            // Clear out consumed metrics
            vs.clearMetrics();
            for(int key:clamps.keySet()) clamps.get(key).clearMetrics();
        });
        Thread.sleep(60000);
        packetCollector.close();
        System.exit(1);
    }
}
