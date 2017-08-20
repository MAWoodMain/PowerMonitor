package me.mawood.powerMonitor;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

/**
 * PowerMonitor
 * Created by Matthew Wood on 19/08/2017.
 */
public class STM8PowerMonitor
{
    private static final byte DEFAULT_I2C_ADDRESS = 0x30;
    private final I2CDevice device;

    public STM8PowerMonitor() throws IOException, I2CFactory.UnsupportedBusNumberException
    {
        this(DEFAULT_I2C_ADDRESS);
    }

    public STM8PowerMonitor(byte address) throws IOException, I2CFactory.UnsupportedBusNumberException
    {
        device = I2CFactory.getInstance(I2CBus.BUS_1).getDevice(address);
        configureRTC();
    }

    public void configureRTC()
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

    public static void main(String[] args) throws IOException, I2CFactory.UnsupportedBusNumberException
    {
        STM8PowerMonitor sPM = new STM8PowerMonitor();
        PowerDataProcessor pDP = new PowerDataProcessor();
        try
        {
            SerialStreamer Ss = new SerialStreamer(pDP);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        pDP.run();
        System.exit(0);
    }
}
