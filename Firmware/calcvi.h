#ifndef CALC_VI_H
#define CALC_VI_H
#include <stdbool.h>
#include <math.h>
#include "adc.h"

extern const int ADC_COUNTS;
extern const float PHASECAL;

void calcVI(char vPin, char iPin, unsigned int crossings);
float getApparentPower(void);
float getRealPower(void);
float getVrms(void);

extern float realPower,apparentPower,powerFactor,Vrms,Irms;

extern int sampleV,sampleI;

extern float VCAL,ICAL;

extern float lastFilteredV,filteredV,filteredI,offsetV,offsetI;

extern float phaseShiftedV;

extern double sumV,sumI,instP,sumP;

extern int startV;

extern bool lastVCross,checkVCross;

#endif