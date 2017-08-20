#include "uart.h"

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

void sendChar(char c)
{
	while(!(USART1_SR & USART_SR_TXE));
	USART1_DR = c;
}

void sendString(const char *str) 
{
	char i;
	for(i = 0; i < strlen(str); i++) sendChar(str[i]);
}

void sendDouble(double double_value)
{
	char bytes[8];
	char i;
	memcpy(bytes, (unsigned char*) (&double_value), 8);
	for(i = 0; i < 8; i++) sendChar(bytes[i]);
}

void sendFloat(float float_value)
{
	char bytes[4];
	char i;
	memcpy(bytes, (unsigned char*) (&float_value), 4);
	for(i = 0; i < 4; i++) sendChar(bytes[i]);
}
