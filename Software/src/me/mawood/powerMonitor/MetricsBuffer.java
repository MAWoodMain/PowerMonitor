package me.mawood.powerMonitor;

import java.nio.ByteBuffer;

public class MetricsBuffer
{
    private ByteBuffer bBuff;
    private byte asciiChar;
    private short voltageCount;
    private short[] channelNumber = new short[9];
    private double[] realPowerValues = new double[9];
    private double[] apparentPowerValues = new double[9];

    MetricsBuffer(byte[] msg)throws IllegalArgumentException
    {
        bBuff = ByteBuffer.wrap(msg);
        if (bBuff.limit()<156) {throw new IllegalArgumentException("Message too short");}
        if (bBuff.get() != 0x56) {throw new IllegalArgumentException("Illegal start character");}
        voltageCount = bBuff.getShort();
        for (int i=0; i<=8; i++)
        {
         channelNumber[i] = bBuff.getShort();
         if (channelNumber[i] != i) {throw new IllegalArgumentException("Illegal channel number");}
         apparentPowerValues[i] = bBuff.getDouble();
         realPowerValues[i] = bBuff.getDouble();
        }
    }
    short getVoltageCount(){return this.voltageCount;}
    double getRealPower(int channel){return this.realPowerValues[channel];}
    double getApparentPower(int channel){return this.apparentPowerValues[channel];}
}
