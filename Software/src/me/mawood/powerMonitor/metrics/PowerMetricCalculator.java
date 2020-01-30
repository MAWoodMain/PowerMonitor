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

    private MetricReading getLatestMetric(Unit metricType) throws OperationNotSupportedException
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
/*
    private MetricReading getLatestMetricByDefn(MetricDefinition metricDefinition) throws OperationNotSupportedException
    {
        MetricReading voltage = voltageMonitor.getLatestMetric();
        MetricReading current  = currentMonitor.getLatestMetric();
        MetricReading real;
        MetricReading apparent;

        switch (metricDefinition)
        {
            case WATTS:
                return powerMonitor.getLatestMetric();
            case KILOWATT:
                MetricReading watts = getLatestMetric(Power.WATTS);
                return new MetricReading(watts.getValue()/metricDefinition.getFactor(),watts.getTimestamp(),metricDefinition.getMetricType());
            case VA:
                return new MetricReading(voltage.getValue()*current.getValue(),voltage.getTimestamp(),metricDefinition.getMetricType());
            case VAR:
                apparent = getLatestMetric(Power.VA);
                real = getLatestMetric(Power.WATTS);
                return new MetricReading(Math.sqrt((apparent.getValue()*apparent.getValue())-(real.getValue()*real.getValue())),apparent.getTimestamp(), metricDefinition.getMetricType());
            case VOLTS:
                return voltage;
            case MILLI_VOLTS:
                voltage = getLatestMetric(Voltage.VOLTS);
                return new MetricReading(voltage.getValue()/metricDefinition.getFactor(),voltage.getTimestamp(),metricDefinition.getMetricType());
            case AMPS:
                return current;
            case MILLI_AMPS:
                return new MetricReading(current.getValue()/metricDefinition.getFactor(),current.getTimestamp(),metricDefinition.getMetricType());
            case WATT_HOURS:
                break;
            case KILOWATT_HOURS:
                break;
           case POWERFACTOR:
               apparent = getLatestMetric(Power.VA);
               real = powerMonitor.getLatestMetric();
               return new MetricReading(real.getValue()/apparent.getValue(),current.getTimestamp(),metricDefinition.getMetricType());
                break;
        }
        return null;
    }
*/

    private List<MetricReading> getMetricsBetween(Unit metricDefinition, Instant startTime, Instant endTime) throws OperationNotSupportedException
    {
        switch (metricDefinition.getType())
        {
            case POWER:
                return getPowerMetricsBetween((Power) metricDefinition, startTime, endTime);
            case CURRENT:
                return getCurrentMetricsBetween((Current) metricDefinition, startTime, endTime);
            case VOLTAGE:
                return getVoltageMetricsBetween((Voltage) metricDefinition, startTime, endTime);
            default:
                throw new OperationNotSupportedException();
        }

    }

    public MetricReading getAverageBetween(Unit metricType, Instant startTime, Instant endTime) throws OperationNotSupportedException, InvalidDataException
    {
        List<MetricReading> data = getMetricsBetween(metricType,startTime,endTime);
        if(data.size() == 0) throw new InvalidDataException("No data available for given time period and metric");
        return new MetricReading(data.stream().mapToDouble(MetricReading::getValue).average().getAsDouble(),data.get(data.size()-1).getTimestamp(),metricType);
    }

    private MetricReading getLatestPowerMetric(Power metricType) throws OperationNotSupportedException
    {
        switch (metricType)
        {
            case WATTS:
                return powerMonitor.getLatestMetric();
            case KILOWATT:
                MetricReading watts = getLatestMetric(Power.WATTS);
                return new MetricReading(watts.getValue()/1000d,watts.getTimestamp(),metricType);
            case VA:
                MetricReading voltage = getLatestMetric(Voltage.VOLTS);
                MetricReading current = getLatestMetric(Current.AMPS);
                return new MetricReading(voltage.getValue()*current.getValue(),voltage.getTimestamp(), metricType);
            case VAR:
                MetricReading apparent = getLatestMetric(Power.VA);
                MetricReading real = getLatestMetric(Power.WATTS);
                return new MetricReading(Math.sqrt((apparent.getValue()*apparent.getValue())-(real.getValue()*real.getValue())),apparent.getTimestamp(), metricType);
            default:
                throw new OperationNotSupportedException();
        }
    }

    private List<MetricReading> getPowerMetricsBetween(Power metricType, Instant startTime, Instant endTime) throws OperationNotSupportedException
    {
        List<MetricReading> output = new ArrayList<>();
        switch (metricType)
        {
            case WATTS:
                return powerMonitor.getMetricsBetween(startTime,endTime);
            case KILOWATT:
                List<MetricReading> watts = getMetricsBetween(Power.WATTS, startTime,endTime);
                for(MetricReading m:watts)
                    output.add(new MetricReading(m.getValue()/1000d,m.getTimestamp(),metricType));
                return output;
            case VA:
                List<MetricReading> voltage = getMetricsBetween(Voltage.VOLTS,startTime,endTime);
                List<MetricReading> current = getMetricsBetween(Current.AMPS,startTime,endTime);
                for (int i = 0; i < Math.min(voltage.size(), current.size()); i++)
                    output.add(new MetricReading(voltage.get(i).getValue()*current.get(i).getValue(),voltage.get(i).getTimestamp(), metricType));
                return output;
            case VAR:
                List<MetricReading> apparent = getMetricsBetween(Power.VA,startTime,endTime);
                List<MetricReading> real = getMetricsBetween(Power.WATTS,startTime,endTime);
                for (int i = 0; i < Math.min(apparent.size(), real.size()); i++)
                    output.add(new MetricReading(Math.sqrt((apparent.get(i).getValue()*apparent.get(i).getValue())
                            - (real.get(i).getValue()*real.get(i).getValue())),apparent.get(i).getTimestamp(), metricType));
                return output;
            default:
                throw new OperationNotSupportedException();
        }
    }

    private MetricReading getLatestVoltageMetric(Voltage metricType) throws OperationNotSupportedException
    {
        switch (metricType)
        {
            case VOLTS:
                return voltageMonitor.getLatestMetric();
            case MILLI_VOLTS:
                MetricReading voltage = getLatestMetric(Voltage.VOLTS);
                return new MetricReading(voltage.getValue()*1000d,voltage.getTimestamp(),metricType);
            default:
                throw new OperationNotSupportedException();
        }
    }

    private List<MetricReading> getVoltageMetricsBetween(Voltage metricType, Instant startTime, Instant endTime) throws OperationNotSupportedException
    {
        List<MetricReading> output = new ArrayList<>();
        switch (metricType)
        {
            case VOLTS:
                return voltageMonitor.getMetricsBetween(startTime, endTime);
            case MILLI_VOLTS:
                List<MetricReading> volts = getMetricsBetween(Voltage.VOLTS, startTime,endTime);
                for(MetricReading m:volts)
                    output.add(new MetricReading(m.getValue()*1000d,m.getTimestamp(),metricType));
                return output;
            default:
                throw new OperationNotSupportedException();
        }
    }

    private MetricReading getLatestCurrentMetric(Current metricType) throws OperationNotSupportedException
    {
        switch (metricType)
        {
            case AMPS:
                return currentMonitor.getLatestMetric();
            case MILLI_AMPS:
                MetricReading current = getLatestMetric(Current.AMPS);
                return new MetricReading(current.getValue()*1000d,current.getTimestamp(),metricType);

            default:
                throw new OperationNotSupportedException();
        }
    }

    private List<MetricReading> getCurrentMetricsBetween(Current metricType, Instant startTime, Instant endTime) throws OperationNotSupportedException
    {
        List<MetricReading> output = new ArrayList<>();
        switch (metricType)
        {
            case AMPS:
                return currentMonitor.getMetricsBetween(startTime, endTime);
            case MILLI_AMPS:
                List<MetricReading> amps = getMetricsBetween(Current.AMPS, startTime,endTime);
                for(MetricReading m:amps)
                    output.add(new MetricReading(m.getValue()*1000d,m.getTimestamp(),metricType));
                return output;
            default:
                throw new OperationNotSupportedException();
        }
    }
}
