package me.mawood.powerMonitor.packets;

import com.pi4j.io.serial.*;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class STM8PacketCollector extends Thread implements SerialDataEventListener, PacketCollector
{
    private static final byte[] START_SEQUENCE;
    private static final int PACKET_LENGTH;
    private final List<Byte> incomingBytes;
    private final List<Byte> bytes;

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
        this(1000);
    }

    public STM8PacketCollector(long extractionPeriod) throws IOException, InterruptedException
    {
        this.extractionPeriod = extractionPeriod;
        this.bytes = new ArrayList<>();
        this.incomingBytes = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.serial = SerialFactory.createInstance();

        SerialConfig config = new SerialConfig();
        config.device("/dev/ttyAMA0") //config.device(SerialPort.getDefaultPort()) was "/dev/ttyS0"
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
        List<Packet> newPackets;
        super.run();
        while(!Thread.interrupted())
        {
            //process list of serial bytes accumulated for period
            time = System.currentTimeMillis();
            if(incomingBytes.size() > 0)
            {
                synchronized (incomingBytes)
                {
                    //System.err.println(incomingBytes.toString()); //debug for log
                    bytes.addAll(incomingBytes);
                    incomingBytes.clear();
                }
                newPackets = new ArrayList<>();
                Instant sampleStart = Instant.now().minusMillis(extractionPeriod);
                int i = 0;
                Collection<byte[]> rawPackets = extractPackets(bytes);
                for(byte[] packet:rawPackets)
                {
                    sampleStart = sampleStart.plusMillis(extractionPeriod/rawPackets.size());
                    try
                    {
                        newPackets.add(new Packet(packet, sampleStart)); // full serial packet without start sequence
                    } catch (IllegalArgumentException ignored) {}
                    i++;
                }
                alertPacketListeners(newPackets);

            }
            //accumulate packets for the period (assume top half of while is short time)
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
}
