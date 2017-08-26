#ifndef __ADC_H
#define __ADC_H

#include "stm8l051f3.h"

#define HARDWARE_CHANNEL_NUM 9

extern unsigned char CHANNELS[HARDWARE_CHANNEL_NUM];
extern unsigned int CHANNEL_COUNT;
extern unsigned char VOLTAGE_CHANNEL;

void ADC_INIT(void);

int readChannel(int adcChannel);

#endif