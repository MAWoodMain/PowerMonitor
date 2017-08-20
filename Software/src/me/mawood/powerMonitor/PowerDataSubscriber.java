package me.mawood.powerMonitor;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class PowerDataSubscriber implements  Runnable, MqttCallback
{
    @Override
    public void connectionLost(Throwable throwable)
    {
        System.out.println("Subscriber connection lost!");
        // code to reconnect to the broker would go here if desired

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
    private void handleMQTTException(MqttException me)
    {
        System.out.println("reason "+me.getReasonCode());
        System.out.println("msg "+me.getMessage());
        System.out.println("loc "+me.getLocalizedMessage());
        System.out.println("cause "+me.getCause());
        System.out.println("excep "+me);
        me.printStackTrace();
    }

    @Override
    public void run()
    {

    }
}
