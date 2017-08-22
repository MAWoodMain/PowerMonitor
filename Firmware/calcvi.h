#ifndef CALC_VI_H
#define CALC_VI_H
#include <stdbool.h>
//#include <math.h>
#include "adc.h"

extern const int ADC_COUNTS;
extern const float PHASECAL;

void calcVI(char vPin, char iPin, unsigned int crossings);
double getApparentPower(void);
double getRealPower(void);
double getVrms(void);
double root(double n);

extern double realPower,apparentPower,powerFactor,Vrms,Irms;

extern int sampleV,sampleI;

extern float VCAL,ICAL;

extern double lastFilteredV,filteredV,filteredI,offsetV,offsetI;

extern double phaseShiftedV;

extern double sqV,sumV,sqI,sumI,instP,sumP;

extern int startV;

extern bool lastVCross,checkVCross;

#endif