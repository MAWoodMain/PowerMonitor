package me.mawood.powerMonitor.mqqt;

import me.mawood.powerMonitor.circuits.Circuits;
import me.mawood.powerMonitor.metrics.InvalidDataException;
import me.mawood.powerMonitor.metrics.Metric;
import me.mawood.powerMonitor.metrics.PowerMetricCalculator;
import me.mawood.powerMonitor.metrics.units.Power;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.naming.OperationNotSupportedException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;

public class PowerDataProcessor extends Thread implements MqttCallback
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
    private static final String BROKER = "tcp://localhost:1883";
    private static final String USERNAME = "emonpi";
    private static final String PASSWORD = "emonpimqtt2016";
    private static final double NOISE_FILTER = 2d; // ignore metrics whose absolute value is smaller than this

    private final MqttClient mqttClient;
    private final MqttConnectOptions connOpts;

    private long noMessagesSentOK;

    // run control variables
    private volatile boolean msgArrived;
    private final Map<Circuits, PowerMetricCalculator> circuitMap;

    /**
     * PowerDataProcessor   Constructor
     */
    public PowerDataProcessor(Map<Circuits, PowerMetricCalculator> circuitMap) throws MqttException
    {
        this.circuitMap = circuitMap;
        noMessagesSentOK = 0;

        mqttClient = new MqttClient(BROKER, CLIENT_ID, new MemoryPersistence());
        // set up MQTT stream definitions
        connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(USERNAME);
        connOpts.setPassword(PASSWORD.toCharArray());
        System.out.println("Connecting PowerDataProcessor to broker: " + BROKER);
        // make connection to MQTT broker
        mqttClient.connect(connOpts);
        System.out.println("PowerDataProcessor Connected");
        this.start();
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
        System.out.println("-------------------------------------------------");
        System.out.println("| Topic:" + s);
        System.out.println("| Message: " + new String(mqttMessage.getPayload()));
        System.out.println("-------------------------------------------------");

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
        System.out.println("PowerDataProcessor disconnected from MQTT Broker");
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


    private void publishMetricToBroker(String subTopic, Metric metric)
    {
        final int qos = 2; //The message is always delivered exactly once

        final DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime( FormatStyle.MEDIUM )
                        .withLocale( Locale.UK )
                        .withZone( ZoneId.systemDefault() );

        //String content = String.format("%.03f %s at %s", metric.getValue(),metric.getUnit().getSymbol(), formatter.format(metric.getTimestamp()));
        double value = metric.getValue();
        if (Math.abs(value) < NOISE_FILTER) value = 0d;
        String content = String.format("%.03f",value );

        try
        {
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            mqttClient.publish(subTopic, message);
        } catch (MqttException me)
        {
            handleMQTTException(me);
        }
    }

    private void publishCircuitToBroker(Circuits circuit) throws InvalidDataException, OperationNotSupportedException
    {
        String subTopic = TOPIC + "/" + circuit.getDisplayName().replace(" ", "_");
        //Metric apparent = circuitMap.get(circuit).getAverageBetween(Power.VA, Instant.now().minusSeconds(2), Instant.now().minusSeconds(1));
        //publishToBroker(subTopic, String.format("ApparentPower %.03f", apparent.getValue()));
        subTopic = TOPIC + "/" + circuit.getDisplayName().replace(" ", "_");
        Metric real = circuitMap.get(circuit).getAverageBetween(Power.WATTS, Instant.now().minusSeconds(2), Instant.now().minusSeconds(1));
        double value = real.getValue();
        if (Math.abs(value) < NOISE_FILTER) value = 0d;
        publishToBroker(subTopic, String.format("%.03f",value));
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
        String subTopic;
        long startTime;
        try
        {
            // wait for first readings to be ready
            Thread.sleep(2010);
        } catch (InterruptedException ignored) {}
        while (!Thread.interrupted())
        {
            startTime = System.currentTimeMillis();
            //rawMetricsBuffer.printMetricsBuffer();
            try
            {
                /*
                subTopic = TOPIC + "/" +WHOLE_HOUSE.getDisplayName().replace(" ", "_");
                Metric voltage = circuitMap.get(WHOLE_HOUSE).getAverageBetween(Voltage.VOLTS, Instant.now().minusSeconds(2), Instant.now().minusSeconds(1));
                publishToBroker(subTopic,String.format("%.03f", voltage.getValue()));
                */
                for(Circuits circuit:circuitMap.keySet())
                {
                    publishCircuitToBroker(circuit);
                }

            } catch (OperationNotSupportedException | InvalidDataException e)
            {
                e.printStackTrace();
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
    }
}
