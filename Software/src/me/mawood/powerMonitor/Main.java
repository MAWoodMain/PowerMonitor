package me.mawood.powerMonitor;

import com.pi4j.io.i2c.I2CFactory;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException, I2CFactory.UnsupportedBusNumberException, InterruptedException, MqttException
    {
        STM8PowerMonitor powerMonitor = new STM8PowerMonitor();
        PowerDataSubscriber powerDataSubscriber = new PowerDataSubscriber();
        PowerDataProcessor powerDataProcessor = new PowerDataProcessor(powerMonitor);

        powerMonitor.OpenSerialPort();

        powerDataSubscriber.run();
        powerDataProcessor.run();

        // TODO add some form or wait or triggered exit
        Thread.sleep(100000); // temporary


        powerMonitor.closeSerialPort();
        powerDataSubscriber.stop();
        powerDataProcessor.stop();

        System.exit(0);
     }
}
