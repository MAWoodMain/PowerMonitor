package me.mawood.powerMonitor;

import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class PowerDataProcessor  implements SerialDataEventListener
{
    private String topic        = "emon";
    private String content      = "test message";
    private int qos             = 2;
    private String broker       = "tcp://localhost:1883";
    private String clientId     = "PowerMonitorADC";
    private MemoryPersistence persistence = new MemoryPersistence();
    private MqttClient powerMonitorADCMQQTClient;

    public PowerDataProcessor()
    {
        // set up clamp configuration
        // bind listener to serial port
        // set up stream definitions
        // make connection to MQTT broker
        try {
            powerMonitorADCMQQTClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: "+broker);
            powerMonitorADCMQQTClient.connect(connOpts);
            System.out.println("Connected");

        } catch(MqttException me) {
            handleMQTTException(me);
        }

    }
    @Override
    public void dataReceived(SerialDataEvent serialDataEvent)
    {
        // unwrap the data
        // Check for sequence gaps, fill if necessary
        // extract data and scale based on clamps
        // publish to broker
        try {
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            powerMonitorADCMQQTClient.publish(topic, message);
            System.out.println("Message published");
            powerMonitorADCMQQTClient.disconnect();
            System.out.println("Disconnected");
            System.exit(0);
        } catch(MqttException me) {
            handleMQTTException(me);
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
    public void shutdownDataProcessing()
    {
        try
        {
            powerMonitorADCMQQTClient.disconnect();
        } catch (MqttException me)
        {
            handleMQTTException(me);
        }
        System.out.println("Disconnected");
        System.exit(0);
    }
}
