package me.mawood.powerMonitor.metrics;

import me.mawood.powerMonitor.metrics.monitors.CurrentMonitor;
import me.mawood.powerMonitor.metrics.monitors.RealPowerMonitor;
import me.mawood.powerMonitor.metrics.monitors.VoltageMonitor;
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

    public Metric getLatestMetric(Unit metricType) throws OperationNotSupportedException
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

    public List<Metric> getMetricsBetween(Unit metricType, Instant startTime, Instant endTime) throws OperationNotSupportedException
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

    public Metric getAverageBetween(Unit metricType, Instant startTime, Instant endTime) throws OperationNotSupportedException, InvalidDataException
    {
        List<Metric> data = getMetricsBetween(metricType,startTime,endTime);
        if(data.size() == 0) throw new InvalidDataException("No data available for given time period and metric");
        return new Metric(data.stream().mapToDouble(Metric::getValue).average().getAsDouble(),data.get(data.size()-1).getTimestamp(),metricType);
    }

    private Metric getLatestPowerMetric(Power metricType) throws OperationNotSupportedException
    {
        switch (metricType)
        {
            case WATTS:
                return powerMonitor.getLatestMetric();
            case KILOWATT:
                Metric watts = getLatestMetric(Power.WATTS);
                return new Metric(watts.getValue()/1000d,watts.getTimestamp(),metricType);
            case VA:
                Metric voltage = getLatestMetric(Voltage.VOLTS);
                Metric current = getLatestMetric(Current.AMPS);
                return new Metric(voltage.getValue()/current.getValue(),voltage.getTimestamp(), metricType);
            case VAR:
                Metric apparent = getLatestMetric(Power.VA);
                Metric real = getLatestMetric(Power.WATTS);
                return new Metric(Math.sqrt((apparent.getValue()*apparent.getValue())-(real.getValue()*real.getValue())),apparent.getTimestamp(), metricType);
            default:
                throw new OperationNotSupportedException();
        }
    }

    private List<Metric> getPowerMetricsBetween(Power metricType, Instant startTime, Instant endTime) throws OperationNotSupportedException
    {
        List<Metric> output = new ArrayList<>();
        switch (metricType)
        {
            case WATTS:
                return powerMonitor.getMetricsBetween(startTime,endTime);
            case KILOWATT:
                List<Metric> watts = getMetricsBetween(Power.WATTS, startTime,endTime);
                for(Metric m:watts)
                    output.add(new Metric(m.getValue()/1000d,m.getTimestamp(),metricType));
                return output;
            case VA:
                List<Metric> voltage = getMetricsBetween(Voltage.VOLTS,startTime,endTime);
                List<Metric> current = getMetricsBetween(Current.AMPS,startTime,endTime);
                for (int i = 0; i < Math.min(voltage.size(), current.size()); i++)
                    output.add(new Metric(voltage.get(i).getValue()/current.get(i).getValue(),voltage.get(i).getTimestamp(), metricType));
                return output;
            case VAR:
                List<Metric> apparent = getMetricsBetween(Power.VA,startTime,endTime);
                List<Metric> real = getMetricsBetween(Power.WATTS,startTime,endTime);
                for (int i = 0; i < Math.min(apparent.size(), real.size()); i++)
                    output.add(new Metric(Math.sqrt((apparent.get(i).getValue()*apparent.get(i).getValue())
                            - (real.get(i).getValue()*real.get(i).getValue())),apparent.get(i).getTimestamp(), metricType));
                return output;
            default:
                throw new OperationNotSupportedException();
        }
    }

    private Metric getLatestVoltageMetric(Voltage metricType) throws OperationNotSupportedException
    {
        switch (metricType)
        {
            case VOLTS:
                return voltageMonitor.getLatestMetric();
            case MILLI_VOLTS:
                Metric voltage = getLatestMetric(Voltage.VOLTS);
                return new Metric(voltage.getValue()*1000d,voltage.getTimestamp(),metricType);
            default:
                throw new OperationNotSupportedException();
        }
    }

    private List<Metric> getVoltageMetricsBetween(Voltage metricType, Instant startTime, Instant endTime) throws OperationNotSupportedException
    {
        List<Metric> output = new ArrayList<>();
        switch (metricType)
        {
            case VOLTS:
                return voltageMonitor.getMetricsBetween(startTime, endTime);
            case MILLI_VOLTS:
                List<Metric> volts = getMetricsBetween(Voltage.VOLTS, startTime,endTime);
                for(Metric m:volts)
                    output.add(new Metric(m.getValue()*1000d,m.getTimestamp(),metricType));
                return output;
            default:
                throw new OperationNotSupportedException();
        }
    }

    private Metric getLatestCurrentMetric(Current metricType) throws OperationNotSupportedException
    {
        switch (metricType)
        {
            case AMPS:
                return currentMonitor.getLatestMetric();
            case MILLI_AMPS:
                Metric current = getLatestMetric(Current.AMPS);
                return new Metric(current.getValue()*1000d,current.getTimestamp(),metricType);

            default:
                throw new OperationNotSupportedException();
        }
    }

    private List<Metric> getCurrentMetricsBetween(Current metricType, Instant startTime, Instant endTime) throws OperationNotSupportedException
    {
        List<Metric> output = new ArrayList<>();
        switch (metricType)
        {
            case AMPS:
                return currentMonitor.getMetricsBetween(startTime, endTime);
            case MILLI_AMPS:
                List<Metric> amps = getMetricsBetween(Current.AMPS, startTime,endTime);
                for(Metric m:amps)
                    output.add(new Metric(m.getValue()*1000d,m.getTimestamp(),metricType));
                return output;
            default:
                throw new OperationNotSupportedException();
        }
    }
}
