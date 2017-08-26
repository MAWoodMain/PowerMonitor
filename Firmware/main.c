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
	CLK_CKDIVR = 0x00;
	
	UART_INIT();
	sendString("UART Initialised");
	RTC_INIT();
	sendString("RTC Initialised");
	ADC_INIT();
	sendString("ADC Initialised");

	// setup debug output pin 
	PC_DDR |= 1 << 6;
	PC_CR1 |= 1 << 6;
	// bring it low
	PC_ODR &= ~(1 << 6);
}

void loop()
{
	float app,real;
	unsigned long appL,realL;
	int i = 0;
	PC_ODR |= 1 << 6;
	calcVI(8);
	PC_ODR &= ~(1 << 6);
	sendString("PM_START");
	sendFloatAsString(getVrms());
	
	while(i<HARDWARE_CHANNEL_NUM)
	{
		sendChar(i);
		sendFloatAsString(getIrms(i));
		sendFloatAsString(getRealPower(i));
		i++;
	}
	i = 0;
}
