package me.mawood.powerMonitor;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;

public class PowerMonitorPacket
{
    class Power
    {
        private final double apparentPower;
        private final double realPower;
        public Power(double apparentPower, double realPower)
        {
            this.apparentPower = apparentPower;
            this.realPower = realPower;
        }

        public double getApparentPower()
        {
            return apparentPower;
        }

        public double getRealPower()
        {
            return realPower;
        }

        @Override
        public String toString()
        {
            return "Power{" +
                    "apparentPower=" + apparentPower +
                    ", realPower=" + realPower +
                    '}';
        }

        public String toCSV()
        {
            return apparentPower + "," + realPower;
        }
    }

    private final double vRMS;
    private final HashMap<Byte,Power> channels;

    public PowerMonitorPacket(byte[] packet) throws UnsupportedEncodingException
    {
        channels = new HashMap<>();

        // expected structure
        // {VRMS(double) 9*(ChannelNumber(byte) ChannelApparentPower(double) ChannelRealPower(double))}

        ByteBuffer buffer = ByteBuffer.wrap(packet);
        byte channelNo;
        double apparent, real;
        vRMS = getDouble(buffer);
        for(int i = 0; i < (packet.length - 10)/20; i++)
        {
            channelNo = buffer.get();
            apparent = getDouble(buffer);
            real = getDouble(buffer);
            channels.put(channelNo,new Power(apparent,real));
        }
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
        return "PowerMonitorPacket{" +
                "vRMS=" + vRMS +
                ", channels=" + channels +
                '}';
    }

    public String toCSV()
    {
        StringBuilder output = new StringBuilder(vRMS + ",");
        for(byte key:channels.keySet()) output.append(channels.get(key).toCSV()).append(",");
        return output.toString().substring(0,output.length()-1);
    }
}
