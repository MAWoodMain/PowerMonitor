package me.mawood.powerMonitor;

import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.util.Arrays;

import java.nio.ByteBuffer;

import static java.lang.Thread.sleep;

public class PowerDataProcessor  implements SerialDataEventListener, Runnable, MqttCallback
{
    private enum MetricType{RealPower, ApparentPower, Voltage}
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

    private class ChannelMap
    {
        int channelNumber;
        double scaleFactor;
        String units;
        String name;
        ChannelMap(int channel, double scale, String units, String name)
        {
            this.channelNumber = channel;
            this.scaleFactor = scale;
            this.units = units;
            this.name = name;
        }
    }
    private STM8PowerMonitor powerMonitor;
    private ChannelMap[] channels = new ChannelMap[10];

    PowerDataProcessor()
    {
        nbrMessagesSentOK =0;
        stop = false;
        // set up clamp configuration
        channels[0]= new ChannelMap(0,10.0,"V","Voltage");
        channels[1]= new ChannelMap(1,CurrentClamps.SCT013_5A1V.getMaxCurrent(),"W","UpstairsLighting");
        channels[2]= new ChannelMap(2,CurrentClamps.SCT013_5A1V.getMaxCurrent(),"W","DownstairsLighting");
        channels[3]= new ChannelMap(3,CurrentClamps.SCT013_5A1V.getMaxCurrent(),"W","ExtensionLighting");
        channels[4]= new ChannelMap(4,CurrentClamps.SCT013_5A1V.getMaxCurrent(),"W","OutsideLighting");
        channels[5]= new ChannelMap(5,CurrentClamps.SCT013_20A1V.getMaxCurrent(),"W","LoungeEndPlugs");
        channels[6]= new ChannelMap(6,CurrentClamps.SCT013_30A1V.getMaxCurrent(),"W","KitchenPlugs");
        channels[7]= new ChannelMap(7,CurrentClamps.SCT013_20A1V.getMaxCurrent(),"W","OutsidePlugs");
        channels[8]= new ChannelMap(8,CurrentClamps.SCT013_30A1V.getMaxCurrent(),"W","Cooker");
        channels[9]= new ChannelMap(9,CurrentClamps.SCT013_100A1V.getMaxCurrent(),"W","WholeHouse");

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

    void setPowerMonitor(STM8PowerMonitor pm)
    {
        this.powerMonitor = pm;
        pm.AddSerialListener(this);// bind listener to serial port

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

    private double getScaledMetric(byte[] bytes, MetricType mt, int channel)
    {
        final int serialOffsetClamps = 10;
        final int serialOffsetVoltage = 10;
        final int sizeOfDouble = 8;
        double rawValue;
        byte[] bytes8;
        if (nbrSerialBytes < serialOffsetClamps+sizeOfDouble*10) return 0; //message too short
        rawValue=0;
        switch (mt)
        {
            case RealPower:
            {
                bytes8 = Arrays.copyOfRange(bytes, serialOffsetClamps+channel*sizeOfDouble, serialOffsetClamps+channel*sizeOfDouble + sizeOfDouble);
                rawValue = ByteBuffer.wrap(bytes8).getDouble();
                break;
            }
            case ApparentPower:
            {
                bytes8 = Arrays.copyOfRange(bytes, serialOffsetClamps+channel*sizeOfDouble + sizeOfDouble, serialOffsetClamps+channel*sizeOfDouble + sizeOfDouble + sizeOfDouble);
                rawValue = ByteBuffer.wrap(bytes8).getDouble();
            }
            case Voltage:
            {
                bytes8 = Arrays.copyOfRange(bytes, serialOffsetVoltage, serialOffsetVoltage + sizeOfDouble); //always channel 0
                rawValue = ByteBuffer.wrap(bytes8).getDouble();
                break;
            }
        }
        return rawValue* channels[channel].scaleFactor;
    }

    private    void publishToBroker(String subTopic, String content)
    {
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
                    for (int channel = powerMonitor.getMinADCChannel(); channel <powerMonitor.getMAXADCChannnel(); channel++ )
                    {
                        subTopic = topic+"/"+channels[channel].name;
                        if (powerMonitor.getADCChannelType(channel)== STM8PowerMonitor.ChannelType.Current)
                        {
                            publishToBroker( subTopic,1 + " " + getScaledMetric(serialBytes, MetricType.RealPower, channel));
                            publishToBroker( subTopic,2 + " " + getScaledMetric(serialBytes, MetricType.ApparentPower, channel));
                        }
                        else
                        {
                            publishToBroker( subTopic, 3 +" " + getScaledMetric(serialBytes, MetricType.Voltage, channel));
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
