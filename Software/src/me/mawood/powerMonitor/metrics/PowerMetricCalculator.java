package me.mawood.powerMonitor.metrics;

import me.mawood.powerMonitor.packets.monitors.CurrentMonitor;
import me.mawood.powerMonitor.packets.monitors.RealPowerMonitor;
import me.mawood.powerMonitor.packets.monitors.VoltageMonitor;
import me.mawood.powerMonitor.metrics.units.Current;
import me.mawood.powerMonitor.metrics.units.Power;
import me.mawood.powerMonitor.metrics.units.Unit;
import me.mawood.powerMonitor.metrics.units.Voltage;

import javax.naming.OperationNotSupportedException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PowerMetricCalculator
{
    private final VoltageMonitor voltageMonitor;
    private final CurrentMonitor currentMonitor;
    private final RealPowerMonitor powerMonitor;

    public PowerMetricCalculator(VoltageMonitor voltageMonitor, CurrentMonitor currentMonitor, RealPowerMonitor powerMonitor)
    {
        this.voltageMonitor = voltageMonitor;
        this.currentMonitor = currentMonitor;
        this.powerMonitor = powerMonitor;
    }

    private Reading getLatestMetric(Unit metricType) throws OperationNotSupportedException
    {
        switch (metricType.getType())
        {
            case POWER:
                return getLatestPowerMetric((Power) metricType);
            case CURRENT:
                return getLatestCurrentMetric((Current) metricType);
            case VOLTAGE:
                return getLatestVoltageMetric((Voltage) metricType);
            default:
                throw new OperationNotSupportedException();
        }

    }

    private List<Reading> getMetricsBetween(Unit metricType, Instant startTime, Instant endTime) throws OperationNotSupportedException
    {
        switch (metricType.getType())
        {
            case POWER:
                return getPowerMetricsBetween((Power) metricType, startTime, endTime);
            case CURRENT:
                return getCurrentMetricsBetween((Current) metricType, startTime, endTime);
            case VOLTAGE:
                return getVoltageMetricsBetween((Voltage) metricType, startTime, endTime);
            default:
                throw new OperationNotSupportedException();
        }

    }

    public Reading getAverageBetween(Unit metricType, Instant startTime, Instant endTime) throws OperationNotSupportedException, InvalidDataException
    {
        List<Reading> data = getMetricsBetween(metricType,startTime,endTime);
        if(data.size() == 0) throw new InvalidDataException("No data available for given time period and metric");
        return new Reading(data.stream().mapToDouble(Reading::getValue).average().getAsDouble(),data.get(data.size()-1).getTimestamp(),metricType);
    }

    private Reading getLatestPowerMetric(Power metricType) throws OperationNotSupportedException
    {
        switch (metricType)
        {
            case WATTS:
                return powerMonitor.getLatestMetric();
            case KILOWATT:
                Reading watts = getLatestMetric(Power.WATTS);
                return new Reading(watts.getValue()/1000d,watts.getTimestamp(),metricType);
            case VA:
                Reading voltage = getLatestMetric(Voltage.VOLTS);
                Reading current = getLatestMetric(Current.AMPS);
                return new Reading(voltage.getValue()*current.getValue(),voltage.getTimestamp(), metricType);
            case VAR:
                Reading apparent = getLatestMetric(Power.VA);
                Reading real = getLatestMetric(Power.WATTS);
                return new Reading(Math.sqrt((apparent.getValue()*apparent.getValue())-(real.getValue()*real.getValue())),apparent.getTimestamp(), metricType);
            default:
                throw new OperationNotSupportedException();
        }
    }

    private List<Reading> getPowerMetricsBetween(Power metricType, Instant startTime, Instant endTime) throws OperationNotSupportedException
    {
        List<Reading> output = new ArrayList<>();
        switch (metricType)
        {
            case WATTS:
                return powerMonitor.getMetricsBetween(startTime,endTime);
            case KILOWATT:
                List<Reading> watts = getMetricsBetween(Power.WATTS, startTime,endTime);
                for(Reading m:watts)
                    output.add(new Reading(m.getValue()/1000d,m.getTimestamp(),metricType));
                return output;
            case VA:
                List<Reading> voltage = getMetricsBetween(Voltage.VOLTS,startTime,endTime);
                List<Reading> current = getMetricsBetween(Current.AMPS,startTime,endTime);
                for (int i = 0; i < Math.min(voltage.size(), current.size()); i++)
                    output.add(new Reading(voltage.get(i).getValue()*current.get(i).getValue(),voltage.get(i).getTimestamp(), metricType));
                return output;
            case VAR:
                List<Reading> apparent = getMetricsBetween(Power.VA,startTime,endTime);
                List<Reading> real = getMetricsBetween(Power.WATTS,startTime,endTime);
                for (int i = 0; i < Math.min(apparent.size(), real.size()); i++)
                    output.add(new Reading(Math.sqrt((apparent.get(i).getValue()*apparent.get(i).getValue())
                            - (real.get(i).getValue()*real.get(i).getValue())),apparent.get(i).getTimestamp(), metricType));
                return output;
            default:
                throw new OperationNotSupportedException();
        }
    }

    private Reading getLatestVoltageMetric(Voltage metricType) throws OperationNotSupportedException
    {
        switch (metricType)
        {
            case VOLTS:
                return voltageMonitor.getLatestMetric();
            case MILLI_VOLTS:
                Reading voltage = getLatestMetric(Voltage.VOLTS);
                return new Reading(voltage.getValue()*1000d,voltage.getTimestamp(),metricType);
            default:
                throw new OperationNotSupportedException();
        }
    }

    private List<Reading> getVoltageMetricsBetween(Voltage metricType, Instant startTime, Instant endTime) throws OperationNotSupportedException
    {
        List<Reading> output = new ArrayList<>();
        switch (metricType)
        {
            case VOLTS:
                return voltageMonitor.getMetricsBetween(startTime, endTime);
            case MILLI_VOLTS:
                List<Reading> volts = getMetricsBetween(Voltage.VOLTS, startTime,endTime);
                for(Reading m:volts)
                    output.add(new Reading(m.getValue()*1000d,m.getTimestamp(),metricType));
                return output;
            default:
                throw new OperationNotSupportedException();
        }
    }

    private Reading getLatestCurrentMetric(Current metricType) throws OperationNotSupportedException
    {
        switch (metricType)
        {
            case AMPS:
                return currentMonitor.getLatestMetric();
            case MILLI_AMPS:
                Reading current = getLatestMetric(Current.AMPS);
                return new Reading(current.getValue()*1000d,current.getTimestamp(),metricType);

            default:
                throw new OperationNotSupportedException();
        }
    }

    private List<Reading> getCurrentMetricsBetween(Current metricType, Instant startTime, Instant endTime) throws OperationNotSupportedException
    {
        List<Reading> output = new ArrayList<>();
        switch (metricType)
        {
            case AMPS:
                return currentMonitor.getMetricsBetween(startTime, endTime);
            case MILLI_AMPS:
                List<Reading> amps = getMetricsBetween(Current.AMPS, startTime,endTime);
                for(Reading m:amps)
                    output.add(new Reading(m.getValue()*1000d,m.getTimestamp(),metricType));
                return output;
            default:
                throw new OperationNotSupportedException();
        }
    }
}
