package me.mawood.powerMonitor;

import java.nio.ByteBuffer;

class MetricsBuffer
{
    private ByteBuffer bBuff;
    private byte asciiChar = 'V';
    private double rmsVoltage;
    private short[] channelNumber = new short[9];
    private double[] realPowerValues = new double[9];
    private double[] apparentPowerValues = new double[9];

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
        bBuff = ByteBuffer.wrap(msg);
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
}
