#ifndef __UART_H
#define __UART_H

#include <float.h>
#include <string.h>
#include "stm8l051f3.h"

#define USART_CR2_TEN (1 << 3)
#define USART_CR3_STOP2 (1 << 5)
#define USART_CR3_STOP1 (1 << 4)
#define USART_SR_TXE (1 << 7)

void UART_INIT(void);
void sendDouble(double c);
void sendChar(char c);
void sendString(const char *str);

#endif