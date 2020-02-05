package org.ladbury.powerMonitor.metrics;

import org.ladbury.powerMonitor.packets.monitors.CurrentMonitor;
import org.ladbury.powerMonitor.packets.monitors.RealPowerMonitor;
import org.ladbury.powerMonitor.packets.monitors.VoltageMonitor;

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


    public MetricReading getLatestMetric(Metric metric) throws OperationNotSupportedException
    {
        MetricReading voltage = voltageMonitor.getLatestMetric();
        MetricReading current  = currentMonitor.getLatestMetric();
        MetricReading real = powerMonitor.getLatestMetric();
        MetricReading apparent;

        switch (metric)
        {
            case WATTS:
                return real;
            case KILOWATT:
                return new MetricReading(real.getValue()/ metric.getFactor(),real.getTimestamp(), metric);
            case VA:
                return new MetricReading(voltage.getValue()*current.getValue(),voltage.getTimestamp(), metric);
            case VAR:
                apparent = getLatestMetric(Metric.VA);
                return new MetricReading(Math.sqrt((apparent.getValue()*apparent.getValue())-(real.getValue()*real.getValue())),apparent.getTimestamp(), metric);
            case VOLTS:
                return voltage;
            case MILLI_VOLTS:
                 return new MetricReading(voltage.getValue()/ metric.getFactor(),voltage.getTimestamp(), metric);
            case AMPS:
                return current;
            case MILLI_AMPS:
                return new MetricReading(current.getValue()/ metric.getFactor(),current.getTimestamp(), metric);
            case POWERFACTOR:
               apparent = getLatestMetric(Metric.VA);
               return new MetricReading(real.getValue()/apparent.getValue(),current.getTimestamp(), metric);
            default:
                throw new OperationNotSupportedException();
        }
    }


    private List<MetricReading> getMetricsBetween(Metric metric, Instant startTime, Instant endTime) throws OperationNotSupportedException
    {
        List<MetricReading> output = new ArrayList<>();
        switch (metric)
        {
            case WATTS:
                return powerMonitor.getMetricsBetween(startTime,endTime);
            case KILOWATT:
                List<MetricReading> watts = getMetricsBetween(Metric.WATTS, startTime,endTime);
                for(MetricReading m:watts)
                    output.add(new MetricReading(m.getValue()/ metric.getFactor(),m.getTimestamp(), metric));
                return output;
            case VA:
                List<MetricReading> voltage = getMetricsBetween(Metric.VOLTS,startTime,endTime);
                List<MetricReading> current = getMetricsBetween(Metric.AMPS,startTime,endTime);
                for (int i = 0; i < Math.min(voltage.size(), current.size()); i++)
                    output.add(new MetricReading(voltage.get(i).getValue()*current.get(i).getValue(),voltage.get(i).getTimestamp(), metric));
                return output;
            case VAR:
                List<MetricReading> apparent = getMetricsBetween(Metric.VA,startTime,endTime);
                List<MetricReading> real = getMetricsBetween(Metric.WATTS,startTime,endTime);
                for (int i = 0; i < Math.min(apparent.size(), real.size()); i++)
                    output.add(new MetricReading(Math.sqrt((apparent.get(i).getValue()*apparent.get(i).getValue())
                            - (real.get(i).getValue()*real.get(i).getValue())),apparent.get(i).getTimestamp(), metric));
                return output;
            case AMPS:
                return currentMonitor.getMetricsBetween(startTime, endTime);
            case MILLI_AMPS:
                List<MetricReading> amps = getMetricsBetween(Metric.AMPS, startTime,endTime);
                for(MetricReading m:amps)
                    output.add(new MetricReading(m.getValue()/ metric.getFactor(),m.getTimestamp(), metric));
                return output;
            case VOLTS:
                return voltageMonitor.getMetricsBetween(startTime, endTime);
            case MILLI_VOLTS:
                List<MetricReading> volts = getMetricsBetween(Metric.VOLTS, startTime,endTime);
                for(MetricReading m:volts)
                    output.add(new MetricReading(m.getValue()/ metric.getFactor(),m.getTimestamp(), metric));
                return output;
            case POWERFACTOR:
                List<MetricReading> a = getMetricsBetween(Metric.VA,startTime,endTime);
                List<MetricReading> r = getMetricsBetween(Metric.WATTS,startTime,endTime);
                for(int i=0;i<a.size();i++)
                {
                    output.add(new MetricReading(r.get(i).getValue()/a.get(i).getValue(),r.get(i).getTimestamp(), metric));
                }
            default:
                throw new OperationNotSupportedException();
        }
    }

    public MetricReading getAverageBetween(Metric metricType, Instant startTime, Instant endTime) throws OperationNotSupportedException, InvalidDataException
    {
        List<MetricReading> data = getMetricsBetween(metricType,startTime,endTime);
        if(data.size() == 0) throw new InvalidDataException("No data available for given time period and metric");
        return new MetricReading(data.stream().mapToDouble(MetricReading::getValue).average().getAsDouble(),data.get(data.size()-1).getTimestamp(),metricType);
    }
}
