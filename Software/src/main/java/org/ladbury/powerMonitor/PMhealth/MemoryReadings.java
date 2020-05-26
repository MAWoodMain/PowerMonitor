package org.ladbury.powerMonitor.PMhealth;

public class MemoryReadings
{
    private long totalMemory;
    private long freeMemory;
    private long usedMemory;

    MemoryReadings()
    {
        totalMemory = Runtime.getRuntime().totalMemory();
        freeMemory = Runtime.getRuntime().freeMemory();
        usedMemory = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
    }
    public MemoryReadings getMemoryReadings()
    {
        totalMemory = Runtime.getRuntime().totalMemory();
        freeMemory = Runtime.getRuntime().freeMemory();
        usedMemory = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        return this;
    }
}
