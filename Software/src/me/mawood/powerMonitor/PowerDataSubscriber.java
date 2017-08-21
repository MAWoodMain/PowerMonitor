package me.mawood.powerMonitor;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.util.Arrays;

import static java.lang.Thread.sleep;

public class PowerDataSubscriber implements  Runnable, MqttCallback
{
    // run control variables
    private volatile boolean msgArrived;
    private volatile boolean stop;

    @Override
    public void connectionLost(Throwable throwable)
    {
        System.out.println("Subscriber connection lost!");
        // code to reconnect to the broker would go here if desired

    }
    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception
    {
        msgArrived = true;
        System.out.println("-------------------------------------------------");
        System.out.println("| Topic:" + s);
        System.out.println("| Message: " + new String(mqttMessage.getPayload()));
        System.out.println("-------------------------------------------------");

    }
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken)
    {

    }
    private enum MetricType{Real, Apparent}
    private final String basetopic    = "emon";
    private final String clientId     = "PMon10";
    private final String topic        = basetopic+"/"+clientId;
    private String content ;
    private int qos             = 2;
    private String broker       = "tcp://localhost:1883";
    private MemoryPersistence persistence = new MemoryPersistence();
    private MqttClient subscriberClientPMon10;
    private int nbrMessagesReceivedOK;

    PowerDataSubscriber()
    {
        try {
            subscriberClientPMon10 = new MqttClient(broker, clientId, persistence);
            subscriberClientPMon10.setCallback(this);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting PowerDataSubscriber to broker: "+broker);
            subscriberClientPMon10.connect(connOpts);
            System.out.println("PowerDataSubscriber Connected");

        } catch(MqttException me) {
            handleMQTTException(me);
        }
        try {
            int subQoS = 0;
            subscriberClientPMon10.subscribe(topic, subQoS);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * shutdownDataSubscriber   Tidy shutdown of the processor
     */
    private void shutdownDataSubscriber()
    {
        try
        {
            subscriberClientPMon10.disconnect();
            System.out.println("Disconnected");
        } catch (MqttException me)
        {
            handleMQTTException(me);
        }
        System.out.println("PowerDataProcessor Disconnected");
        System.out.println(nbrMessagesReceivedOK+ " messages Received successfully");
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
    //
    // Runnable implementation
    //

    /**
     * run  The main processing loop
     */
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
                    nbrMessagesReceivedOK++;
                    msgArrived = false;
                }
                sleep(100);
            }
        }catch (InterruptedException e)
        {
            shutdownDataSubscriber();
            System.out.println("Data Processing Intterupted, exiting");
        }
    }

    /**
     * stop     Method to stop the main processing loop and close down processing
     */
    void stop()
    {
        stop = true;
    }
}
