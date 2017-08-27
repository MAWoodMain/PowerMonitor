package me.mawood.powerMonitor.metrics;

import me.mawood.powerMonitor.metrics.monitors.CurrentMonitor;
import me.mawood.powerMonitor.metrics.monitors.RealPowerMonitor;
import me.mawood.powerMonitor.metrics.monitors.VoltageMonitor;
import me.mawood.powerMonitor.metrics.units.Current;
import me.mawood.powerMonitor.metrics.units.Power;
import me.mawood.powerMonitor.metrics.units.Unit;
import me.mawood.powerMonitor.metrics.units.Voltage;

import javax.naming.OperationNotSupportedException;

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

    private Metric<Power> getLatestPowerMetric(Power metricType) throws OperationNotSupportedException
    {
        switch (metricType)
        {
            case WATTS:
                return powerMonitor.getLatestMetric();
            case KILOWATT:
                Metric watts = getLatestMetric(Power.WATTS);
                return new Metric<>(watts.getValue()/1000d,watts.getTimestamp(),metricType);
            case VA:
                Metric voltage = getLatestMetric(Voltage.VOLTS);
                Metric current = getLatestMetric(Current.AMPS);
                return new Metric<>(voltage.getValue()/current.getValue(),voltage.getTimestamp(), metricType);
            case VAR:
                Metric apparent = getLatestMetric(Power.VA);
                Metric real = getLatestMetric(Power.WATTS);
                return new Metric<>(Math.sqrt((apparent.getValue()*apparent.getValue())-(real.getValue()*real.getValue())),apparent.getTimestamp(), metricType);
            default:
                throw new OperationNotSupportedException();
        }
    }

    private Metric<Voltage> getLatestVoltageMetric(Voltage metricType) throws OperationNotSupportedException
    {
        switch (metricType)
        {
            case VOLTS:
                return voltageMonitor.getLatestMetric();
            case MILLI_VOLTS:
                Metric voltage = getLatestMetric(Voltage.VOLTS);
                return new Metric<>(voltage.getValue()*1000d,voltage.getTimestamp(),metricType);
            default:
                throw new OperationNotSupportedException();
        }
    }

    private Metric<Current> getLatestCurrentMetric(Current metricType) throws OperationNotSupportedException
    {
        switch (metricType)
        {
            case AMPS:
                return currentMonitor.getLatestMetric();
            case MILLI_AMPS:
                Metric current = getLatestMetric(Current.AMPS);
                return new Metric<>(current.getValue()*1000d,current.getTimestamp(),metricType);

            default:
                throw new OperationNotSupportedException();
        }
    }
}
