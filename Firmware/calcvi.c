#include "calcvi.h"

int startV, sampleV, sampleI[HARDWARE_CHANNEL_NUM];

float Vrms,realPower[HARDWARE_CHANNEL_NUM],Irms[HARDWARE_CHANNEL_NUM],lastOffsetV,offsetV,offsetI,vOffset,iOffset,phaseShiftedV;

double sumVSquared, instP, sumISquared[HARDWARE_CHANNEL_NUM], sumP[HARDWARE_CHANNEL_NUM];

bool lastVCross,checkVCross;
offsetV = ADC_COUNTS>>1;
offsetI = ADC_COUNTS>>1;

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
  //-------------------------------------------------------------------------------------------------------------------------
  // 1) Waits for the waveform to be close to 'zero' (mid-scale adc) part in sin curve.
  //-------------------------------------------------------------------------------------------------------------------------

	do
	{
		startV = readChannel(VOLTAGE_CHANNEL);
		// keep trying until voltage is within 5% of a crossing point (offset zero)
	} while (!((startV<(ADC_COUNTS*0.55))&&(startV>(ADC_COUNTS*0.45)))); 
  //-------------------------------------------------------------------------------------------------------------------------
  // 2) Main measurement loop
  //-------------------------------------------------------------------------------------------------------------------------

	while(crossCount < crossings)
	{
        numberOfSamples++;
        //-----------------------------------------------------------------------------
        // A) Read in raw voltage and current samples
        //-----------------------------------------------------------------------------

        // read voltage ADC
        sampleV = readChannel(VOLTAGE_CHANNEL);
        // read current ADCs
		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
			sampleI[i] = readChannel(CHANNELS[i]);

        lastOffsetV = offsetV; //is this initialised? should be lastFilteredV

        //-----------------------------------------------------------------------------
        // B) Apply digital low pass filters to extract the 2.5 V or 1.65 V dc offset,
        //     then subtract this - signal is now centred on 0 counts.
        //-----------------------------------------------------------------------------

        // Voltage part
        vOffset = vOffset + ((sampleV-vOffset)/1024);  //GJW variable names are different here
        offsetV = sampleV - vOffset; // answer should be called filteredV


        //-----------------------------------------------------------------------------
        // C) Root-mean-square method voltage
        //-----------------------------------------------------------------------------

        sumVSquared += offsetV * offsetV;

        //-----------------------------------------------------------------------------
        // E) Phase calibration
        //-----------------------------------------------------------------------------
        phaseShiftedV = lastOffsetV + PHASECAL * (offsetV - lastOffsetV); //lastoffsetV should be lastFilteredV
		
		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
		{
            //-----------------------------------------------------------------------------
            // B) Apply digital low pass filters to extract the 2.5 V or 1.65 V dc offset,
            //     then subtract this - signal is now centred on 0 counts.
            //-----------------------------------------------------------------------------

            // Current part
			iOffset = iOffset + ((sampleI[i]-iOffset)/1024);
			offsetI = sampleI[i] - iOffset; // answer should be called filteredI

			//-----------------------------------------------------------------------------
            // D) Root-mean-square method current
            //-----------------------------------------------------------------------------
			sumISquared[i] += offsetI * offsetI;

            //-----------------------------------------------------------------------------
            // F) Instantaneous power calc
            //-----------------------------------------------------------------------------
			instP = phaseShiftedV * offsetI; //offsetI should be called filteredI
			sumP[i] +=instP;
		}
        //-----------------------------------------------------------------------------
        // G) Find the number of times the voltage has crossed the initial voltage
        //    - every 2 crosses we will have sampled 1 wavelength
        //    - so this method allows us to sample an integer number of half wavelengths which increases accuracy
        //-----------------------------------------------------------------------------
        lastVCross = checkVCross;
		checkVCross = sampleV > startV;
        if (numberOfSamples==1) lastVCross = checkVCross;
        if (lastVCross != checkVCross) crossCount++;
    }
    //-------------------------------------------------------------------------------------------------------------------------
    // 3) Post loop calculations
    //-------------------------------------------------------------------------------------------------------------------------
    //Calculation of the root of the mean of the voltage and current squared (rms)
    //Calibration coefficients applied.

    //GJW stuff missing here
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

