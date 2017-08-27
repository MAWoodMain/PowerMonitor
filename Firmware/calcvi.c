#include "calcvi.h"

int startV, sampleV, sampleI[HARDWARE_CHANNEL_NUM];

float Vrms,realPower[HARDWARE_CHANNEL_NUM],Irms[HARDWARE_CHANNEL_NUM],lastOffsetV,offsetV,offsetI,vOffset,iOffset,phaseShiftedV;

double sumVSquared, instP, sumISquared[HARDWARE_CHANNEL_NUM], sumP[HARDWARE_CHANNEL_NUM];

bool lastVCross,checkVCross;

// Modified version of a method from https://github.com/openenergymonitor/EmonLib/blob/master/EmonLib.cpp
// Calculator credits to the openenergymonitor project (https://github.com/openenergymonitor)
// Current clamp and AC/AC voltage source scaling and calibration is done outside of the firmware so that
// different external devices can be used.
void calcVI(const unsigned int crossings)
{
    unsigned int crossCount = 0;
    unsigned int numberOfSamples = 0;
    double VoltsPerCount;
    int i;
	
    //Reset accumulators
    sumVSquared = 0;
	for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
	{
		sumISquared[i] = 0;
		sumP[i] = 0;
	}
	
	do
	{
		startV = readChannel(VOLTAGE_CHANNEL);
		// keep trying until voltage is within 5% of a crossing point (offset zero)
	} while (!((startV<(ADC_COUNTS*0.55))&&(startV>(ADC_COUNTS*0.45)))); 
	
	while(crossCount < crossings)
	{
        numberOfSamples++;

        // read voltage ADC
        sampleV = readChannel(VOLTAGE_CHANNEL);
        // read current ADCs
		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
			sampleI[i] = readChannel(CHANNELS[i]);

        lastOffsetV = offsetV;
        // low pass filter to move sample voltage to +/- 1.65v scale rather than 0-3.3v scale
        vOffset = vOffset + ((sampleV-vOffset)/1024);
        offsetV = sampleV - vOffset;

        sumVSquared += offsetV * offsetV;

        phaseShiftedV = lastOffsetV + PHASECAL * (offsetV - lastOffsetV); //??????
		
		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
		{
            // low pass filter to move sample voltage to +/- 1.65v scale rather than 0-3.3v scale
			iOffset = iOffset + ((sampleI[i]-iOffset)/1024);
			offsetI = sampleI[i] - iOffset;
			sumISquared[i] += offsetI * offsetI;
			
			instP = phaseShiftedV * offsetI; //??????
			sumP[i] +=instP;
		}
		// check for a voltage zero crossing i.e completion of a half wave and increment cross count if so
        lastVCross = checkVCross;
		checkVCross = sampleV > startV;
        if (numberOfSamples==1) lastVCross = checkVCross;
        if (lastVCross != checkVCross) crossCount++;
    }
    // post loop calculations
	VoltsPerCount = VCC / ADC_COUNTS; //constant 3.3/4096
    Vrms = VoltsPerCount * sqrt(sumVSquared / numberOfSamples);

	for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
	{
		Irms[i] = VoltsPerCount * sqrt(sumISquared[i] / numberOfSamples);
		// VoltsPerCount applies to current and voltage in power as P=VI
		realPower[i] = VoltsPerCount * VoltsPerCount * sumP[i] / numberOfSamples;
	}
}

float getRealPower(unsigned int channelNo)
{
	return realPower[channelNo];
}

float getIrms(unsigned int channelNo)
{
	return Irms[channelNo];
}

float getVrms()
{
	return Vrms;
}

