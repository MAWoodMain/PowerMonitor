#include "calcvi.h"

int startV, sampleV, sampleI[HARDWARE_CHANNEL_NUM];

float Vrms,realPower[HARDWARE_CHANNEL_NUM],Irms[HARDWARE_CHANNEL_NUM],lastFilteredV,filteredV,filteredI,offsetV,offsetI,phaseShiftedV;

double sumV, instP, sumI[HARDWARE_CHANNEL_NUM], sumP[HARDWARE_CHANNEL_NUM];

bool lastVCross,checkVCross;

// Modified version of a method from https://github.com/openenergymonitor/EmonLib/blob/master/EmonLib.cpp
// Calculator credits to the openenergymonitor project (https://github.com/openenergymonitor)
// Current clamp and AC/AC voltage source scaling and calibration is done outside of the firmware so that
// different external devices can be used.
void calcVI(const unsigned int crossings)
{
    unsigned int crossCount = 0;
    unsigned int numberOfSamples = 0;
    Double VoltsPerCount;
    int i;
	
    //Reset accumulators
    sumV = 0;
	for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
	{
		sumI[i] = 0;
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

        lastFilteredV = filteredV;
        // low pass filter to move sample voltage to +/- 1.65v scale rather than 0-3.3v scale
        offsetV = offsetV + ((sampleV-offsetV)/1024);
        filteredV = sampleV - offsetV;

        sumV += filteredV * filteredV;

        phaseShiftedV = lastFilteredV + PHASECAL * (filteredV - lastFilteredV);
		
		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
		{
            // low pass filter to move sample voltage to +/- 1.65v scale rather than 0-3.3v scale
			offsetI = offsetI + ((sampleI[i]-offsetI)/1024);
			filteredI = sampleI[i] - offsetI;
			sumI[i] += filteredI * filteredI;
			instP = phaseShiftedV * filteredI;
			sumP[i] +=instP;
		}
		// check for a voltage zero crossing i.e completion of a half wave and increment cross count if so
        lastVCross = checkVCross;
		checkVCross = sampleV > startV;
        if (numberOfSamples==1) lastVCross = checkVCross;
        if (lastVCross != checkVCross) crossCount++;
    }
    // post loop calculations
	VoltsPerCount = VCC / ADC_COUNTS;
    Vrms = VoltsPerCount * sqrt(sumV / numberOfSamples);

	for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
	{
		Irms[i] = VoltsPerCount * sqrt(sumI[i] / numberOfSamples);
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

