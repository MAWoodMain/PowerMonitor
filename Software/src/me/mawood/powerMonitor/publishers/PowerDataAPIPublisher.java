package me.mawood.powerMonitor.publishers;

import me.mawood.data_api_client.accessors.DataTypeAccessor;
import me.mawood.data_api_client.accessors.DeviceAccessor;
import me.mawood.data_api_client.accessors.ReadingAccessor;
import me.mawood.data_api_client.objects.DataType;
import me.mawood.data_api_client.objects.Device;
import me.mawood.data_api_client.objects.Reading;
import me.mawood.powerMonitor.circuits.Circuit;
import me.mawood.powerMonitor.metrics.InvalidDataException;
import me.mawood.powerMonitor.metrics.MetricReading;
import me.mawood.powerMonitor.metrics.PowerMetricCalculator;
import me.mawood.powerMonitor.metrics.units.Power;
import me.mawood.powerMonitor.metrics.units.Voltage;

import javax.naming.OperationNotSupportedException;
import javax.ws.rs.BadRequestException;
import java.time.Instant;
import java.util.*;

public class PowerDataAPIPublisher extends Thread
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
    private static final String API_URL = "http://192.168.1.164/api/";
    private static final String VOLTAGE_DATA_TYPE = "Voltage";
    private static final String REAL_POWER_DATA_TYPE = "RealPower";
    private static final String APPARENT_POWER_DATA_TYPE = "ApparentPower";

    private long noMessagesSentOK;

    // run control variables
    private volatile boolean msgArrived;
    private final Map<Circuit, PowerMetricCalculator> circuitMap;
    private final ReadingAccessor readingAccessor;

    private Map<Circuit, Device> deviceMap = new HashMap<>();

    /**
     * PowerDataMQTTPublisher   Constructor
     */
    public PowerDataAPIPublisher(Map<Circuit, PowerMetricCalculator> circuitMap)
    {
        DeviceAccessor deviceAccessor = new DeviceAccessor(API_URL);
        DataTypeAccessor dataTypeAccessor = new DataTypeAccessor(API_URL);
        readingAccessor = new ReadingAccessor(API_URL);
        Collection<DataType> dataTypes = dataTypeAccessor.getDataTypes();
        //TODO wait for data service to be available
        if(dataTypes.isEmpty())
        {
            DataType dt = new DataType();
            dt.setName("Voltage");
            dt.setTag(VOLTAGE_DATA_TYPE);
            dt.setSymbol("V");
            dataTypes.add(dt);
            dataTypeAccessor.addDataType(dt);
            new DataType();
            dt.setName("Real Power");
            dt.setTag(REAL_POWER_DATA_TYPE);
            dt.setSymbol("W");
            dataTypes.add(dt);
            dataTypeAccessor.addDataType(dt);new DataType();
            dt.setName("Apparent Power");
            dt.setTag(APPARENT_POWER_DATA_TYPE);
            dt.setSymbol("VA");
            dataTypes.add(dt);
            dataTypeAccessor.addDataType(dt);
        }
        this.circuitMap = circuitMap;
        Device device;
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
                //if we get an exception here give up
            }
        }
        noMessagesSentOK = 0;
        //connect to DB
        System.out.println("PowerDataAPIPublisher devices established");
    }

      private void AddReadingToDatabase(String deviceTag, String dataTypeTag, MetricReading reading)
    {
        //System.out.printf("Adding %S reading to %s\n",dataTypeTag,deviceTag);
        readingAccessor.addReading(deviceTag,dataTypeTag,new Reading[]{new Reading(reading.getValue(),reading.getTimestamp().toEpochMilli())});
    }

    private void updateCircuitInDatabase(Circuit circuit) throws InvalidDataException, OperationNotSupportedException
    {
        MetricReading apparent = circuitMap.get(circuit).getAverageBetween(Power.VA, Instant.now().minusSeconds(2), Instant.now().minusSeconds(1));
        AddReadingToDatabase(deviceMap.get(circuit).getTag(),APPARENT_POWER_DATA_TYPE, apparent);
        MetricReading real = circuitMap.get(circuit).getAverageBetween(Power.WATTS, Instant.now().minusSeconds(2), Instant.now().minusSeconds(1));
        AddReadingToDatabase(deviceMap.get(circuit).getTag(),REAL_POWER_DATA_TYPE, real);
        if (circuit.getTag().equalsIgnoreCase("Whole_House"))
        {
            MetricReading voltage = circuitMap.get(circuit).getAverageBetween(Voltage.VOLTS, Instant.now().minusSeconds(2), Instant.now().minusSeconds(1));
            AddReadingToDatabase(deviceMap.get(circuit).getTag(),VOLTAGE_DATA_TYPE, voltage);
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
        System.out.println("Data Processing Interrupted, exiting");
        System.exit(0);
    }
}
