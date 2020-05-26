package org.ladbury.powerMonitor.PMhealth;

import org.ladbury.powerMonitor.Main;
import java.time.Instant;

public class MemoryData
{
    String time;
    final String device;
    //final int channel;
    //final String circuitTag;
    MemoryReadings readings;

    MemoryData()
    {
        device = Main.getMqttHandler().getClientID();
        //channel = -1;
        //circuitTag = "";
        time = Instant.now().toString();
        readings = new MemoryReadings();
    }
}
