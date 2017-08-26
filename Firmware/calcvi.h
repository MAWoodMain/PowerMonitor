#ifndef CALC_VI_H
#define CALC_VI_H
#include <stdbool.h>
#include <math.h>
#include "adc.h"

extern const int ADC_COUNTS;
extern const float PHASECAL;

void calcVI(unsigned int crossings);

float getVrms(void);
float getIrms(unsigned int channelNo);
float getRealPower(unsigned int channelNo);

extern float Vrms;
extern float realPower[];
extern float Irms[];

extern int sampleV;
extern int sampleI[];

extern float lastFilteredV,filteredV,filteredI,offsetV,offsetI;

extern float phaseShiftedV;

extern double sumV,instP;
extern double sumI[];
extern double sumP[];

extern int startV;

extern bool lastVCross,checkVCross;

#endif