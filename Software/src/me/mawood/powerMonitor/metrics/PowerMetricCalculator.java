package me.mawood.powerMonitor.metrics;

import me.mawood.powerMonitor.packets.monitors.CurrentMonitor;
import me.mawood.powerMonitor.packets.monitors.RealPowerMonitor;
import me.mawood.powerMonitor.packets.monitors.VoltageMonitor;

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


    public MetricReading getLatestMetric(MetricDefinition metricDefinition) throws OperationNotSupportedException
    {
        MetricReading voltage = voltageMonitor.getLatestMetric();
        MetricReading current  = currentMonitor.getLatestMetric();
        MetricReading real = powerMonitor.getLatestMetric();
        MetricReading apparent;

        switch (metricDefinition)
        {
            case WATTS:
                return real;
            case KILOWATT:
                return new MetricReading(real.getValue()/metricDefinition.getFactor(),real.getTimestamp(),metricDefinition);
            case VA:
                return new MetricReading(voltage.getValue()*current.getValue(),voltage.getTimestamp(),metricDefinition);
            case VAR:
                apparent = getLatestMetric(MetricDefinition.VA);
                return new MetricReading(Math.sqrt((apparent.getValue()*apparent.getValue())-(real.getValue()*real.getValue())),apparent.getTimestamp(), metricDefinition);
            case VOLTS:
                return voltage;
            case MILLI_VOLTS:
                 return new MetricReading(voltage.getValue()/metricDefinition.getFactor(),voltage.getTimestamp(),metricDefinition);
            case AMPS:
                return current;
            case MILLI_AMPS:
                return new MetricReading(current.getValue()/metricDefinition.getFactor(),current.getTimestamp(),metricDefinition);
            case WATT_HOURS:
                throw new OperationNotSupportedException() ;
            case KILOWATT_HOURS:
                throw new OperationNotSupportedException() ;
           case POWERFACTOR:
               apparent = getLatestMetric(MetricDefinition.VA);
               return new MetricReading(real.getValue()/apparent.getValue(),current.getTimestamp(),metricDefinition);
        }
        return null;
    }


    private List<MetricReading> getMetricsBetween(MetricDefinition metricDefinition, Instant startTime, Instant endTime) throws OperationNotSupportedException
    {
        List<MetricReading> output = new ArrayList<>();
        switch (metricDefinition)
        {
            case WATTS:
                return powerMonitor.getMetricsBetween(startTime,endTime);
            case KILOWATT:
                List<MetricReading> watts = getMetricsBetween(MetricDefinition.WATTS, startTime,endTime);
                for(MetricReading m:watts)
                    output.add(new MetricReading(m.getValue()/metricDefinition.getFactor(),m.getTimestamp(),metricDefinition));
                return output;
            case VA:
                List<MetricReading> voltage = getMetricsBetween(MetricDefinition.VOLTS,startTime,endTime);
                List<MetricReading> current = getMetricsBetween(MetricDefinition.AMPS,startTime,endTime);
                for (int i = 0; i < Math.min(voltage.size(), current.size()); i++)
                    output.add(new MetricReading(voltage.get(i).getValue()*current.get(i).getValue(),voltage.get(i).getTimestamp(), metricDefinition));
                return output;
            case VAR:
                List<MetricReading> apparent = getMetricsBetween(MetricDefinition.VA,startTime,endTime);
                List<MetricReading> real = getMetricsBetween(MetricDefinition.WATTS,startTime,endTime);
                for (int i = 0; i < Math.min(apparent.size(), real.size()); i++)
                    output.add(new MetricReading(Math.sqrt((apparent.get(i).getValue()*apparent.get(i).getValue())
                            - (real.get(i).getValue()*real.get(i).getValue())),apparent.get(i).getTimestamp(), metricDefinition));
                return output;
            case AMPS:
                return currentMonitor.getMetricsBetween(startTime, endTime);
            case MILLI_AMPS:
                List<MetricReading> amps = getMetricsBetween(MetricDefinition.AMPS, startTime,endTime);
                for(MetricReading m:amps)
                    output.add(new MetricReading(m.getValue()/metricDefinition.getFactor(),m.getTimestamp(),metricDefinition));
                return output;
            case VOLTS:
                return voltageMonitor.getMetricsBetween(startTime, endTime);
            case MILLI_VOLTS:
                List<MetricReading> volts = getMetricsBetween(MetricDefinition.VOLTS, startTime,endTime);
                for(MetricReading m:volts)
                    output.add(new MetricReading(m.getValue()/metricDefinition.getFactor(),m.getTimestamp(),metricDefinition));
                return output;
            case WATT_HOURS:
                throw new OperationNotSupportedException() ;
            case KILOWATT_HOURS:
                throw new OperationNotSupportedException() ;
            case POWERFACTOR:
                List<MetricReading> a = getMetricsBetween(MetricDefinition.VA,startTime,endTime);
                List<MetricReading> r = getMetricsBetween(MetricDefinition.WATTS,startTime,endTime);
                for(int i=0;i<a.size();i++)
                {
                    output.add(new MetricReading(r.get(i).getValue()/a.get(i).getValue(),r.get(i).getTimestamp(),metricDefinition));
                }

            default:
                throw new OperationNotSupportedException();
        }
    }

    public MetricReading getAverageBetween(MetricDefinition metricType, Instant startTime, Instant endTime) throws OperationNotSupportedException, InvalidDataException
    {
        List<MetricReading> data = getMetricsBetween(metricType,startTime,endTime);
        if(data.size() == 0) throw new InvalidDataException("No data available for given time period and metric");
        return new MetricReading(data.stream().mapToDouble(MetricReading::getValue).average().getAsDouble(),data.get(data.size()-1).getTimestamp(),metricType);
    }
}
