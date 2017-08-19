#include <string.h>
#include "STM8L051F3.h"
#include "main.h"

int RTC_INIT();

int RTC_INIT()
{
	CLK_PCKENR2 |= 0x04;
	
	// Disable write protection
	RTC_WPR = 0xCA;
	RTC_WPR = 0x53;
	// Set init flat
	RTC_ISR1 |= 0x80;
	// Wait for it to enter init mode
	while(!(RTC_ISR1 & 0x40));
	
	// Set time 17:30:00 24 hour mode
	RTC_TR1 = 0x00;
	RTC_TR2 = 0x30;
	RTC_TR3 = 0x57;
	// Set date 18/08/17 saturday
	RTC_DR1 = 0x19;
	RTC_DR2 = 0xC8;
	RTC_DR3 = 0x17;
	// Clear init bit
	RTC_ISR1 &= ~0x80;
	return 1;
}

main()
{
	unsigned long i = 0;
	
	RTC_INIT();

	CLK_CKDIVR = 0x00; // Set the frequency to 16 MHz
	CLK_PCKENR1 = 0x20; // Enable peripherals

	PC_DDR = 0x08; // Put TX line on
	PC_CR1 = 0x08;

	USART1_CR2 = 1 << 3; // Allow TX & RX
	USART1_CR3 &= ~(1 << 4 | 1 << 5); // 1 stop bit
	USART1_BRR2 = 0x03; 
	USART1_BRR1 = 0x68; // 9600 baud

	do {
		while(!(USART1_SR & 1 << 7));
		USART1_DR = RTC_TR1;
	} while(1);
}