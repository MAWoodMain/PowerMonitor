#include "calcvi.h"

const int ADC_COUNTS = 4096;
const float PHASECAL = 1.7;
float Vrms;
float realPower[HARDWARE_CHANNEL_NUM],Irms[HARDWARE_CHANNEL_NUM];
int sampleI[HARDWARE_CHANNEL_NUM];
int sampleV;


float lastFilteredV,filteredV,filteredI,offsetV,offsetI;

float phaseShiftedV;

double sumV, instP;
double sumI[HARDWARE_CHANNEL_NUM], sumP[HARDWARE_CHANNEL_NUM];

int startV;

bool lastVCross,checkVCross;

// Modified version of a method from https://github.com/openenergymonitor/EmonLib/blob/master/EmonLib.cpp
// Calculator credits to the openenergymonitor project (https://github.com/openenergymonitor)
void calcVI(const unsigned int crossings)
{
	const float SupplyVoltage = 3.3;
  unsigned int crossCount = 0;
  unsigned int numberOfSamples = 0;
	double RATIO;
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
		
    numberOfSamples++;                       //Count number of times looped.
    lastFilteredV = filteredV;               //Used for delay/phase compensation

    //-----------------------------------------------------------------------------
    // A) Read in raw voltage and current samples
    //-----------------------------------------------------------------------------
		
    sampleV = readChannel(VOLTAGE_CHANNEL);
		
		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
		{
			sampleI[i] = readChannel(CHANNELS[i]);
		}
		

    //-----------------------------------------------------------------------------
    // B) Apply digital low pass filters to extract the 2.5 V or 1.65 V dc offset,
    //     then subtract this - signal is now centred on 0 counts.
    //-----------------------------------------------------------------------------
    offsetV = offsetV + ((sampleV-offsetV)/1024);
    filteredV = sampleV - offsetV;
    sumV += filteredV * filteredV;
    phaseShiftedV = lastFilteredV + PHASECAL * (filteredV - lastFilteredV);
		
		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
		{
			offsetI = offsetI + ((sampleI[i]-offsetI)/1024);
			filteredI = sampleI[i] - offsetI;
			sumI[i] += filteredI * filteredI;
		
			instP = phaseShiftedV * filteredI;
			sumP[i] +=instP;
		}

    //-----------------------------------------------------------------------------
    // G) Find the number of times the voltage has crossed the initial voltage
    //    - every 2 crosses we will have sampled 1 wavelength
    //    - so this method allows us to sample an integer number of half wavelengths which increases accuracy
    //-----------------------------------------------------------------------------
    lastVCross = checkVCross;
		
		checkVCross = sampleV > startV;
		// /\ REPLACES \/
    //if (sampleV > startV) checkVCross = true;
    //                 else checkVCross = false;
		
    if (numberOfSamples==1) lastVCross = checkVCross;

    if (lastVCross != checkVCross) crossCount++;
	}
	
  //-------------------------------------------------------------------------------------------------------------------------
  // 3) Post loop calculations
  //-------------------------------------------------------------------------------------------------------------------------
  //Calculation of the root of the mean of the voltage and current squared (rms)
  //Calibration coefficients applied.


  //V_RATIO = SupplyVoltage / ADC_COUNTS;//VCAL *(SupplyVoltage / ADC_COUNTS);
  //I_RATIO = SupplyVoltage / ADC_COUNTS;//ICAL *(SupplyVoltage / ADC_COUNTS);
	RATIO = SupplyVoltage / ADC_COUNTS;
  Vrms = RATIO * sqrt(sumV / numberOfSamples);

	for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
	{
		Irms[i] = RATIO * sqrt(sumI[i] / numberOfSamples);
		realPower[i] = RATIO * RATIO * sumP[i] / numberOfSamples;
	}

  //Calculation power values
  //apparentPower = Vrms * Irms;
  //powerFactor=realPower / apparentPower;
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

