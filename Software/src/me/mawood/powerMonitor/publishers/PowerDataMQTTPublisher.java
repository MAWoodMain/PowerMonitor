package me.mawood.powerMonitor.publishers;

import me.mawood.powerMonitor.circuits.Circuit;
import me.mawood.powerMonitor.metrics.InvalidDataException;
import me.mawood.powerMonitor.metrics.MetricReading;
import me.mawood.powerMonitor.metrics.PowerMetricCalculator;
import me.mawood.powerMonitor.metrics.units.Current;
import me.mawood.powerMonitor.metrics.units.Power;
import me.mawood.powerMonitor.metrics.units.Voltage;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.naming.OperationNotSupportedException;
import java.time.Instant;
import java.util.Map;

public class PowerDataMQTTPublisher extends Thread implements MqttCallback
{
    private class ChannelMap
    {
        final int channelNumber;
        final double scaleFactor;
        final String units;
        final String name;

        private ChannelMap(int channelNumber, double scaleFactor, String units, String name)
        {
            this.channelNumber = channelNumber;
            this.scaleFactor = scaleFactor;
            this.units = units;
            this.name = name;
        }
    }

    private static final String CLIENT_ID = "PMon10";
    private static final String TOPIC = "emon/" + CLIENT_ID;
    private static final String BROKER = "tcp://10.0.128.2:1883";
    private static final String USERNAME = "emonpi";
    private static final String PASSWORD = "emonpimqtt2016";

    private final MqttClient mqttClient;
    private final MqttConnectOptions connOpts;

    private long noMessagesSentOK;

    // run control variables
    private volatile boolean msgArrived;
    private final Map<Circuit, PowerMetricCalculator> circuitMap;

    /**
     * PowerDataMQTTPublisher   Constructor
     */
    public PowerDataMQTTPublisher(Map<Circuit, PowerMetricCalculator> circuitMap) throws MqttException
    {
        this.circuitMap = circuitMap;
        noMessagesSentOK = 0;

        mqttClient = new MqttClient(BROKER, CLIENT_ID, new MemoryPersistence());
        // set up MQTT stream definitions
        connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(USERNAME);
        connOpts.setPassword(PASSWORD.toCharArray());
        System.out.println("Connecting PowerDataMQTTPublisher to broker: " + BROKER);
        // make connection to MQTT broker
        mqttClient.connect(connOpts);
        System.out.println("PowerDataMQTTPublisher Connected");
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
     * messageArrived       An MQTT message has been received (not expected to happen
     * as this class doesn't subscribe
     *
     * @param s           Topic
     * @param mqttMessage Message
     * @throws Exception if we can't handle it
     */
    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception
    {
        /*
        System.out.println("-------------------------------------------------");
        System.out.println("| Topic:" + s);
        System.out.println("| Message: " + new String(mqttMessage.getPayload()));
        System.out.println("-------------------------------------------------");
        */
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
        System.out.println("PowerDataMQTTPublisher disconnected from MQTT Broker");
        System.out.println(noMessagesSentOK + " messages sent successfully");
    }

    /**
     * publishToBroker - send a message to the MQTT broker
     *
     * @param subTopic - fully qualified TOPIC identifier
     * @param content  - message content "key data"
     */
    private void publishToBroker(String subTopic, String content)
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

    private void publishCircuitToBroker(Circuit circuit) throws InvalidDataException, OperationNotSupportedException
    {
        String subTopic= TOPIC + "/" + circuit.getDisplayName().replace(" ", "_");
        Instant readingTime =  Instant.now().minusSeconds(1);
        MetricReading apparent = circuitMap.get(circuit).getAverageBetween(Power.VA, Instant.now().minusSeconds(2), readingTime);
        MetricReading real = circuitMap.get(circuit).getAverageBetween(Power.WATTS, Instant.now().minusSeconds(2), readingTime);
        MetricReading reactive = circuitMap.get(circuit).getAverageBetween(Power.VAR, Instant.now().minusSeconds(2), readingTime);

        MetricReading current = circuitMap.get(circuit).getAverageBetween(Current.AMPS, Instant.now().minusSeconds(2), readingTime);
        Double powerFactor = Math.cos(Math.atan(reactive.getValue()/real.getValue()));
        String jsonReadings =
                "{\"Time\":\""+readingTime.toString()+"\","+
                "\"Readings\":{"+
                "\"Real\":"+ real.getValue().toString()+","+
                "\"Apparent\":"+ apparent.getValue().toString()+","+
                "\"Reactive\":"+ reactive.getValue().toString()+","+
                "\"Current\":"+ current.getValue().toString()+","+
                "\"PowerFactor\":"+ powerFactor.toString();
        if (subTopic.contains("Whole_House"))
        {
            MetricReading voltage = circuitMap.get(circuit).getAverageBetween(Voltage.VOLTS, Instant.now().minusSeconds(2), Instant.now().minusSeconds(1));
            jsonReadings = jsonReadings+",\"Voltage\":"+ voltage.toString();
        }
        jsonReadings = jsonReadings+"}}";
        publishToBroker(subTopic,jsonReadings);
        /*
        publishMetricToBroker(subTopic + "/ApparentPower", apparent);
        publishMetricToBroker(subTopic + "/RealPower", real);
        if (subTopic.contains("Whole_House"))
        {
            MetricReading voltage = circuitMap.get(circuit).getAverageBetween(Voltage.VOLTS, Instant.now().minusSeconds(2), Instant.now().minusSeconds(1));
            publishMetricToBroker(subTopic + "/Voltage", voltage);
        }*/
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
        long startTime;
        try
        {
            // wait for first readings to be ready
            Thread.sleep(2010);
        } catch (InterruptedException ignored)
        {
        }
        while (!Thread.interrupted())
        {
            startTime = System.currentTimeMillis();
            //rawMetricsBuffer.printMetricsBuffer();

            for (Circuit circuit : circuitMap.keySet())
            {
                try
                {
                    publishCircuitToBroker(circuit);
                } catch (InvalidDataException | OperationNotSupportedException e)
                {
                    //System.out.println("no data for circuit: " + circuit.getDisplayName());
                }
            }


            //Frequency
            while (startTime + 1000 > System.currentTimeMillis())
            {
                // wait half the remaining time
                try
                {
                    Thread.sleep(Math.max(0, ((startTime + 1000) - System.currentTimeMillis()) / 2));
                } catch (InterruptedException ignore)
                {
                }
            }
        }
        shutdownDataProcessing();
        System.out.println("Data Processing Interrupted, exiting");
        System.exit(0);
    }
}
