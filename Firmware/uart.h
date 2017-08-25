#ifndef __UART_H
#define __UART_H

#include <float.h>
#include <string.h>
#include <stdio.h>
#include "stm8l051f3.h"

#define USART_CR2_TEN (1 << 3)
#define USART_CR3_STOP2 (1 << 5)
#define USART_CR3_STOP1 (1 << 4)
#define USART_SR_TXE (1 << 7)

void UART_INIT(void);
void sendDouble(double c);
void sendChar(unsigned char c);
void sendString(const char *str);
void sendFloatAsLong(double double_value);
void sendLong(unsigned long long_value);
void sendFloatAsString(float float_value);

void float_to_string(float f, char r[]);
int n_tu(int number, int count);

#endif