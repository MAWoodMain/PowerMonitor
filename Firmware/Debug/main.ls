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
3522                     .const:	section	.text
3523  0000               L41:
3524  0000 0000b960      	dc.l	47456
3525                     ; 24 main()
3525                     ; 25 {
3526                     	switch	.text
3527  003e               _main:
3529  003e 5204          	subw	sp,#4
3530       00000004      OFST:	set	4
3533                     ; 26 	unsigned long i = 0;
3535  0040 ae0000        	ldw	x,#0
3536  0043 1f03          	ldw	(OFST-1,sp),x
3537  0045 ae0000        	ldw	x,#0
3538  0048 1f01          	ldw	(OFST-3,sp),x
3539                     ; 28 	CLK_CKDIVR = 0x00;
3541  004a 725f50c0      	clr	_CLK_CKDIVR
3542                     ; 29 	UART_INIT();
3544  004e ad6b          	call	_UART_INIT
3546                     ; 30 	uart_write("UART Initialised");
3548  0050 ae0014        	ldw	x,#L5632
3549  0053 adba          	call	_uart_write
3551                     ; 31 	RTC_INIT();
3553  0055 cd00e4        	call	_RTC_INIT
3555                     ; 32 	uart_write("RTC Initialised");
3557  0058 ae0004        	ldw	x,#L7632
3558  005b adb2          	call	_uart_write
3560  005d 2009          	jra	L7732
3561  005f               L5732:
3562                     ; 36 		while(i < 47456) i++;
3564  005f 96            	ldw	x,sp
3565  0060 1c0001        	addw	x,#OFST-3
3566  0063 a601          	ld	a,#1
3567  0065 cd0000        	call	c_lgadc
3569  0068               L7732:
3572  0068 96            	ldw	x,sp
3573  0069 1c0001        	addw	x,#OFST-3
3574  006c cd0000        	call	c_ltor
3576  006f ae0000        	ldw	x,#L41
3577  0072 cd0000        	call	c_lcmp
3579  0075 25e8          	jrult	L5732
3581  0077 2009          	jra	L5042
3582  0079               L3042:
3583                     ; 37 		while(i > 0) i--;
3585  0079 96            	ldw	x,sp
3586  007a 1c0001        	addw	x,#OFST-3
3587  007d a601          	ld	a,#1
3588  007f cd0000        	call	c_lgsbc
3590  0082               L5042:
3593  0082 96            	ldw	x,sp
3594  0083 1c0001        	addw	x,#OFST-3
3595  0086 cd0000        	call	c_lzmp
3597  0089 26ee          	jrne	L3042
3598                     ; 38 		sendChar(RTC_TR1 & 0x7F);
3600  008b c65140        	ld	a,_RTC_TR1
3601  008e a47f          	and	a,#127
3602  0090 cd0000        	call	_sendChar
3604                     ; 39 		sendChar(RTC_TR2 & 0x7F);
3606  0093 c65141        	ld	a,_RTC_TR2
3607  0096 a47f          	and	a,#127
3608  0098 cd0000        	call	_sendChar
3610                     ; 40 		sendChar(RTC_TR3 & 0x3F);
3612  009b c65142        	ld	a,_RTC_TR3
3613  009e a43f          	and	a,#63
3614  00a0 cd0000        	call	_sendChar
3616                     ; 41 		sendChar(RTC_DR1 & 0x3F);
3618  00a3 c65144        	ld	a,_RTC_DR1
3619  00a6 a43f          	and	a,#63
3620  00a8 cd0000        	call	_sendChar
3622                     ; 42 		sendChar(RTC_DR2 & 0x1F);
3624  00ab c65145        	ld	a,_RTC_DR2
3625  00ae a41f          	and	a,#31
3626  00b0 cd0000        	call	_sendChar
3628                     ; 43 		sendChar(RTC_DR3 & 0xFF);
3630  00b3 c65146        	ld	a,_RTC_DR3
3631  00b6 cd0000        	call	_sendChar
3634  00b9 20ad          	jra	L7732
3664                     ; 49 void UART_INIT()
3664                     ; 50 {
3665                     	switch	.text
3666  00bb               _UART_INIT:
3670                     ; 52 	CLK_PCKENR1 |= 0x20;
3672  00bb 721a50c3      	bset	_CLK_PCKENR1,#5
3673                     ; 55 	PC_DDR |= 0xFF;
3675  00bf c6500c        	ld	a,_PC_DDR
3676  00c2 aaff          	or	a,#255
3677  00c4 c7500c        	ld	_PC_DDR,a
3678                     ; 56 	PC_CR1 |= 0xFF;
3680  00c7 c6500d        	ld	a,_PC_CR1
3681  00ca aaff          	or	a,#255
3682  00cc c7500d        	ld	_PC_CR1,a
3683                     ; 59 	USART1_CR2 = USART_CR2_TEN;
3685  00cf 35085235      	mov	_USART1_CR2,#8
3686                     ; 62 	USART1_CR3 &= ~(USART_CR3_STOP1 | USART_CR3_STOP2);
3688  00d3 c65236        	ld	a,_USART1_CR3
3689  00d6 a4cf          	and	a,#207
3690  00d8 c75236        	ld	_USART1_CR3,a
3691                     ; 65 	USART1_BRR2 = 0x05; 
3693  00db 35055233      	mov	_USART1_BRR2,#5
3694                     ; 66 	USART1_BRR1 = 0x04;
3696  00df 35045232      	mov	_USART1_BRR1,#4
3697                     ; 67 }
3700  00e3 81            	ret
3734                     ; 69 void RTC_INIT()
3734                     ; 70 {
3735                     	switch	.text
3736  00e4               _RTC_INIT:
3740                     ; 72 	CLK_PCKENR2 |= 0x04;
3742  00e4 721450c4      	bset	_CLK_PCKENR2,#2
3743                     ; 74 	CLK_CRTCR |= 0x01;
3745  00e8 721050c1      	bset	_CLK_CRTCR,#0
3746                     ; 77 	RTC_WPR = 0xCA;
3748  00ec 35ca5159      	mov	_RTC_WPR,#202
3749                     ; 78 	RTC_WPR = 0x53;
3751  00f0 35535159      	mov	_RTC_WPR,#83
3752                     ; 81 	if ((RTC_ISR1 & 0x40) == 0)
3754  00f4 c6514c        	ld	a,_RTC_ISR1
3755  00f7 a540          	bcp	a,#64
3756  00f9 260b          	jrne	L1342
3757                     ; 84     RTC_ISR1 = 0x80;
3759  00fb 3580514c      	mov	_RTC_ISR1,#128
3761  00ff               L7342:
3762                     ; 87     while ((RTC_ISR1 & 0x40) == 0);
3764  00ff c6514c        	ld	a,_RTC_ISR1
3765  0102 a540          	bcp	a,#64
3766  0104 27f9          	jreq	L7342
3767  0106               L1342:
3768                     ; 90 	RTC_TR1 = 0x00;
3770  0106 725f5140      	clr	_RTC_TR1
3771                     ; 91 	RTC_TR2 = 0x30;
3773  010a 35305141      	mov	_RTC_TR2,#48
3774                     ; 92 	RTC_TR3 = 0x57;
3776  010e 35575142      	mov	_RTC_TR3,#87
3777                     ; 94 	RTC_DR1 = 0x19;
3779  0112 35195144      	mov	_RTC_DR1,#25
3780                     ; 95 	RTC_DR2 = 0xC8;
3782  0116 35c85145      	mov	_RTC_DR2,#200
3783                     ; 96 	RTC_DR3 = 0x17;
3785  011a 35175146      	mov	_RTC_DR3,#23
3786                     ; 98 	RTC_ISR1 =0x00;
3788  011e 725f514c      	clr	_RTC_ISR1
3789                     ; 99 	RTC_ISR2 =0x00;
3791  0122 725f514d      	clr	_RTC_ISR2
3792                     ; 100 	RTC_WPR = 0xFF; 
3794  0126 35ff5159      	mov	_RTC_WPR,#255
3795                     ; 101 }
3798  012a 81            	ret
3811                     	xdef	_main
3812                     	xdef	_uart_write
3813                     	xdef	_sendChar
3814                     	xdef	_UART_INIT
3815                     	xdef	_RTC_INIT
3816                     	xref	_strlen
3817                     	switch	.const
3818  0004               L7632:
3819  0004 52544320496e  	dc.b	"RTC Initialised",0
3820  0014               L5632:
3821  0014 554152542049  	dc.b	"UART Initialised",0
3822                     	xref.b	c_y
3842                     	xref	c_lzmp
3843                     	xref	c_lgsbc
3844                     	xref	c_lcmp
3845                     	xref	c_ltor
3846                     	xref	c_lgadc
3847                     	end
