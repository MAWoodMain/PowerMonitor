package me.mawood.powerMonitor.old;

import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.serial.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

import static java.lang.Thread.sleep;

/**
 * PacketCollector
 * Created by Matthew Wood on 19/08/2017.
 */
class STM8PowerMonitorOld implements SerialDataEventListener, Runnable, PowerMonitor
{
    private static final byte DEFAULT_I2C_ADDRESS = 0x30;

    //private final I2CDevice device;
    private Serial serial;
    private SerialConfig config;
    private MetricsBuffer rawMetricsBuffer,newRawMetricsBuffer;

    // run control variables
    private volatile boolean msgArrived;
    private volatile SerialDataEvent serialDataEvent;
    private volatile boolean stop;
    private volatile int samplesInBuffer;


    STM8PowerMonitorOld() throws IOException, I2CFactory.UnsupportedBusNumberException
    {
        this(DEFAULT_I2C_ADDRESS);
    }

    @SuppressWarnings("WeakerAccess")
    STM8PowerMonitorOld(byte address) throws IOException, I2CFactory.UnsupportedBusNumberException
    {
        //device = I2CFactory.getInstance(I2CBus.BUS_1).getDevice(address);
        //configureRTC();
        InitialiseSerialPort();
        samplesInBuffer = 0;
        rawMetricsBuffer = new MetricsBuffer();
    }

    @Override
    public MetricsBuffer getRawMetricsBuffer() {return rawMetricsBuffer;}

    public MetricsBuffer getAndResetRawMetricsBuffer()
    {
        samplesInBuffer = 0; // the next sample will overwrite the buffer
        return rawMetricsBuffer;
    }

    private void configureRTC()
    {
        byte[] timeRegisters = constructTimeRegisters();
        for(byte b:timeRegisters)
            System.out.println(String.format("%8s",Integer.toBinaryString(b)).replace(' ', '0') + " = " + String.format("%02x",b));
    }

    private static byte[] constructTimeRegisters()
    {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());

        byte seconds = (byte)ldt.getSecond();
        byte secondsByte = DecToBCDArray(seconds)[0];
        buffer.put(secondsByte);

        byte min = (byte)ldt.getMinute();
        byte minutesByte = DecToBCDArray(min)[0];
        buffer.put(minutesByte);

        byte hour = (byte)ldt.getHour();
        byte hourByte = DecToBCDArray(hour)[0];
        buffer.put(hourByte);

        byte day = (byte)ldt.getDayOfMonth();
        byte dayByte = DecToBCDArray(day)[0];
        buffer.put(dayByte);

        byte month = (byte)ldt.getMonth().getValue();
        byte monthByte = DecToBCDArray(month)[0];
        buffer.put(monthByte);

        byte year = (byte)(ldt.getYear() - 2000);
        byte yearByte = DecToBCDArray(year)[0];
        buffer.put(yearByte);


        return buffer.array();
    }

    private static byte[] DecToBCDArray(long num) {
        int digits = 0;

        long temp = num;
        while (temp != 0) {
            digits++;
            temp /= 10;
        }

        int byteLen = digits % 2 == 0 ? digits / 2 : (digits + 1) / 2;
        boolean isOdd = digits % 2 != 0;

        byte bcd[] = new byte[byteLen];

        for (int i = 0; i < digits; i++) {
            byte tmp = (byte) (num % 10);

            if (i == digits - 1 && isOdd)
                bcd[i / 2] = tmp;
            else if (i % 2 == 0)
                bcd[i / 2] = tmp;
            else {
                byte foo = (byte) (tmp << 4);
                bcd[i / 2] |= foo;
            }

            num /= 10;
        }

        for (int i = 0; i < byteLen / 2; i++) {
            byte tmp = bcd[i];
            bcd[i] = bcd[byteLen - i - 1];
            bcd[byteLen - i - 1] = tmp;
        }

        return bcd;
    }
    private void InitialiseSerialPort()
    {
        config = new SerialConfig();
        try
        {
            config.device(SerialPort.getDefaultPort())
                    .baud(Baud._230400)
                    .dataBits(DataBits._8)
                    .stopBits(StopBits._1)
                    .parity(Parity.NONE)
                    .flowControl(FlowControl.NONE);
        } catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }

        serial = SerialFactory.createInstance();
        addSerialListener(this);
    }
    void addSerialListener( SerialDataEventListener listener)
    {
        serial.addListener(listener);
    }
    void OpenSerialPort()
    {
        try
        {
            serial.open(config);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    void closeSerialPort()
    {
        try
        {
            serial.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    //
    // SerialDataEventListener implementation
    //

    /**
     * dataReceived             Handler for serial data arriving
     * @param serialDataEvent   The data that has arrived
     */
    @Override
    public void dataReceived(SerialDataEvent serialDataEvent)
    {
        this.serialDataEvent = serialDataEvent;
        msgArrived = true;

    }
    //
    // Runnable implementation
    //

    /**
     * run  The main processing loop
     */
    @Override
    public void run()
    {
        byte[] serialBytes;
        try
        {
            while (!Thread.interrupted() && !stop)
            {
                if (msgArrived)
                {
                    msgArrived = false;
                    //TODO  Check for sequence gaps, fill if necessary
                    try
                    {
                        serialBytes = serialDataEvent.getBytes();
                        System.out.println("Received: " + Arrays.toString(serialBytes));
                        newRawMetricsBuffer = new MetricsBuffer(serialBytes);
                        newRawMetricsBuffer.printMetricsBuffer();
                        if (samplesInBuffer == 0)
                        {
                            rawMetricsBuffer = newRawMetricsBuffer.clone();
                            samplesInBuffer = 1;
                        }
                        else
                        {
                            samplesInBuffer++;
                            rawMetricsBuffer.updateAverages(    samplesInBuffer,
                                                                newRawMetricsBuffer.getRmsVoltage(),
                                                                newRawMetricsBuffer.getRealPowers(),
                                                                newRawMetricsBuffer.getApparentPowers());
                        }

                    } catch (IOException e1)
                    {
                        e1.printStackTrace();
                    }
                }
                sleep(100);
            }
        }catch (InterruptedException e)
        {
            System.out.println("STMPowerMonitor Interrupted, exiting");
        }
    }

    /**
     * stop     Method to stop the main processing loop and close down processing
     */
    public void stop()
    {
        stop = true;
    }
}
