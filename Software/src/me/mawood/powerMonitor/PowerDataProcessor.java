package me.mawood.powerMonitor;

import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.util.Arrays;

import static java.lang.Thread.sleep;


public class PowerDataProcessor  implements SerialDataEventListener, Runnable
{
    private enum MetricType{REAL, APPARENT};
    private final String basetopic    = "emon";
    private final String clientId     = "PMon10";
    private final String topic        = basetopic+"/"+clientId;

    private String content      = "test message";
    private int qos             = 2;
    private String broker       = "tcp://localhost:1883";
    private MemoryPersistence persistence = new MemoryPersistence();
    private MqttClient powerMonitorADCMQQTClient;
    private volatile boolean msgArrived;
    private volatile SerialDataEvent serialDataEvent;

    PowerDataProcessor()
    {
        // set up clamp configuration
        // bind listener to serial port
        // set up stream definitions
        // make connection to MQTT broker
        try {
            powerMonitorADCMQQTClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting PowerDataProcessor to broker: "+broker);
            powerMonitorADCMQQTClient.connect(connOpts);
            System.out.println("PowerDataProcessor Connected");

        } catch(MqttException me) {
            handleMQTTException(me);
        }

    }

    @Override
    public void dataReceived(SerialDataEvent serialDataEvent)
    {
        this.serialDataEvent = serialDataEvent;
        msgArrived = true;

    }

    private void handleMQTTException(MqttException me)
    {
        System.out.println("reason "+me.getReasonCode());
        System.out.println("msg "+me.getMessage());
        System.out.println("loc "+me.getLocalizedMessage());
        System.out.println("cause "+me.getCause());
        System.out.println("excep "+me);
        me.printStackTrace();
    }

    private void shutdownDataProcessing()
    {
        try
        {
            powerMonitorADCMQQTClient.disconnect();
            System.out.println("Disconnected");
        } catch (MqttException me)
        {
            handleMQTTException(me);
        }
        System.out.println("PowerDataProcessor Disconnected");
        System.exit(0);
    }

    private String makeMetricLable(MetricType mt, int clampNbr)
    {
        return topic+"/"+mt.toString()+" "+clampNbr ;
    }

    private int getMetric(byte[] bytes, MetricType mt, int clamp)
    {
        return 0;
    }

    @Override
    public void run()
    {
        int nbrSerialBytes = 0;
        byte[] serialBytes = null;
        try
        {
            while (true)
            {
                if (msgArrived)
                {
                    msgArrived = false;
                    try
                    {
                        serialBytes = serialDataEvent.getBytes();
                        nbrSerialBytes = serialDataEvent.length();
                        System.out.println("Received: " + Arrays.toString(serialBytes));
                    } catch (IOException e1)
                    {
                        e1.printStackTrace();
                    }
                    for (MetricType mt : MetricType.values() )
                    {
                        for (int clamp = 0; clamp<10; clamp++)
                        {
                            content = makeMetricLable(mt,clamp)+getMetric(serialBytes,mt, clamp);
                        }
                    }
                    // unwrap the data
                    // Check for sequence gaps, fill if necessary
                    // extract data and scale based on clamps
                    // publish to broker
                    try
                    {
                        MqttMessage message = new MqttMessage(content.getBytes());
                        message.setQos(qos);
                        powerMonitorADCMQQTClient.publish(topic, message);
                        System.out.println("Message published");
                    } catch (MqttException me)
                    {
                        handleMQTTException(me);
                    }

                }
                sleep(100);
            }
        }catch (InterruptedException e)
        {
            shutdownDataProcessing();
            System.out.println("Data Processing Intterupted, exiting");
        }
    }
}
