#include "calcvi.h"

const int ADC_COUNTS = 4096;
const float PHASECAL = 1.7;

float realPower,apparentPower,powerFactor,Vrms,Irms;

int sampleV,sampleI;

float VCAL,ICAL;

float lastFilteredV,filteredV,filteredI,offsetV,offsetI;

float phaseShiftedV;

double sumV,sumI,instP,sumP;

int startV;

bool lastVCross,checkVCross;

// Modified version of a method from https://github.com/openenergymonitor/EmonLib/blob/master/EmonLib.cpp
// Calculator credits to the openenergymonitor project (https://github.com/openenergymonitor)
void calcVI(const char vPin, const char iPin, const unsigned int crossings)
{
	const float SupplyVoltage = 3.3;
  unsigned int crossCount = 0;
  unsigned int numberOfSamples = 0;
	double V_RATIO,I_RATIO;
	
	float VCAL = 210.0;
	float ICAL = 1800/372; // 1800 turns / burden resistor valuer for 5A clamp 372 Ohms
	
  //Reset accumulators
  sumV = 0;
  sumI = 0;
  sumP = 0;	
	do
	{
    startV = readChannel(vPin);
		// keep trying until voltage is within 5% of a crossing point (offset zero)
	} while (!((startV<(ADC_COUNTS*0.55))&&(startV>(ADC_COUNTS*0.45)))); 
	
	while(crossCount < crossings)
	{
		
    numberOfSamples++;                       //Count number of times looped.
    lastFilteredV = filteredV;               //Used for delay/phase compensation

    //-----------------------------------------------------------------------------
    // A) Read in raw voltage and current samples
    //-----------------------------------------------------------------------------
    sampleV = readChannel(vPin);                 //Read in raw voltage signal
    sampleI = readChannel(iPin);                 //Read in raw current signal

    //-----------------------------------------------------------------------------
    // B) Apply digital low pass filters to extract the 2.5 V or 1.65 V dc offset,
    //     then subtract this - signal is now centred on 0 counts.
    //-----------------------------------------------------------------------------
    offsetV = offsetV + ((sampleV-offsetV)/1024);
    filteredV = sampleV - offsetV;
    offsetI = offsetI + ((sampleI-offsetI)/1024);
    filteredI = sampleI - offsetI;

    //-----------------------------------------------------------------------------
    // C) Root-mean-square method voltage
    //-----------------------------------------------------------------------------
    //1) square voltage values
    sumV += filteredV * filteredV;

    //-----------------------------------------------------------------------------
    // D) Root-mean-square method current
    //-----------------------------------------------------------------------------
    //1) square current values
    sumI += filteredI * filteredI;

    //-----------------------------------------------------------------------------
    // E) Phase calibration
    //-----------------------------------------------------------------------------
    phaseShiftedV = lastFilteredV + PHASECAL * (filteredV - lastFilteredV);

    //-----------------------------------------------------------------------------
    // F) Instantaneous power calc
    //-----------------------------------------------------------------------------
    instP = phaseShiftedV * filteredI;          //Instantaneous Power
    sumP +=instP;                               //Sum

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

  V_RATIO = VCAL *(SupplyVoltage / ADC_COUNTS);
  Vrms = V_RATIO * sqrt(sumV / numberOfSamples);

  I_RATIO = ICAL *(SupplyVoltage / ADC_COUNTS);
  Irms = I_RATIO * sqrt(sumI / numberOfSamples);

  //Calculation power values
  realPower = V_RATIO * I_RATIO * sumP / numberOfSamples;
  apparentPower = Vrms * Irms;
  //powerFactor=realPower / apparentPower;
}

float getRealPower()
{
	return realPower;
}

float getApparentPower()
{
	return apparentPower;
}

float getVrms()
{
	return Vrms;
}

