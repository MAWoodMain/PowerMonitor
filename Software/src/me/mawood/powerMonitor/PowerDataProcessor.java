package me.mawood.powerMonitor;

import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.util.Arrays;

import java.nio.ByteBuffer;

import java.nio.ByteBuffer;
import static java.lang.Thread.sleep;

public class PowerDataProcessor  implements SerialDataEventListener, Runnable, MqttCallback
{
    private enum MetricType{Real, Apparent}
    private final String basetopic    = "emon";
    private final String clientId     = "PMon10";
    private final String topic        = basetopic+"/"+clientId;

    private String content ;
    private int qos             = 2; //The message is always delivered exactly once
    private String broker       = "tcp://localhost:1883";
    private MemoryPersistence persistence = new MemoryPersistence();
    private MqttClient publisherClientPMOn10;
    private MqttConnectOptions connOpts;
    private long nbrMessagesSentOK;
    private boolean stop;
    private int nbrSerialBytes;
    private volatile boolean msgArrived;
    private volatile SerialDataEvent serialDataEvent;

    private class ClampParameters
    {
        int clampNumber;
        double scaleFactor;
        String units;
        int maxCurrent;
        int burdenResistor;
        ClampParameters(int clamp, float scale, String units, int maxCurrent, int burdenResistor)
        {
            this.clampNumber = clamp;
            this.scaleFactor = scale;
            this.units = units;
            this.burdenResistor = burdenResistor;
            this.maxCurrent = maxCurrent;
        }
    }
    private ClampParameters[] cp = new ClampParameters[10];

    PowerDataProcessor()
    {
        nbrMessagesSentOK =0;
        stop = false;
        // set up clamp configuration
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
            // set up MQTT stream definitions
            publisherClientPMOn10 = new MqttClient(broker, clientId, persistence);
            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting PowerDataProcessor to broker: "+broker);
            // make connection to MQTT broker
            publisherClientPMOn10.connect(connOpts);
            System.out.println("PowerDataProcessor Connected");

        } catch(MqttException me) {
            handleMQTTException(me);
        }

    }
    @Override
    public void connectionLost(Throwable throwable)
    {
        System.out.println("Subscriber connection lost!");
        // code to reconnect to the broker
        try
        {
            publisherClientPMOn10.connect(connOpts);
        } catch (MqttException me)
        {
            handleMQTTException(me);
            System.exit(1);
        }
    }
    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception
    {
        System.out.println("-------------------------------------------------");
        System.out.println("| Topic:" + s);
        System.out.println("| Message: " + new String(mqttMessage.getPayload()));
        System.out.println("-------------------------------------------------");

    }
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken)
    {
        nbrMessagesSentOK++;
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
            publisherClientPMOn10.disconnect();
            System.out.println("Disconnected");
        } catch (MqttException me)
        {
            handleMQTTException(me);
        }
        System.out.println("PowerDataProcessor Disconnected");
        System.out.println(nbrMessagesSentOK+ " messages sent successfully");
    }

    private double getScaledMetric(byte[] bytes, MetricType mt, int clamp)
    {
        final int serialOffsetClamps = 10;
        final int sizeOfDouble = 8;
        double rawValue;
        byte[] bytes8;
        if (nbrSerialBytes < serialOffsetClamps+sizeOfDouble*10) return 0; //message too short
        if (mt==MetricType.Real)
        {
            bytes8 = Arrays.copyOfRange(bytes, serialOffsetClamps, serialOffsetClamps+sizeOfDouble);
            rawValue = ByteBuffer.wrap(bytes8).getDouble();
        }
        else
        {
            bytes8 = Arrays.copyOfRange(bytes, serialOffsetClamps+sizeOfDouble, serialOffsetClamps+sizeOfDouble+sizeOfDouble);
            rawValue = ByteBuffer.wrap(bytes8).getDouble();
        }
        return rawValue*cp[clamp].scaleFactor;
    }

    @Override
    public void run()
    {
        byte[] serialBytes = null;
        String subTopic;
        try
        {
            while (!stop)
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
                            //clamp is the key
                            content = clamp+ " " + getScaledMetric(serialBytes,mt, clamp);
                            // publish to broker
                            try
                            {
                                MqttMessage message = new MqttMessage(content.getBytes());
                                message.setQos(qos);
                                publisherClientPMOn10.publish(subTopic, message);
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
    public void stop()
    {
        stop = true;
    }
}
