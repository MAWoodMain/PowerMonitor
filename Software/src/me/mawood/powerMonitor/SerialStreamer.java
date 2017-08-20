package me.mawood.powerMonitor;

import com.pi4j.io.serial.*;

import java.io.IOException;
import java.util.Arrays;

class SerialStreamer
{
    private Serial serial;
    SerialStreamer(SerialDataEventListener listener) throws IOException, InterruptedException
    {
        SerialConfig config = new SerialConfig();
        config.device(SerialPort.getDefaultPort())
                .baud(Baud._9600)
                .dataBits(DataBits._8)
                .stopBits(StopBits._1)
                .parity(Parity.NONE)
                .flowControl(FlowControl.NONE);

        serial = SerialFactory.createInstance();

        serial.addListener(listener);

        serial.open(config);
        Thread.sleep(10000);
    }

}
