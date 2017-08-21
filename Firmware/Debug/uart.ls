   1                     ; C Compiler for STM8 (COSMIC Software)
   2                     ; Parser V4.10.2 - 02 Nov 2011
   3                     ; Generator (Limited) V4.3.7 - 29 Nov 2011
3351                     ; 3 void UART_INIT()
3351                     ; 4 {
3353                     	switch	.text
3354  0000               _UART_INIT:
3358                     ; 6 	CLK_PCKENR1 |= 0x20;
3360  0000 721a50c3      	bset	_CLK_PCKENR1,#5
3361                     ; 9 	PC_DDR |= 0xFF;
3363  0004 c6500c        	ld	a,_PC_DDR
3364  0007 aaff          	or	a,#255
3365  0009 c7500c        	ld	_PC_DDR,a
3366                     ; 10 	PC_CR1 |= 0xFF;
3368  000c c6500d        	ld	a,_PC_CR1
3369  000f aaff          	or	a,#255
3370  0011 c7500d        	ld	_PC_CR1,a
3371                     ; 13 	USART1_CR2 = USART_CR2_TEN;
3373  0014 35085235      	mov	_USART1_CR2,#8
3374                     ; 16 	USART1_CR3 &= ~(USART_CR3_STOP1 | USART_CR3_STOP2);
3376  0018 c65236        	ld	a,_USART1_CR3
3377  001b a4cf          	and	a,#207
3378  001d c75236        	ld	_USART1_CR3,a
3379                     ; 19 	USART1_BRR2 = 0x05; 
3381  0020 35055233      	mov	_USART1_BRR2,#5
3382                     ; 20 	USART1_BRR1 = 0x04;
3384  0024 35045232      	mov	_USART1_BRR1,#4
3385                     ; 21 }
3388  0028 81            	ret
3424                     ; 23 void sendChar(char c)
3424                     ; 24 {
3425                     	switch	.text
3426  0029               _sendChar:
3428  0029 88            	push	a
3429       00000000      OFST:	set	0
3432  002a               L1232:
3433                     ; 25 	while(!(USART1_SR & USART_SR_TXE));
3435  002a c65230        	ld	a,_USART1_SR
3436  002d a580          	bcp	a,#128
3437  002f 27f9          	jreq	L1232
3438                     ; 26 	USART1_DR = c;
3440  0031 7b01          	ld	a,(OFST+1,sp)
3441  0033 c75231        	ld	_USART1_DR,a
3442                     ; 27 }
3445  0036 84            	pop	a
3446  0037 81            	ret
3492                     ; 29 void sendString(const char *str) 
3492                     ; 30 {
3493                     	switch	.text
3494  0038               _sendString:
3496  0038 89            	pushw	x
3497  0039 88            	push	a
3498       00000001      OFST:	set	1
3501                     ; 32 	for(i = 0; i < strlen(str); i++) sendChar(str[i]);
3503  003a 0f01          	clr	(OFST+0,sp)
3505  003c 2010          	jra	L3532
3506  003e               L7432:
3509  003e 7b02          	ld	a,(OFST+1,sp)
3510  0040 97            	ld	xl,a
3511  0041 7b03          	ld	a,(OFST+2,sp)
3512  0043 1b01          	add	a,(OFST+0,sp)
3513  0045 2401          	jrnc	L21
3514  0047 5c            	incw	x
3515  0048               L21:
3516  0048 02            	rlwa	x,a
3517  0049 f6            	ld	a,(x)
3518  004a addd          	call	_sendChar
3522  004c 0c01          	inc	(OFST+0,sp)
3523  004e               L3532:
3526  004e 1e02          	ldw	x,(OFST+1,sp)
3527  0050 cd0000        	call	_strlen
3529  0053 7b01          	ld	a,(OFST+0,sp)
3530  0055 905f          	clrw	y
3531  0057 9097          	ld	yl,a
3532  0059 90bf00        	ldw	c_y,y
3533  005c b300          	cpw	x,c_y
3534  005e 22de          	jrugt	L7432
3535                     ; 33 }
3538  0060 5b03          	addw	sp,#3
3539  0062 81            	ret
3594                     ; 35 void sendDouble(double double_value)
3594                     ; 36 {
3595                     	switch	.text
3596  0063               _sendDouble:
3598  0063 5209          	subw	sp,#9
3599       00000009      OFST:	set	9
3602                     ; 39 	memcpy(bytes, (unsigned char*) (&double_value), 8);
3604  0065 96            	ldw	x,sp
3605  0066 1c0001        	addw	x,#OFST-8
3606  0069 bf00          	ldw	c_x,x
3607  006b 9096          	ldw	y,sp
3608  006d 72a9000c      	addw	y,#OFST+3
3609  0071 90bf00        	ldw	c_y,y
3610  0074 ae0008        	ldw	x,#8
3611  0077               L61:
3612  0077 5a            	decw	x
3613  0078 92d600        	ld	a,([c_y.w],x)
3614  007b 92d700        	ld	([c_x.w],x),a
3615  007e 5d            	tnzw	x
3616  007f 26f6          	jrne	L61
3617                     ; 40 	for(i = 0; i < 8; i++) sendChar(bytes[i]);
3619  0081 0f09          	clr	(OFST+0,sp)
3620  0083               L5042:
3623  0083 96            	ldw	x,sp
3624  0084 1c0001        	addw	x,#OFST-8
3625  0087 9f            	ld	a,xl
3626  0088 5e            	swapw	x
3627  0089 1b09          	add	a,(OFST+0,sp)
3628  008b 2401          	jrnc	L02
3629  008d 5c            	incw	x
3630  008e               L02:
3631  008e 02            	rlwa	x,a
3632  008f f6            	ld	a,(x)
3633  0090 ad97          	call	_sendChar
3637  0092 0c09          	inc	(OFST+0,sp)
3640  0094 7b09          	ld	a,(OFST+0,sp)
3641  0096 a108          	cp	a,#8
3642  0098 25e9          	jrult	L5042
3643                     ; 41 }
3646  009a 5b09          	addw	sp,#9
3647  009c 81            	ret
3702                     ; 43 void sendFloat(float float_value)
3702                     ; 44 {
3703                     	switch	.text
3704  009d               _sendFloat:
3706  009d 5205          	subw	sp,#5
3707       00000005      OFST:	set	5
3710                     ; 47 	memcpy(bytes, (unsigned char*) (&float_value), 4);
3712  009f 96            	ldw	x,sp
3713  00a0 1c0001        	addw	x,#OFST-4
3714  00a3 bf00          	ldw	c_x,x
3715  00a5 9096          	ldw	y,sp
3716  00a7 72a90008      	addw	y,#OFST+3
3717  00ab 90bf00        	ldw	c_y,y
3718  00ae ae0004        	ldw	x,#4
3719  00b1               L42:
3720  00b1 5a            	decw	x
3721  00b2 92d600        	ld	a,([c_y.w],x)
3722  00b5 92d700        	ld	([c_x.w],x),a
3723  00b8 5d            	tnzw	x
3724  00b9 26f6          	jrne	L42
3725                     ; 48 	for(i = 0; i < 4; i++) sendChar(bytes[i]);
3727  00bb 0f05          	clr	(OFST+0,sp)
3728  00bd               L1442:
3731  00bd 96            	ldw	x,sp
3732  00be 1c0001        	addw	x,#OFST-4
3733  00c1 9f            	ld	a,xl
3734  00c2 5e            	swapw	x
3735  00c3 1b05          	add	a,(OFST+0,sp)
3736  00c5 2401          	jrnc	L62
3737  00c7 5c            	incw	x
3738  00c8               L62:
3739  00c8 02            	rlwa	x,a
3740  00c9 f6            	ld	a,(x)
3741  00ca cd0029        	call	_sendChar
3745  00cd 0c05          	inc	(OFST+0,sp)
3748  00cf 7b05          	ld	a,(OFST+0,sp)
3749  00d1 a104          	cp	a,#4
3750  00d3 25e8          	jrult	L1442
3751                     ; 49 }
3754  00d5 5b05          	addw	sp,#5
3755  00d7 81            	ret
3768                     	xdef	_sendFloat
3769                     	xdef	_sendString
3770                     	xdef	_sendChar
3771                     	xdef	_sendDouble
3772                     	xdef	_UART_INIT
3773                     	xref	_strlen
3774                     	xref.b	c_x
3775                     	xref.b	c_y
3794                     	end
