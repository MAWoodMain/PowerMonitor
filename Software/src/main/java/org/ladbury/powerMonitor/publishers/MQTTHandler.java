package org.ladbury.powerMonitor.publishers;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.LinkedBlockingQueue;


public class MQTTHandler implements MqttCallback
{

    private static String brokerAddress = "10.0.128.2";
    private static final String PORT = "1883";
    private static final String PROTOCOL = "tcp";
    private static String broker;

    private static final String USERNAME = "emonpi";
    private static final String PASSWORD = "emonpimqtt2016";

    private static String clientID = "PMon10";
    private static  String topic = "emon/" + clientID;
    private static  String cmndTopic = topic +"/cmnd";
    private static  String logTopic = topic+"/log";

    public static String getTopic()
    {
        return topic;
    }

    private final MqttClient mqttClient;
    private final MqttConnectOptions connOpts;

    private long noMessagesSentOK;

    // run control variables
    LinkedBlockingQueue<String> loggingQ;
    LinkedBlockingQueue<String> commandQ;
    /**
     * MQTTHandler   Constructor
     */
    public MQTTHandler( String brokerAddr,
                        String clientname,
                        LinkedBlockingQueue<String> loggingQ,
                        LinkedBlockingQueue<String> commandQ) throws MqttException
    {
        this.loggingQ = loggingQ;
        this.commandQ = commandQ;
        noMessagesSentOK = 0;
        if (brokerAddr != null) {
            if (!brokerAddr.equals(""))
            {
                brokerAddress = brokerAddr;
            }
        }
        broker = PROTOCOL + "://" + brokerAddress + ":" + PORT;
        if (clientname != null) {
            if (!clientname.equals(""))
            {
                clientID = clientname;
                topic =  "emon/" + clientname;
                logTopic = topic + "/log";
                cmndTopic = topic + "/cmnd";
            }
        }
        mqttClient = new MqttClient(broker, clientID, new MemoryPersistence());
        // set up MQTT stream definitions
        connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(USERNAME);
        connOpts.setPassword(PASSWORD.toCharArray());
        System.out.println("Connecting MQTTHandler to broker: " + broker);
        // make connection to MQTT broker
        try {
            mqttClient.connect(connOpts);
            System.out.println("MQTTHandler Connected");
            loggingQ.add("MQTTHandler Connected");
            mqttClient.setCallback(this);
            mqttClient.subscribe(cmndTopic + "/#");
            loggingQ.add("MQTTHandler: Subscribed to <" + cmndTopic + ">");
        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("broker: "+broker + " Addr param" +brokerAddr + " Full addr: "+ brokerAddress);
        }
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
        System.out.println("Subscriber connection lost! " + broker);
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
        String payload = String.valueOf(mqttMessage.getPayload());
        loggingQ.add("MQTT msg received Topic: " + topic + " Message: " + payload);
        System.out.println("MQTT msg received Topic: " + topic + " Message: " +payload);
        String[] subtopics = topic.split("/");
        if (subtopics[2].equalsIgnoreCase("cmnd"))
        {
            commandQ.add(payload);
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
        System.out.println("MQTTHandler disconnected from MQTT Broker");
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
        publishToBroker(logTopic,msg);
    }

}
