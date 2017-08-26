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
	long i = 0;
	
	while(i<HARDWARE_CHANNEL_NUM)
	{
		calcVI(VOLTAGE_CHANNEL, CHANNELS[i], 10);
		if(i==0) 
		{
			sendString("PM_START");
			//sendFloatAsLong(getVrms());
			sendFloatAsString(getVrms());
		}
		sendChar(i);
		sendFloatAsString(getIrms());
		sendFloatAsString(getRealPower());
		//app = getApparentPower();
		//real = getRealPower();
		//appL = app*1000000.0;
		//sendLong(appL);
		//realL = real*1000000.0;
		//sendLong(realL);
		i++;
	}
	i = 0;
	//while(i < 47456) i++;
	//while(i > 0) i--;
}
