package me.mawood.powerMonitor;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static java.lang.Thread.sleep;

public class PowerDataProcessor  implements Runnable, MqttCallback
{
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

    private class PowerData
    {
        double apparentPower;
        double realPower;
        double voltage;
        double reactivePower;
        double loadFactor;
        double current;

        PowerData(double apparentPower, double realPower, double voltage, double currentScale, double voltageScale )
        {
            this.apparentPower = apparentPower*currentScale;
            this.realPower = realPower*currentScale;
            this.voltage = voltage*voltageScale;
            this.reactivePower = Math.sqrt(this.apparentPower*this.apparentPower-this.realPower*this.realPower);
            this.loadFactor = this.realPower/this.apparentPower;
            this.current = this.realPower/this.voltage;
        }
    }

    private PowerData scaledPowerData[];

    private final String clientId     = "PMon10";
    private MqttClient publisherClientPMOn10;
    private MqttConnectOptions connOpts;

    private STM8PowerMonitor powerMonitor;
    private ChannelMap[] aDCchannels = new ChannelMap[10];
    private long nbrMessagesSentOK;

    // run control variables
    private volatile boolean msgArrived;
    private volatile boolean stop;

     /**
     * PowerDataProcessor   Constructor
     */
    PowerDataProcessor()
    {
        nbrMessagesSentOK =0;
        stop = false;
        // set up channel configuration
        aDCchannels[0]= new ChannelMap(0,10.0,"V","Voltage");
        aDCchannels[1]= new ChannelMap(1,CurrentClamps.SCT013_5A1V.getMaxCurrent(),"W","UpstairsLighting");
        aDCchannels[2]= new ChannelMap(2,CurrentClamps.SCT013_5A1V.getMaxCurrent(),"W","DownstairsLighting");
        aDCchannels[3]= new ChannelMap(3,CurrentClamps.SCT013_5A1V.getMaxCurrent(),"W","ExtensionLighting");
        aDCchannels[4]= new ChannelMap(4,CurrentClamps.SCT013_5A1V.getMaxCurrent(),"W","OutsideLighting");
        aDCchannels[5]= new ChannelMap(5,CurrentClamps.SCT013_20A1V.getMaxCurrent(),"W","LoungeEndPlugs");
        aDCchannels[6]= new ChannelMap(6,CurrentClamps.SCT013_30A1V.getMaxCurrent(),"W","KitchenPlugs");
        aDCchannels[7]= new ChannelMap(7,CurrentClamps.SCT013_20A1V.getMaxCurrent(),"W","OutsidePlugs");
        aDCchannels[8]= new ChannelMap(8,CurrentClamps.SCT013_30A1V.getMaxCurrent(),"W","Cooker");
        aDCchannels[9]= new ChannelMap(9,CurrentClamps.SCT013_100A1V.getMaxCurrent(),"W","WholeHouse");
        scaledPowerData = new PowerData[9];
        try {
            // set up MQTT stream definitions
            String broker = "tcp://localhost:1883";
            publisherClientPMOn10 = new MqttClient(broker, clientId, new MemoryPersistence());
            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting PowerDataProcessor to broker: "+ broker);
            // make connection to MQTT broker
            publisherClientPMOn10.connect(connOpts);
            System.out.println("PowerDataProcessor Connected");

        } catch(MqttException me) {
            handleMQTTException(me);
        }
    }

    //
    // MqttCallback implementation
    //

    /**
     * connectionLost       The connection to the MQTT server has been lost
     * @param throwable     the cause?
     */
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

    /**
     * messageArrived       An MQTT message has been recieved (not expected to happen
     *                      as this class doesn't subscribe
     * @param s             Topic
     * @param mqttMessage   Message
     * @throws Exception    if we can't handle it
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
     * deliveryComplete             Notification that an MQTT delivery has been successfully recieved at the broker
     * @param iMqttDeliveryToken    Which delivery?
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken)
    {
        nbrMessagesSentOK++;
    }


    /**
     * handleMQTTException  Handler for MQTT exceptions
     * @param me            The MQTT exception
     */
    private void handleMQTTException(MqttException me)
    {
        System.out.println("reason "+me.getReasonCode());
        System.out.println("msg "+me.getMessage());
        System.out.println("loc "+me.getLocalizedMessage());
        System.out.println("cause "+me.getCause());
        System.out.println("excep "+me);
        me.printStackTrace();
    }

    /**
     * setPowerMonitor  Stores the connected powerMonitor object
     * @param pm        The object reference
     */
    void setPowerMonitor(STM8PowerMonitor pm)
    {
        this.powerMonitor = pm;
    }

    /**
     * shutdownDataProcessing   Tidy shutdown of the processor
     */
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

    private PowerData[] calculateScaledPower(MetricsBuffer rawMetrics)
    {
        PowerData[] powerdata = new PowerData[9];
        for(int i = 0; i<8; i++)
        {
            powerdata[i] = new PowerData(   rawMetrics.getApparentPower(i),
                                            rawMetrics.getRealPower(i),
                                            rawMetrics.getRmsVoltage(),
                                            aDCchannels[i+1].scaleFactor,
                                            aDCchannels[0].scaleFactor );
        }
        return powerdata;
    }
    /**
     * publishToBroker - send a message to the MQTT broker
     * @param subTopic  - fully qualified topic identifier
     * @param content   - message content "key data"
     */
    private void publishToBroker(String subTopic, String content)
    {
        final int qos = 2; //The message is always delivered exactly once

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

    //
    // Runnable implementation
    //

    /**
     * run  The main processing loop
     */
    @Override
    public void run()
    {
        String basetopic = "emon";
        String topic = basetopic + "/" + clientId;
        String subTopic;
        MetricsBuffer rawMetricsBuffer;
        try
        {
            while (!stop)
            {
                if (msgArrived)
                {
                    msgArrived = false;
                    //TODO  Check for sequence gaps, fill if necessary

                    rawMetricsBuffer = powerMonitor.getRawMetricsBuffer();
                    //rawMetricsBuffer.printMetricsBuffer();
                    scaledPowerData = calculateScaledPower(rawMetricsBuffer);
                    subTopic = topic +"/"+ aDCchannels[0].name;
                    publishToBroker( subTopic, 3 +" " + scaledPowerData[0].voltage);
                    for (int channel = 0; channel <8; channel++ )
                    {
                        subTopic = topic +"/"+ aDCchannels[channel+1].name;
                        publishToBroker( subTopic,1 + " " + scaledPowerData[channel].apparentPower);
                        publishToBroker( subTopic,2 + " " + scaledPowerData[channel].realPower);
                    }
                }
                sleep(1000); //defines output frequency
            }
        }catch (InterruptedException e)
        {
            shutdownDataProcessing();
            System.out.println("Data Processing Interrupted, exiting");
        }
    }

    /**
     * stop     Method to stop the main processing loop and close down processing
     */
    public void stop()
    {
        stop = true;
    }
}
