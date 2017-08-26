package me.mawood.powerMonitor.old;

import java.nio.ByteBuffer;
import java.util.Arrays;

class MetricsBuffer
{
    private static final String START_SEQUENCE = "PM_START";
    private final int noChannels = 9;
    private double rmsVoltage;
    private final short[] channelNumber = new short[noChannels];
    private double[] realPowerValues = new double[noChannels];
    private double[] apparentPowerValues = new double[noChannels];

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
        if (bBuff.limit()<(161+START_SEQUENCE.length())) {throw new IllegalArgumentException("Message too short");}

        // get start sequence from buffer
        byte[] identifier = new byte[START_SEQUENCE.length()];
        bBuff.get(identifier,0,START_SEQUENCE.length());
        // check if it recognised
        if(!Arrays.equals(START_SEQUENCE.toCharArray(), ByteBuffer.wrap(identifier).asCharBuffer().array()))
            throw new IllegalArgumentException("Message identifier not recognised");

        rmsVoltage = bBuff.getDouble();
        for (int i = 0; i< noChannels; i++)
        {
            channelNumber[i] = bBuff.getShort();
            if (channelNumber[i] != i) {throw new IllegalArgumentException("Illegal channel number");}
            apparentPowerValues[i] = bBuff.getDouble();
            realPowerValues[i] = bBuff.getDouble();
        }
    }

    MetricsBuffer(double v, double[] realPower, double[] apparentPower)
    {
        this.rmsVoltage = v;
        for( short i = 0; i < noChannels; i++) {channelNumber[i] =i;}
        this.apparentPowerValues = apparentPower.clone();
        this.realPowerValues =  realPower.clone();
    }

    MetricsBuffer()
    {
        rmsVoltage = 0;
        for (short i = 0; i< noChannels; i++)
        {
            channelNumber[i] = i;
            apparentPowerValues[i] = 0;
            realPowerValues[i] = 0;
        }

    }
    @SuppressWarnings({"MethodDoesntCallSuperMethod", "CloneDoesntDeclareCloneNotSupportedException"})
    @Override
    protected MetricsBuffer clone()
    {
        MetricsBuffer cloneMB = new MetricsBuffer();
        cloneMB.rmsVoltage = this.rmsVoltage;
        cloneMB.apparentPowerValues = this.apparentPowerValues.clone();
        cloneMB.realPowerValues = this.realPowerValues.clone();
        return cloneMB;
    }
    void updateAverages(int newN, double v, double[] realPower, double[] apparentPower) throws IllegalArgumentException
    {
        if( newN<2) {throw new IllegalArgumentException("Illegal average divisor");}
        this.rmsVoltage = (this.rmsVoltage*(newN-1)+v)/newN;
        for (int i = 0; i< noChannels; i++)
        {
            this.apparentPowerValues[i] = ((this.apparentPowerValues[i] * (newN - 1)) + apparentPower[i]) / newN;
            this.realPowerValues[i] = ((this.realPowerValues[i] * (newN - 1)) + realPower[i]) / newN;
        }
    }

    double getRmsVoltage(){return this.rmsVoltage;}
    double getRealPower(int channel){return this.realPowerValues[channel];}
    double[] getRealPowers(){return this.realPowerValues;}
    double getApparentPower(int channel){return this.apparentPowerValues[channel];}
    double[] getApparentPowers(){return this.apparentPowerValues;}
    int getNoPowerChannels() {return this.noChannels;}

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
                ", noChannels=" + noChannels +
                ", channelNumber=" + Arrays.toString(channelNumber) +
                ", realPowerValues=" + Arrays.toString(realPowerValues) +
                ", apparentPowerValues=" + Arrays.toString(apparentPowerValues) +
                '}';
    }
}
