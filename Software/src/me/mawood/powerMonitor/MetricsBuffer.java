package me.mawood.powerMonitor;

import java.nio.ByteBuffer;
import java.util.Arrays;

class MetricsBuffer
{
    private static final byte asciiChar = 'V';
    private double rmsVoltage;
    private final short[] channelNumber = new short[9];
    private final double[] realPowerValues = new double[9];
    private final double[] apparentPowerValues = new double[9];

    /**
     * MetricsBuffer    Translates a serial message in bytes to the metrics, the message is expected to contain
     *                  1 byte Ascii 'V'
     *                  8 bytes RMS voltage double
     *                  1 byte channel number 0-8
     *                  8 bytes double apparent power   }repeated 9 times
     *                  8 bytes double real power       }
     *                  total length 156 bytes
     * @param msg       The bytes received from the power monitor
     * @throws IllegalArgumentException if the message is not as expected
     */
    MetricsBuffer(byte[] msg)throws IllegalArgumentException
    {
        ByteBuffer bBuff = ByteBuffer.wrap(msg);
        if (bBuff.limit()<162) {throw new IllegalArgumentException("Message too short");}
        if (bBuff.get() != asciiChar) {throw new IllegalArgumentException("Illegal start character");}
        rmsVoltage = bBuff.getDouble();
        for (int i=0; i<=8; i++)
        {
         channelNumber[i] = bBuff.getShort();
         if (channelNumber[i] != i) {throw new IllegalArgumentException("Illegal channel number");}
         apparentPowerValues[i] = bBuff.getDouble();
         realPowerValues[i] = bBuff.getDouble();
        }
    }

    double getRmsVoltage(){return this.rmsVoltage;}
    double getRealPower(int channel){return this.realPowerValues[channel];}
    double getApparentPower(int channel){return this.apparentPowerValues[channel];}

    void printMetricsBuffer()
    {
        System.out.println("Voltage Count: "+ rmsVoltage);
        for (int i=0; i<=8; i++)
        {
            System.out.println("ChNbr: "+ channelNumber[i] + "AppP: " + apparentPowerValues[i] + "RealP: " + realPowerValues[i]);
        }
    }

    @Override
    public String toString()
    {
        return "MetricsBuffer{" +
                "rmsVoltage=" + rmsVoltage +
                ", channelNumber=" + Arrays.toString(channelNumber) +
                ", realPowerValues=" + Arrays.toString(realPowerValues) +
                ", apparentPowerValues=" + Arrays.toString(apparentPowerValues) +
                '}';
    }
}
