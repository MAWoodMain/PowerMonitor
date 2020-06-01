package org.ladbury.powerMonitor.publishers;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.ladbury.powerMonitor.Main;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;


@SuppressWarnings("SpellCheckingInspection")
public class MQTTHandler implements MqttCallback
{

    private static String brokerAddress = "10.0.128.2";
    private static final String PORT = "1883";
    private static final String PROTOCOL = "tcp";
    private static String broker;

    private static final String USERNAME = "emonpi";
    private static final String PASSWORD = "emonpimqtt2016";

    private static String clientID;
    private static String topic;
    private static String cmndTopic;
    private static String logTopic;
    private static String responseTopic;
    private static String telemetryTopic;


    private final MqttClient mqttClient;
    private final MqttConnectOptions connOpts;
    private final PMLogger logger;

    private long noMessagesSentOK;

    // run control variables
    final LinkedBlockingQueue<String> commandQ;
    /**
     * MQTTHandler   Constructor
     */
    public MQTTHandler( String brokerAddr,
                        String clientname,
                        LinkedBlockingQueue<String> commandQ) throws MqttException
    {
        this.logger = Main.getLogger();
        this.commandQ = commandQ;
        noMessagesSentOK = 0;
        if (brokerAddr != null) {
            if (!brokerAddr.equals(""))
            {
                brokerAddress = brokerAddr;
            }
        }
        broker = PROTOCOL + "://" + brokerAddress + ":" + PORT;
        //set up client id
        clientID = "PMon10";
        if (clientname != null) {
            if (clientname.equals("")) {
                clientname = clientID;
            }
        } else clientname = clientID;
        clientID = clientname; //overwrite clientID as we have a valid name now wether specfied or not

        //set up topics
        topic =  "emon/" + clientname;
        logTopic = topic + "/log";
        cmndTopic = topic + "/cmnd";
        responseTopic = topic+ "/response";
        telemetryTopic = topic + "/tele";

        mqttClient = new MqttClient(broker, clientID, new MemoryPersistence());
        // set up MQTT stream definitions
        connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(USERNAME);
        connOpts.setPassword(PASSWORD.toCharArray());
        connOpts.setAutomaticReconnect(true);
        if (!connectToBroker()){ System.exit(1); }
    }
    private boolean connectToBroker()
    {
        System.out.println("Connecting MQTTHandler to broker: " + broker);
        // make connection to MQTT broker
        try {
            mqttClient.connect(connOpts);
            System.out.println("MQTTHandler Connected");
            logger.add("Connected", Level.INFO,this.getClass().getName());
            mqttClient.setCallback(this);
            mqttClient.subscribe(cmndTopic + "/#");
            logger.add("Subscribed to <" + cmndTopic + ">", Level.INFO,this.getClass().getName());
            return true;
        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Couldn't connect to broker: "+broker + " Addr param" +brokerAddress + " Full addr: "+ brokerAddress);
        }
        return false;
    }
    public String getTopic()
    {
        return topic;
    }
    public String getResponseTopic()
    {
        return responseTopic;
    }
    public String getTelemetryTopic()
    {
        return telemetryTopic;
    }
    public String getLogTopic()
    {
        return logTopic;
    }
    public String getCommandTopic()
    {
        return cmndTopic;
    }

    public static String getClientID()
    {
        return clientID;
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
    @SuppressWarnings("RedundantThrows")
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception
    {
        String payload = new String(mqttMessage.getPayload(), StandardCharsets.UTF_8);
        logger.add("msg received Topic: " + topic + " Message: " + payload, Level.INFO,this.getClass().getName());
        System.out.println("msg received Topic: " + topic + " Message: " +payload);
        if (topic.equalsIgnoreCase(cmndTopic))
        {
            logger.add("msg is command, adding to queue", Level.INFO,this.getClass().getName());
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
            connectToBroker(); // will retry on next message if fails
        }
    }

    public void logToBroker(String msg)
    {
        publishToBroker(getLogTopic(),msg);
    }

}
