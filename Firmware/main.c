#include <string.h>
#include "stm8l051f3.h"

#define USART_CR2_TEN (1 << 3)
#define USART_CR3_STOP2 (1 << 5)
#define USART_CR3_STOP1 (1 << 4)
#define USART_SR_TXE (1 << 7)

#define NO_OF_CHANNELS 9;
const char currentChannels[] = {18,17,16,15,14,13,12,11,4};
const char voltageChannel = 22;

void UART_INIT(void);
void RTC_INIT(void);
void ADC_INIT(void);
int readChannel(int adcChannel);

void sendChar(char c)
{
	while(!(USART1_SR & USART_SR_TXE));
	USART1_DR = c;
}

int sendString(const char *str) {
	char i;
	for(i = 0; i < strlen(str); i++) sendChar(str[i]);
	return(i); // Bytes sent
}

main()
{
	unsigned long i = 0;
	unsigned int adcValue = 0;
	
	CLK_CKDIVR = 0x00;
	UART_INIT();
	//uart_write("UART Initialised");
	RTC_INIT();
	//uart_write("RTC Initialised");
	ADC_INIT();
	//uart_write("ADC Initialised");

	sendChar(0x00);

	do {
			adcValue = readChannel(voltageChannel);
			sendChar('V');
			sendChar((char)(adcValue >> 8));
			sendChar((char)adcValue);
		while(i<9)
		{
			adcValue = readChannel(currentChannels[i]);
			sendChar(i);
			sendChar((char)(adcValue >> 8));
			sendChar((char)adcValue);
			i++;
		}
		i = 0;
		while(i < 47456) i++;
		while(i > 0) i--;
	} while(1);

}

void UART_INIT()
{
	// Enable peripheral
	CLK_PCKENR1 |= 0x20;

	// Put TX line on
	PC_DDR |= 0xFF;
	PC_CR1 |= 0xFF;

	// Allow TX & RX
	USART1_CR2 = USART_CR2_TEN;
	
	// 1 stop bit
	USART1_CR3 &= ~(USART_CR3_STOP1 | USART_CR3_STOP2);
	
	// 9600 baud
	USART1_BRR2 = 0x05; 
	USART1_BRR1 = 0x04;
}

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
	ADC1_CR2 = 0x07;
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
