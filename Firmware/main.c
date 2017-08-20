#include "stm8l051f3.h"
#include "uart.h"
#include "rtc.h"
#include "adc.h"
#include "calcvi.h"

void setup(void);
void loop(void);


main()
{
	setup();
	do {
		loop();
	} while(1);

}

void setup()
{
	double d = 7.5;
	
	CLK_CKDIVR = 0x00;
	
	UART_INIT();
	sendString("UART Initialised");
	RTC_INIT();
	sendString("RTC Initialised");
	ADC_INIT();
	sendString("ADC Initialised");

	sendChar(0x00);
	sendDouble(d);
	sendChar(0x00);
}

void loop()
{
	unsigned long i = 0;
	unsigned int adcValue = 0;
	adcValue = readChannel(VOLTAGE_CHANNEL);
	sendChar('V');
	sendChar((char)(adcValue >> 8));
	sendChar((char)adcValue);
	while(i<HARDWARE_CHANNEL_NUM)
	{
		calcVI(VOLTAGE_CHANNEL, CHANNELS[i], 30);
		sendChar(i);
		sendDouble(getApparentPower());
		sendDouble(getRealPower());
		i++;
	}
	i = 0;
	while(i < 47456) i++;
	while(i > 0) i--;
}
