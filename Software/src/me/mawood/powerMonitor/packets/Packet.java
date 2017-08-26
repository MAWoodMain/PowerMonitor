package me.mawood.powerMonitor.packets;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;

public class Packet
{
    class Measurement
    {
        private final double iRms;
        private final double realPower;
        public Measurement(double iRms, double realPower)
        {
            this.iRms = iRms;
            this.realPower = realPower;
        }

        public double getIRms()
        {
            return iRms;
        }

        public double getRealPower()
        {
            return realPower;
        }

        @Override
        public String toString()
        {
            return "Power{" +
                    "iRms=" + iRms +
                    ", realPower=" + realPower +
                    '}';
        }

        public String toCSV()
        {
            return iRms + "," + realPower;
        }
    }

    private final double vRms;
    private final HashMap<Byte,Measurement> channels;

    public Packet(byte[] packet) throws UnsupportedEncodingException
    {
        channels = new HashMap<>();

        // expected structure
        // {VRMS(double) 9*(ChannelNumber(byte) ChannelApparentPower(double) ChannelRealPower(double))}

        ByteBuffer buffer = ByteBuffer.wrap(packet);
        byte channelNo;
        double iRms, real;
        vRms = getDouble(buffer);
        for(int i = 0; i < (packet.length - 10)/20; i++)
        {
            channelNo = buffer.get();
            iRms = getDouble(buffer);
            real = getDouble(buffer);
            channels.put(channelNo,new Measurement(iRms,real));
        }
    }

    public boolean hasChannel(byte channelNumber)
    {
        return channels.containsKey(channelNumber);
    }

    public double getVRms()
    {
        return vRms;
    }

    public double getRealPower(byte channelNumber)
    {
        if (!channels.containsKey(channelNumber))
            throw new IllegalArgumentException("Channel '" + channelNumber + "' not present");

        return channels.get(channelNumber).getRealPower();
    }

    public double getIRms(byte channelNumber)
    {
        if (!channels.containsKey(channelNumber))
            throw new IllegalArgumentException("Channel '" + channelNumber + "' not present");

        return channels.get(channelNumber).getIRms();
    }

    private double getDouble(ByteBuffer byteBuffer)
    {
        byte[] buffer = new byte[10];
        byteBuffer.get(buffer,0,10);
        //buffer = Arrays.copyOfRange(buffer,1,buffer.length-1);
        for(int i = 0; i < buffer.length; i++)
        {
            if(buffer[i] == 0)
            {
                buffer = Arrays.copyOf(buffer,i);
                return Double.parseDouble(new String(buffer, Charset.forName("US-ASCII")));
            }
        }
        return Double.parseDouble(new String(buffer, Charset.forName("US-ASCII")));
    }

    @Override
    public String toString()
    {
        return "Packet{" +
                "vRms=" + vRms +
                ", channels=" + channels +
                '}';
    }

    public String toCSV()
    {
        StringBuilder output = new StringBuilder(vRms + ",");
        for(byte key:channels.keySet()) output.append(channels.get(key).toCSV()).append(",");
        return output.toString().substring(0,output.length()-1);
    }
}
