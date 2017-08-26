package me.mawood.powerMonitor.old;
import me.mawood.powerMonitor.CurrentClampConfig;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static java.lang.Thread.sleep;

class PowerDataProcessor  implements Runnable, MqttCallback
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

    private class PowerData
    {
        final double apparentPower;
        final double realPower;
        final double voltage;
        final double reactivePower;
        final double loadFactor;
        final double current;

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
    private final MqttClient publisherClientPMOn10;
    private final MqttConnectOptions connOpts;

    private final PowerMonitor powerMonitor;
    private final ChannelMap[] adcChannels = new ChannelMap[10];
    private long noMessagesSentOK;

    // run control variables
    private volatile boolean msgArrived;
    private volatile boolean stop;

     /**
     * PowerDataProcessor   Constructor
     */
    PowerDataProcessor(PowerMonitor powerMonitor) throws MqttException
    {
        this.powerMonitor = powerMonitor;

        noMessagesSentOK =0;
        stop = false;
        // set up channel configuration
        adcChannels[0]= new ChannelMap(0,10.0,"V","Voltage");
        adcChannels[1]= new ChannelMap(1, CurrentClampConfig.SCT013_5A1V.getMaxCurrent(),"W","UpstairsLighting");
        adcChannels[2]= new ChannelMap(2, CurrentClampConfig.SCT013_5A1V.getMaxCurrent(),"W","DownstairsLighting");
        adcChannels[3]= new ChannelMap(3, CurrentClampConfig.SCT013_5A1V.getMaxCurrent(),"W","ExtensionLighting");
        adcChannels[4]= new ChannelMap(4, CurrentClampConfig.SCT013_5A1V.getMaxCurrent(),"W","OutsideLighting");
        adcChannels[5]= new ChannelMap(5, CurrentClampConfig.SCT013_20A1V.getMaxCurrent(),"W","LoungeEndPlugs");
        adcChannels[6]= new ChannelMap(6, CurrentClampConfig.SCT013_30A1V.getMaxCurrent(),"W","KitchenPlugs");
        adcChannels[7]= new ChannelMap(7, CurrentClampConfig.SCT013_20A1V.getMaxCurrent(),"W","OutsidePlugs");
        adcChannels[8]= new ChannelMap(8, CurrentClampConfig.SCT013_30A1V.getMaxCurrent(),"W","Cooker");
        adcChannels[9]= new ChannelMap(9, CurrentClampConfig.SCT013_100A1V.getMaxCurrent(),"W","WholeHouse");
        scaledPowerData = new PowerData[9];

        String broker = "tcp://localhost:1883";
        publisherClientPMOn10 = new MqttClient(broker, clientId, new MemoryPersistence());
        // set up MQTT stream definitions
        connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        System.out.println("Connecting PowerDataProcessor to broker: "+ broker);
        // make connection to MQTT broker
        publisherClientPMOn10.connect(connOpts);
        System.out.println("PowerDataProcessor Connected");
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
     * messageArrived       An MQTT message has been received (not expected to happen
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
     * deliveryComplete             Notification that an MQTT delivery has been successfully received at the broker
     * @param iMqttDeliveryToken    Which delivery?
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken)
    {
        noMessagesSentOK++;
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
        System.out.println(noMessagesSentOK + " messages sent successfully");
    }

    private PowerData[] calculateScaledPower(MetricsBuffer rawMetrics)
    {
        PowerData[] powerData = new PowerData[rawMetrics.getNoPowerChannels()];
        for(int i = 0; i < rawMetrics.getNoPowerChannels(); i++)
        {
            powerData[i] = new PowerData(   rawMetrics.getApparentPower(i),
                                            rawMetrics.getRealPower(i),
                                            rawMetrics.getRmsVoltage(),
                                            adcChannels[i+1].scaleFactor,
                                            adcChannels[0].scaleFactor );
        }
        return powerData;
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
        String baseTopic = "emon";
        String topic = baseTopic + "/" + clientId;
        String subTopic;
        MetricsBuffer rawMetricsBuffer;
        try
        {
            while (!Thread.interrupted() && !stop)
            {
                if (msgArrived)
                {
                    msgArrived = false;
                    //TODO  Check for sequence gaps, fill if necessary

                    rawMetricsBuffer = powerMonitor.getAndResetRawMetricsBuffer();
                    //rawMetricsBuffer.printMetricsBuffer();
                    scaledPowerData = calculateScaledPower(rawMetricsBuffer);
                    subTopic = topic +"/"+ adcChannels[0].name;
                    publishToBroker( subTopic, 3 +" " + scaledPowerData[0].voltage);
                    for (int channel = 0; channel < rawMetricsBuffer.getNoPowerChannels(); channel++ )
                    {
                        subTopic = topic +"/"+ adcChannels[channel+1].name;
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
    void stop()
    {
        stop = true;
    }
}
