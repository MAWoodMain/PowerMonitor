package org.ladbury.powerMonitor.currentClamps;

public class Clamps
{
    public final int NUMBER_CLAMP_TYPES = 10;
    private final Clamp[] clamps = new Clamp[NUMBER_CLAMP_TYPES];

    public Clamps()
    {
        clamps[0] = new Clamp("SCT013_5A1V",5,372.0, 1800, 1.0913432471, -0.0069045);
        clamps[1] = new Clamp("SCT013_10A1V",10,186.0, 1800, 1, 0);
        clamps[2] = new Clamp("SCT013_15A1V",15,124.0, 1800, 1, 0);
        clamps[3] = new Clamp("SCT013_20A1V",20,93.0, 1800, 1, 0);
        clamps[4] = new Clamp("SCT013_25A1V",25,74.4, 1800, 1, 0);
        clamps[5] = new Clamp("SCT013_30A1V",30,62.0, 1800, 1, 0);
        clamps[6] = new Clamp("SCT013_50A1V",50,37.2, 1800, 1, 0);
        clamps[7] = new Clamp("SCT013_60A1V",60,31.0, 1800, 1, 0);
        clamps[8] = new Clamp("SCT013_70A1V",70,26.6, 1800, 1, 0);
        clamps[9] = new Clamp("SCT013_100A1V",100,22.0, 1800, 1, 0);
    }

    private int getClampIndex(String clampName)
    {
        if (clampName == null) return -1;
        for (int i = 0; i <NUMBER_CLAMP_TYPES; i++)
        {
            if(clamps[i] != null)
                if (clamps[i].getClampName()!= null)
                    if (clampName.equalsIgnoreCase(clamps[i].getClampName())) return i;
        }
        return -1;
    }

    public Clamp getClamp(String clampName)
    {
        int index = getClampIndex(clampName);
        if (index < 0) return null;
        return clamps[index];
    }

    public boolean setClamp( String clampName, Clamp clamp )
    {
        int index = getClampIndex(clampName);
        if (index <0) return false;
        clamps[index] = clamp;
        return true;
    }
}
