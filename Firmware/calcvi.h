#ifndef CALC_VI_H
#define CALC_VI_H
#include <stdbool.h>
#include <math.h>
#include "adc.h"

#define ADC_COUNTS 4096
#define PHASECAL 1.7
#define VCC 3.3

void calcVI(unsigned int crossings);

float getVrms(void);
float getIrms(unsigned int channelNo);
float getRealPower(unsigned int channelNo);


extern int startV,sampleV,sampleI[];

extern float lastFilteredV,offsetV,offsetI,filteredV,filteredI,phaseShiftedV;
extern double sumVSquared,instP,sumISquared[],sumP[];
extern bool lastVCross,checkVCross;

extern float Vrms,realPower[],Irms[];

#endif
