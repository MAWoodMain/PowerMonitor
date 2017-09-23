package me.mawood.powerMonitor.processing;

import me.mawood.data_api_client.accessors.DeviceAccessor;
import me.mawood.data_api_client.objects.Device;
import me.mawood.powerMonitor.circuits.Circuit;
import me.mawood.powerMonitor.metrics.InvalidDataException;
import me.mawood.powerMonitor.metrics.Reading;
import me.mawood.powerMonitor.metrics.PowerMetricCalculator;
import me.mawood.powerMonitor.metrics.units.Power;
import me.mawood.powerMonitor.metrics.units.Voltage;

import javax.naming.OperationNotSupportedException;
import javax.ws.rs.BadRequestException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class PowerDataDatabaseUpdater extends Thread
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
    private static final String API_URL = "http://silent-fox/api/";
    private static final String USERNAME = "gjwood";
    private static final String PASSWORD = "P@ssw0rd";

    private long noMessagesSentOK;

    // run control variables
    private volatile boolean msgArrived;
    private final Map<Circuit, PowerMetricCalculator> circuitMap;
    private final DeviceAccessor deviceAccessor;
    private Map<Circuit, Device> deviceMap = new HashMap<>();

    /**
     * PowerDataMQTTPublisher   Constructor
     */
    public PowerDataDatabaseUpdater(Map<Circuit, PowerMetricCalculator> circuitMap)
    {
        deviceAccessor = new DeviceAccessor(API_URL);
        Device device;

        this.circuitMap = circuitMap;
        for (Circuit circuit : circuitMap.keySet())
        {
            try
            {
                device = deviceAccessor.getDevice(circuit.getTag());
                deviceMap.put(circuit,device);
            }
            catch (BadRequestException e)
            {
                device = new Device();
                device.setName(circuit.getDisplayName());
                device.setTag(circuit.getTag());
                deviceMap.put(circuit,device);
                deviceAccessor.addDevice(device);
            }
        }
        noMessagesSentOK = 0;
        //connect to DB
        System.out.println("PowerDataDatabaseUpdater devices established");
    }

    /**
     * shutdownDatabaseProcessing   Tidy shutdown of the processor
     */
    private void shutdownDatabaseProcessing()
    {
        System.out.println("PowerDataDataBaseUpdate disconnected from DB");
        System.out.println(noMessagesSentOK + " messages sent successfully");
    }

    /**
     * updateDatabase - send a message to the MQTT broker
     *
     * @param tag - fully qualified TOPIC identifier
     * @param content  - message content "key data"
     */
    private void updateDatabase(String tag, String content)
    {
    }

    private void AddReadingToDatabase(String tag, Reading reading)
    {

    }

    private void updateCircuitInDatabase(Circuit circuit) throws InvalidDataException, OperationNotSupportedException
    {
        String subTopic;
        subTopic =  "/" + circuit.getDisplayName().replace(" ", "_");
        Reading apparent = circuitMap.get(circuit).getAverageBetween(Power.VA, Instant.now().minusSeconds(2), Instant.now().minusSeconds(1));
        AddReadingToDatabase(subTopic + "/ApparentPower", apparent);
        Reading real = circuitMap.get(circuit).getAverageBetween(Power.WATTS, Instant.now().minusSeconds(2), Instant.now().minusSeconds(1));
        AddReadingToDatabase(subTopic + "/RealPower", real);
        if (subTopic.contains("Whole_House"))
        {
            Reading voltage = circuitMap.get(circuit).getAverageBetween(Voltage.VOLTS, Instant.now().minusSeconds(2), Instant.now().minusSeconds(1));
            AddReadingToDatabase(subTopic + "/Voltage", voltage);
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
        String subTopic;
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
                    updateCircuitInDatabase(circuit);
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
        shutdownDatabaseProcessing();
        System.out.println("Data Processing Interrupted, exiting");
        System.exit(0);
    }
}
