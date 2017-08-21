package me.mawood.powerMonitor;

import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException, I2CFactory.UnsupportedBusNumberException
    {
        STM8PowerMonitor powerMonitor = new STM8PowerMonitor();
        powerMonitor.setOutputDataFrequency(1);
        PowerDataSubscriber powerDataSubscriber = new PowerDataSubscriber();
        PowerDataProcessor powerDataProcessor = new PowerDataProcessor();
        powerDataProcessor.setPowerMonitor(powerMonitor);
        powerMonitor.OpenSerialPort();
        powerDataSubscriber.run();
        powerDataProcessor.run();
        powerMonitor.closeSerialPort();
        powerDataSubscriber.stop();
        System.exit(0);
     }
}
