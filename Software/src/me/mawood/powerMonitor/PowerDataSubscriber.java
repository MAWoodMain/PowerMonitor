package me.mawood.powerMonitor;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static java.lang.Thread.sleep;

class PowerDataSubscriber implements  Runnable, MqttCallback
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

    private static final String baseTopic = "emon";
    private static final String clientId     = "PMon10";
    private static final String topic        = baseTopic +"/"+clientId;
    private static final int qos             = 2;
    private static final String broker       = "tcp://localhost:1883";
    private final MqttClient subscriberClientPMon10;
    private int nbrMessagesReceivedOK;

    PowerDataSubscriber() throws MqttException
    {
        subscriberClientPMon10 = new MqttClient(broker, clientId, new MemoryPersistence());
        subscriberClientPMon10.setCallback(this);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        System.out.println("Connecting PowerDataSubscriber to broker: "+broker);
        subscriberClientPMon10.connect(connOpts);
        System.out.println("PowerDataSubscriber Connected");
        subscriberClientPMon10.subscribe(topic, qos);

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
            while (!Thread.interrupted() && !stop)
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
            System.out.println("Data Processing Interrupted, exiting");
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
