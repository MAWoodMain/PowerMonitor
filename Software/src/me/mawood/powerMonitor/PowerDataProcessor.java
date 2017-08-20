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
    private enum MetricType{Real, Apparent}
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
    private class ClampParameters
    {
        int clampNumber;
        float scaleFactor;
        String units;
        int maxCurrent;
        int burdenResistor;
        ClampParameters(int cn, float sf, String u, int mc, int br)
        {
            clampNumber = cn;
            scaleFactor = sf;
            units = u;
            burdenResistor = br;
            maxCurrent = mc;
        }
    }
    private ClampParameters[] cp = new ClampParameters[10];

    PowerDataProcessor()
    {
        // set up clamp configuration
        // bind listener to serial port
        // set up stream definitions
        // make connection to MQTT broker
        cp[0]= new ClampParameters(0,1.0f,"V",0,0);
        cp[1]= new ClampParameters(1,1.0f,"W",100, 10);
        cp[2]= new ClampParameters(2,1.0f,"W",20, 93);
        cp[3]= new ClampParameters(3,1.0f,"W",20, 93);
        cp[4]= new ClampParameters(4,1.0f,"W",20, 93);
        cp[5]= new ClampParameters(5,1.0f,"W",20, 93);
        cp[6]= new ClampParameters(6,1.0f,"W",30, 62);
        cp[7]= new ClampParameters(7,1.0f,"W",30, 62);
        cp[8]= new ClampParameters(8,1.0f,"W",5, 372);
        cp[9]= new ClampParameters(9,1.0f,"W",5, 372);
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

    private int getScaledMetric(byte[] bytes, MetricType mt, int clamp)
    {
        //TODO extract data and scale based on clamps
        return 0;
    }

    @Override
    public void run()
    {
        int nbrSerialBytes = 0;
        byte[] serialBytes = null;
        String subTopic;
        try
        {
            while (true)
            {
                if (msgArrived)
                {
                    msgArrived = false;
                    //TODO  Check for sequence gaps, fill if necessary
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
                        subTopic = topic+"/"+mt.toString();
                        for (int clamp = 0; clamp<10; clamp++)
                        {
                            content = clamp+ " " + getScaledMetric(serialBytes,mt, clamp);
                            // publish to broker
                            try
                            {
                                MqttMessage message = new MqttMessage(content.getBytes());
                                message.setQos(qos);
                                powerMonitorADCMQQTClient.publish(subTopic, message);
                                //System.out.println("Message published");
                            } catch (MqttException me)
                            {
                                handleMQTTException(me);
                            }
                        }
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
