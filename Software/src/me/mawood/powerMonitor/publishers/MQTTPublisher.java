package me.mawood.powerMonitor.publishers;

import me.mawood.powerMonitor.metrics.MetricReading;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;


public class MQTTPublisher extends Thread implements MqttCallback
{

    private static final String CLIENT_ID = "PMon10";
    public static final String TOPIC = "emon/" + CLIENT_ID;
    private static final String BROKER = "tcp://10.0.128.2:1883";
    private static final String USERNAME = "emonpi";
    private static final String PASSWORD = "emonpimqtt2016";
    private static final String CMND_TOPIC = TOPIC+"/cmnd/#";
    private static final String LOG_TOPIC = TOPIC+"/log";

    private final MqttClient mqttClient;
    private final MqttConnectOptions connOpts;

    private long noMessagesSentOK;

    // run control variables
    LinkedBlockingQueue<String> loggingQ;
    LinkedBlockingQueue<String> commandQ;
    /**
     * MQTTPublisher   Constructor
     */
    public MQTTPublisher(LinkedBlockingQueue<String> loggingQ,
                         LinkedBlockingQueue<String> commandQ) throws MqttException
    {
        this.loggingQ = loggingQ;
        this.commandQ = commandQ;
        noMessagesSentOK = 0;

        mqttClient = new MqttClient(BROKER, CLIENT_ID, new MemoryPersistence());
        // set up MQTT stream definitions
        connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(USERNAME);
        connOpts.setPassword(PASSWORD.toCharArray());
        System.out.println("Connecting MQTTPublisher to broker: " + BROKER);
        // make connection to MQTT broker
        mqttClient.connect(connOpts);
        System.out.println("MQTTPublisher Connected");
        loggingQ.add("MQTTPublisher Connected");
        mqttClient.setCallback(this);
        mqttClient.subscribe(CMND_TOPIC);
        loggingQ.add("MQTTPublisher: Subscribed to <"+ CMND_TOPIC+">");
    }

    //
    // MqttCallback implementation
    //

    /**
     * connectionLost       The connection to the MQTT server has been lost
     *
     * @param throwable the cause?
     */
    @Override
    public void connectionLost(Throwable throwable)
    {
        System.out.println("Subscriber connection lost!");
        // code to reconnect to the broker
        try
        {
            mqttClient.connect(connOpts);
        } catch (MqttException me)
        {
            handleMQTTException(me);
            System.exit(1);
        }
    }

    /**
     * messageArrived       An MQTT message has been received
     *                      this class is subscribed cmnd subtopic
     *
     * @param topic           Topic
     * @param mqttMessage Message
     * @throws Exception if we can't handle it
     */
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception
    {
        loggingQ.add("MQTT msg received Topic: " + topic + " Message: " + new String(mqttMessage.getPayload()));
        System.out.println("MQTT msg received Topic: " + topic + " Message: " + new String(mqttMessage.getPayload()));
        String[] subtopics = topic.split("/");
        if (subtopics[2].equalsIgnoreCase("cmnd"))
        {
            commandQ.add(Arrays.toString(mqttMessage.getPayload()));
        }
    }

    /**
     * deliveryComplete             Notification that an MQTT delivery has been successfully received at the broker
     *
     * @param iMqttDeliveryToken Which delivery?
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken)
    {
        noMessagesSentOK++;
    }


    /**
     * handleMQTTException  Handler for MQTT exceptions
     *
     * @param me The MQTT exception
     */
    public static void handleMQTTException(MqttException me)
    {
        System.out.println("reason " + me.getReasonCode());
        System.out.println("msg " + me.getMessage());
        System.out.println("loc " + me.getLocalizedMessage());
        System.out.println("cause " + me.getCause());
        System.out.println("exception " + me);
        me.printStackTrace();
    }

    /**
     * shutdownDataProcessing   Tidy shutdown of the processor
     */
    private void shutdownDataProcessing()
    {
        try
        {
            mqttClient.disconnect();
        } catch (MqttException me)
        {
            handleMQTTException(me);
        }
        System.out.println("MQTTPublisher disconnected from MQTT Broker");
        System.out.println(noMessagesSentOK + " messages sent successfully");
    }

    /**
     * publishToBroker - send a message to the MQTT broker
     *
     * @param subTopic - fully qualified TOPIC identifier
     * @param content  - message content "key data"
     */
    public void publishToBroker(String subTopic, String content)
    {
        //System.out.println("'"+subTopic+"'"+"'"+content+"'");
        final int qos = 2; //The message is always delivered exactly once

        try
        {
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            mqttClient.publish(subTopic, message);
            // System.out.println("Message published to " + subTopic);
        } catch (MqttException me)
        {
            handleMQTTException(me);
        }
    }

    public void logToBroker(String msg)
    {
        publishToBroker(LOG_TOPIC,msg);
    }

    private void publishMetricToBroker(String subTopic, MetricReading metricReading)
    {
        final int qos = 2; //The message is always delivered exactly once

        metricReading.suppressNoise();
        try
        {
            MqttMessage message = new MqttMessage(metricReading.toString().getBytes());
            message.setQos(qos);
            mqttClient.publish(subTopic, message);
        } catch (MqttException me)
        {
            handleMQTTException(me);
        }
    }


    //
    // Runnable implementation
    //

    /**
     * run  The main publishers loop
     */
    @Override
    public void run()
    {
        try
        {
            // wait for first readings to be ready
            Thread.sleep(2010);
        } catch (InterruptedException ignored){}
        while (!Thread.interrupted())
        {
           //rawMetricsBuffer.printMetricsBuffer();
            try
            {
                // wait for first readings to be ready
                Thread.sleep(100);
            } catch (InterruptedException ignored){}

        }
        shutdownDataProcessing();
        System.out.println("MQTT Processing Interrupted, exiting");
        loggingQ.add("MQTT Processing Interrupted, exiting");
        System.exit(0);
    }
}
