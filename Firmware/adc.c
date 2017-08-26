#include "adc.h"

unsigned char CHANNELS[] = {18,17,16,15,14,13,12,11,4};
unsigned char VOLTAGE_CHANNEL = 22;

void ADC_INIT()
{
	// Enable the ADC peripherial (PCKEN20)
	CLK_PCKENR2 |= 0x01;
	// Enable internal reference voltage
	//ADC1_TRIGR1 |= 0x10;
	// Configure the ADC
	//   - 12-bit resolution
	//   - single conversion
	//   - wake-up ADC
	ADC1_CR1 = 0x01;
	// 384 (max) adc clock cycles sampling time
	// Channels 1-24
	ADC1_CR2 = 0x02;
	// Channel 24 Vrefint and TS
	ADC1_CR3 = 0xE0;
}

int readChannel(int adcChannel)
{
	ADC1_CR3 &= ~0x1F;
	ADC1_CR3 |= (0x1F & adcChannel);
	
	ADC1_SQR2 = 0;
	ADC1_SQR3 = 0;
	ADC1_SQR4 = 0;
	
	if (adcChannel > 15)
	{
		ADC1_SQR2 = (0x01 << (adcChannel-16));
	}
	else if (adcChannel > 7)
	{
		ADC1_SQR3 = (0x01 << (adcChannel-8));
	}
	else
	{
		ADC1_SQR4 = (0x01 << adcChannel);
	}
	ADC1_CR1 |= 0x02;
	// Wait for EOC
	while(!(ADC1_SR & 0x01));
	return (ADC1_DRH << 8)|ADC1_DRL;
}