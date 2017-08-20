#include "rtc.h"

void RTC_INIT()
{
	// Enable peripheral
	CLK_PCKENR2 |= 0x04;
	// Select RTC clock source
	CLK_CRTCR |= 0x02;
	
	// Disable write protection
	RTC_WPR = 0xCA;
	RTC_WPR = 0x53;
	
	// If init mode flat not set
	if ((RTC_ISR1 & 0x40) == 0)
  {
    /* Set the Initialization mode */
    RTC_ISR1 = 0x80;

    /* Wait until INITF flag is set */
    while ((RTC_ISR1 & 0x40) == 0);
  }
	// Set time 13:35:02 24 hour mode
	RTC_TR1 = 0x02;
	RTC_TR2 = 0x35;
	RTC_TR3 = 0x53;
	// Set date 20/08/17 saturday
	RTC_DR1 = 0x20;
	RTC_DR2 = 0xC8;
	RTC_DR3 = 0x17;
	// Clear init bit
	RTC_ISR1 =0x00;
	RTC_ISR2 =0x00;
	RTC_WPR = 0xFF; 
}