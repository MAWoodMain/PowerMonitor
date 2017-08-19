#include <string.h>
#include "stm8l051f3.h"

#define USART_CR2_TEN (1 << 3)
#define USART_CR3_STOP2 (1 << 5)
#define USART_CR3_STOP1 (1 << 4)
#define USART_SR_TXE (1 << 7)

void RTC_INIT(void);
void UART_INIT(void);

void sendChar(char c)
{
	while(!(USART1_SR & USART_SR_TXE));
	USART1_DR = c;
}

int uart_write(const char *str) {
	char i;
	for(i = 0; i < strlen(str); i++) sendChar(str[i]);
	return(i); // Bytes sent
}

main()
{
	unsigned long i = 0;
	
	CLK_CKDIVR = 0x00;
	
	UART_INIT();
	USART1_DR = 'x';
	//RTC_INIT();


	do {
		while(i < 47456) i++;
		sendChar('x');
		//PB_ODR |= 0x01;
		while(i > 0) i--;
		//PB_ODR &= ~0x01;
		sendChar('x');
		
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
	USART1_BRR2 = 0x03; 
	USART1_BRR1 = 0x68;
}

void RTC_INIT()
{
	// Enable peripheral
	CLK_PCKENR2 |= 0x04;
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
}