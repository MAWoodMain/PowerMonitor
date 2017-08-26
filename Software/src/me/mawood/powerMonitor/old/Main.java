package me.mawood.powerMonitor.old;

import com.pi4j.io.i2c.I2CFactory;
import me.mawood.powerMonitor.old.STM8PowerMonitorOld;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException, I2CFactory.UnsupportedBusNumberException, InterruptedException, MqttException
    {
        STM8PowerMonitorOld powerMonitor = new STM8PowerMonitorOld();



        //PowerDataSubscriber powerDataSubscriber = new PowerDataSubscriber();
        //PowerDataProcessor powerDataProcessor = new PowerDataProcessor(powerMonitor);

        powerMonitor.addSerialListener(e-> {
            System.out.println("Event:");
            System.out.println(powerMonitor.getAndResetRawMetricsBuffer().toString());
        });

        powerMonitor.OpenSerialPort();

        //powerDataSubscriber.run();
       // powerDataProcessor.run();

        // TODO add some form or wait or triggered exit
        Thread.sleep(100000); // temporary


        powerMonitor.closeSerialPort();
        //powerDataSubscriber.stop();
        //powerDataProcessor.stop();

        System.exit(0);
     }
}
