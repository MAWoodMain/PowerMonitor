   1                     ; C Compiler for STM8 (COSMIC Software)
   2                     ; Parser V4.10.2 - 02 Nov 2011
   3                     ; Generator (Limited) V4.3.7 - 29 Nov 2011
3357                     ; 12 void sendChar(char c)
3357                     ; 13 {
3359                     	switch	.text
3360  0000               _sendChar:
3362  0000 88            	push	a
3363       00000000      OFST:	set	0
3366  0001               L1132:
3367                     ; 14 	while(!(USART1_SR & USART_SR_TXE));
3369  0001 c65230        	ld	a,_USART1_SR
3370  0004 a580          	bcp	a,#128
3371  0006 27f9          	jreq	L1132
3372                     ; 15 	USART1_DR = c;
3374  0008 7b01          	ld	a,(OFST+1,sp)
3375  000a c75231        	ld	_USART1_DR,a
3376                     ; 16 }
3379  000d 84            	pop	a
3380  000e 81            	ret
3426                     ; 18 int uart_write(const char *str) {
3427                     	switch	.text
3428  000f               _uart_write:
3430  000f 89            	pushw	x
3431  0010 88            	push	a
3432       00000001      OFST:	set	1
3435                     ; 20 	for(i = 0; i < strlen(str); i++) sendChar(str[i]);
3437  0011 0f01          	clr	(OFST+0,sp)
3439  0013 2010          	jra	L3432
3440  0015               L7332:
3443  0015 7b02          	ld	a,(OFST+1,sp)
3444  0017 97            	ld	xl,a
3445  0018 7b03          	ld	a,(OFST+2,sp)
3446  001a 1b01          	add	a,(OFST+0,sp)
3447  001c 2401          	jrnc	L01
3448  001e 5c            	incw	x
3449  001f               L01:
3450  001f 02            	rlwa	x,a
3451  0020 f6            	ld	a,(x)
3452  0021 addd          	call	_sendChar
3456  0023 0c01          	inc	(OFST+0,sp)
3457  0025               L3432:
3460  0025 1e02          	ldw	x,(OFST+1,sp)
3461  0027 cd0000        	call	_strlen
3463  002a 7b01          	ld	a,(OFST+0,sp)
3464  002c 905f          	clrw	y
3465  002e 9097          	ld	yl,a
3466  0030 90bf00        	ldw	c_y,y
3467  0033 b300          	cpw	x,c_y
3468  0035 22de          	jrugt	L7332
3469                     ; 21 	return(i); // Bytes sent
3471  0037 7b01          	ld	a,(OFST+0,sp)
3472  0039 5f            	clrw	x
3473  003a 97            	ld	xl,a
3476  003b 5b03          	addw	sp,#3
3477  003d 81            	ret
3515                     .const:	section	.text
3516  0000               L41:
3517  0000 0000b960      	dc.l	47456
3518                     ; 24 main()
3518                     ; 25 {
3519                     	switch	.text
3520  003e               _main:
3522  003e 5204          	subw	sp,#4
3523       00000004      OFST:	set	4
3526                     ; 26 	unsigned long i = 0;
3528  0040 ae0000        	ldw	x,#0
3529  0043 1f03          	ldw	(OFST-1,sp),x
3530  0045 ae0000        	ldw	x,#0
3531  0048 1f01          	ldw	(OFST-3,sp),x
3532                     ; 28 	CLK_CKDIVR = 0x00;
3534  004a 725f50c0      	clr	_CLK_CKDIVR
3535                     ; 30 	UART_INIT();
3537  004e ad3d          	call	_UART_INIT
3539                     ; 31 	USART1_DR = 'x';
3541  0050 35785231      	mov	_USART1_DR,#120
3542  0054 2009          	jra	L3732
3543  0056               L1732:
3544                     ; 36 		while(i < 47456) i++;
3546  0056 96            	ldw	x,sp
3547  0057 1c0001        	addw	x,#OFST-3
3548  005a a601          	ld	a,#1
3549  005c cd0000        	call	c_lgadc
3551  005f               L3732:
3554  005f 96            	ldw	x,sp
3555  0060 1c0001        	addw	x,#OFST-3
3556  0063 cd0000        	call	c_ltor
3558  0066 ae0000        	ldw	x,#L41
3559  0069 cd0000        	call	c_lcmp
3561  006c 25e8          	jrult	L1732
3562                     ; 37 		sendChar('x');
3564  006e a678          	ld	a,#120
3565  0070 ad8e          	call	_sendChar
3568  0072 2009          	jra	L1042
3569  0074               L7732:
3570                     ; 39 		while(i > 0) i--;
3572  0074 96            	ldw	x,sp
3573  0075 1c0001        	addw	x,#OFST-3
3574  0078 a601          	ld	a,#1
3575  007a cd0000        	call	c_lgsbc
3577  007d               L1042:
3580  007d 96            	ldw	x,sp
3581  007e 1c0001        	addw	x,#OFST-3
3582  0081 cd0000        	call	c_lzmp
3584  0084 26ee          	jrne	L7732
3585                     ; 41 		sendChar('x');
3587  0086 a678          	ld	a,#120
3588  0088 cd0000        	call	_sendChar
3591  008b 20d2          	jra	L3732
3622                     ; 47 void UART_INIT()
3622                     ; 48 {
3623                     	switch	.text
3624  008d               _UART_INIT:
3628                     ; 50 	CLK_PCKENR1 |= 0x20;
3630  008d 721a50c3      	bset	_CLK_PCKENR1,#5
3631                     ; 53 	PC_DDR |= 0xFF;
3633  0091 c6500c        	ld	a,_PC_DDR
3634  0094 aaff          	or	a,#255
3635  0096 c7500c        	ld	_PC_DDR,a
3636                     ; 54 	PC_CR1 |= 0xFF;
3638  0099 c6500d        	ld	a,_PC_CR1
3639  009c aaff          	or	a,#255
3640  009e c7500d        	ld	_PC_CR1,a
3641                     ; 57 	USART1_CR2 = USART_CR2_TEN;
3643  00a1 35085235      	mov	_USART1_CR2,#8
3644                     ; 60 	USART1_CR3 &= ~(USART_CR3_STOP1 | USART_CR3_STOP2);
3646  00a5 c65236        	ld	a,_USART1_CR3
3647  00a8 a4cf          	and	a,#207
3648  00aa c75236        	ld	_USART1_CR3,a
3649                     ; 63 	USART1_BRR2 = 0x03; 
3651  00ad 35035233      	mov	_USART1_BRR2,#3
3652                     ; 64 	USART1_BRR1 = 0x68;
3654  00b1 35685232      	mov	_USART1_BRR1,#104
3655                     ; 65 	USART1_PSCR |= 0x01;
3657  00b5 7210523a      	bset	_USART1_PSCR,#0
3658                     ; 66 }
3661  00b9 81            	ret
3694                     ; 68 void RTC_INIT()
3694                     ; 69 {
3695                     	switch	.text
3696  00ba               _RTC_INIT:
3700                     ; 71 	CLK_PCKENR2 |= 0x04;
3702  00ba 721450c4      	bset	_CLK_PCKENR2,#2
3703                     ; 72 	CLK_CRTCR |= 0x02;
3705  00be 721250c1      	bset	_CLK_CRTCR,#1
3706                     ; 75 	RTC_WPR = 0xCA;
3708  00c2 35ca5159      	mov	_RTC_WPR,#202
3709                     ; 76 	RTC_WPR = 0x53;
3711  00c6 35535159      	mov	_RTC_WPR,#83
3712                     ; 79 	if ((RTC_ISR1 & 0x40) == 0)
3714  00ca c6514c        	ld	a,_RTC_ISR1
3715  00cd a540          	bcp	a,#64
3716  00cf 260b          	jrne	L5242
3717                     ; 82     RTC_ISR1 = 0x80;
3719  00d1 3580514c      	mov	_RTC_ISR1,#128
3721  00d5               L3342:
3722                     ; 85     while ((RTC_ISR1 & 0x40) == 0);
3724  00d5 c6514c        	ld	a,_RTC_ISR1
3725  00d8 a540          	bcp	a,#64
3726  00da 27f9          	jreq	L3342
3727  00dc               L5242:
3728                     ; 88 	RTC_TR1 = 0x00;
3730  00dc 725f5140      	clr	_RTC_TR1
3731                     ; 89 	RTC_TR2 = 0x30;
3733  00e0 35305141      	mov	_RTC_TR2,#48
3734                     ; 90 	RTC_TR3 = 0x57;
3736  00e4 35575142      	mov	_RTC_TR3,#87
3737                     ; 92 	RTC_DR1 = 0x19;
3739  00e8 35195144      	mov	_RTC_DR1,#25
3740                     ; 93 	RTC_DR2 = 0xC8;
3742  00ec 35c85145      	mov	_RTC_DR2,#200
3743                     ; 94 	RTC_DR3 = 0x17;
3745  00f0 35175146      	mov	_RTC_DR3,#23
3746                     ; 96 	RTC_ISR1 &= ~0x80;
3748  00f4 721f514c      	bres	_RTC_ISR1,#7
3749                     ; 97 }
3752  00f8 81            	ret
3765                     	xdef	_main
3766                     	xdef	_uart_write
3767                     	xdef	_sendChar
3768                     	xdef	_UART_INIT
3769                     	xdef	_RTC_INIT
3770                     	xref	_strlen
3771                     	xref.b	c_y
3790                     	xref	c_lzmp
3791                     	xref	c_lgsbc
3792                     	xref	c_lcmp
3793                     	xref	c_ltor
3794                     	xref	c_lgadc
3795                     	end
