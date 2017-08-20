   1                     ; C Compiler for STM8 (COSMIC Software)
   2                     ; Parser V4.10.2 - 02 Nov 2011
   3                     ; Generator (Limited) V4.3.7 - 29 Nov 2011
3317                     .const:	section	.text
3318  0000               _currentChannels:
3319  0000 12            	dc.b	18
3320  0001 11            	dc.b	17
3321  0002 10            	dc.b	16
3322  0003 0f            	dc.b	15
3323  0004 0e            	dc.b	14
3324  0005 0d            	dc.b	13
3325  0006 0c            	dc.b	12
3326  0007 0b            	dc.b	11
3327  0008 04            	dc.b	4
3328  0009               _voltageChannel:
3329  0009 16            	dc.b	22
3371                     ; 18 void sendChar(char c)
3371                     ; 19 {
3373                     	switch	.text
3374  0000               _sendChar:
3376  0000 88            	push	a
3377       00000000      OFST:	set	0
3380  0001               L1132:
3381                     ; 20 	while(!(USART1_SR & USART_SR_TXE));
3383  0001 c65230        	ld	a,_USART1_SR
3384  0004 a580          	bcp	a,#128
3385  0006 27f9          	jreq	L1132
3386                     ; 21 	USART1_DR = c;
3388  0008 7b01          	ld	a,(OFST+1,sp)
3389  000a c75231        	ld	_USART1_DR,a
3390                     ; 22 }
3393  000d 84            	pop	a
3394  000e 81            	ret
3440                     ; 24 int sendString(const char *str) {
3441                     	switch	.text
3442  000f               _sendString:
3444  000f 89            	pushw	x
3445  0010 88            	push	a
3446       00000001      OFST:	set	1
3449                     ; 26 	for(i = 0; i < strlen(str); i++) sendChar(str[i]);
3451  0011 0f01          	clr	(OFST+0,sp)
3453  0013 2010          	jra	L3432
3454  0015               L7332:
3457  0015 7b02          	ld	a,(OFST+1,sp)
3458  0017 97            	ld	xl,a
3459  0018 7b03          	ld	a,(OFST+2,sp)
3460  001a 1b01          	add	a,(OFST+0,sp)
3461  001c 2401          	jrnc	L01
3462  001e 5c            	incw	x
3463  001f               L01:
3464  001f 02            	rlwa	x,a
3465  0020 f6            	ld	a,(x)
3466  0021 addd          	call	_sendChar
3470  0023 0c01          	inc	(OFST+0,sp)
3471  0025               L3432:
3474  0025 1e02          	ldw	x,(OFST+1,sp)
3475  0027 cd0000        	call	_strlen
3477  002a 7b01          	ld	a,(OFST+0,sp)
3478  002c 905f          	clrw	y
3479  002e 9097          	ld	yl,a
3480  0030 90bf00        	ldw	c_y,y
3481  0033 b300          	cpw	x,c_y
3482  0035 22de          	jrugt	L7332
3483                     ; 27 	return(i); // Bytes sent
3485  0037 7b01          	ld	a,(OFST+0,sp)
3486  0039 5f            	clrw	x
3487  003a 97            	ld	xl,a
3490  003b 5b03          	addw	sp,#3
3491  003d 81            	ret
3542                     	switch	.const
3543  000a               L41:
3544  000a 00000009      	dc.l	9
3545  000e               L61:
3546  000e 0000b960      	dc.l	47456
3547                     ; 30 main()
3547                     ; 31 {
3548                     	switch	.text
3549  003e               _main:
3551  003e 5206          	subw	sp,#6
3552       00000006      OFST:	set	6
3555                     ; 32 	unsigned long i = 0;
3557  0040 ae0000        	ldw	x,#0
3558  0043 1f05          	ldw	(OFST-1,sp),x
3559  0045 ae0000        	ldw	x,#0
3560  0048 1f03          	ldw	(OFST-3,sp),x
3561                     ; 33 	unsigned int adcValue = 0;
3563                     ; 35 	CLK_CKDIVR = 0x00;
3565  004a 725f50c0      	clr	_CLK_CKDIVR
3566                     ; 36 	UART_INIT();
3568  004e cd00da        	call	_UART_INIT
3570                     ; 38 	RTC_INIT();
3572  0051 cd0103        	call	_RTC_INIT
3574                     ; 40 	ADC_INIT();
3576  0054 cd014a        	call	_ADC_INIT
3578                     ; 43 	sendChar(0x00);
3580  0057 4f            	clr	a
3581  0058 ada6          	call	_sendChar
3583  005a               L1732:
3584                     ; 46 			adcValue = readChannel(voltageChannel);
3586  005a ae0016        	ldw	x,#22
3587  005d cd015b        	call	_readChannel
3589  0060 1f01          	ldw	(OFST-5,sp),x
3590                     ; 47 			sendChar('V');
3592  0062 a656          	ld	a,#86
3593  0064 ad9a          	call	_sendChar
3595                     ; 48 			sendChar((char)(adcValue >> 8));
3597  0066 7b01          	ld	a,(OFST-5,sp)
3598  0068 ad96          	call	_sendChar
3600                     ; 49 			sendChar((char)adcValue);
3602  006a 7b02          	ld	a,(OFST-4,sp)
3603  006c ad92          	call	_sendChar
3606  006e 2023          	jra	L7732
3607  0070               L5732:
3608                     ; 52 			adcValue = readChannel(currentChannels[i]);
3610  0070 1e05          	ldw	x,(OFST-1,sp)
3611  0072 d60000        	ld	a,(_currentChannels,x)
3612  0075 5f            	clrw	x
3613  0076 97            	ld	xl,a
3614  0077 cd015b        	call	_readChannel
3616  007a 1f01          	ldw	(OFST-5,sp),x
3617                     ; 53 			sendChar(i);
3619  007c 7b06          	ld	a,(OFST+0,sp)
3620  007e ad80          	call	_sendChar
3622                     ; 54 			sendChar((char)(adcValue >> 8));
3624  0080 7b01          	ld	a,(OFST-5,sp)
3625  0082 cd0000        	call	_sendChar
3627                     ; 55 			sendChar((char)adcValue);
3629  0085 7b02          	ld	a,(OFST-4,sp)
3630  0087 cd0000        	call	_sendChar
3632                     ; 56 			i++;
3634  008a 96            	ldw	x,sp
3635  008b 1c0003        	addw	x,#OFST-3
3636  008e a601          	ld	a,#1
3637  0090 cd0000        	call	c_lgadc
3639  0093               L7732:
3640                     ; 50 		while(i<9)
3642  0093 96            	ldw	x,sp
3643  0094 1c0003        	addw	x,#OFST-3
3644  0097 cd0000        	call	c_ltor
3646  009a ae000a        	ldw	x,#L41
3647  009d cd0000        	call	c_lcmp
3649  00a0 25ce          	jrult	L5732
3650                     ; 58 		i = 0;
3652  00a2 ae0000        	ldw	x,#0
3653  00a5 1f05          	ldw	(OFST-1,sp),x
3654  00a7 ae0000        	ldw	x,#0
3655  00aa 1f03          	ldw	(OFST-3,sp),x
3656  00ac               L3042:
3657                     ; 59 		while(i < 47456) i++;
3660  00ac 96            	ldw	x,sp
3661  00ad 1c0003        	addw	x,#OFST-3
3662  00b0 a601          	ld	a,#1
3663  00b2 cd0000        	call	c_lgadc
3667  00b5 96            	ldw	x,sp
3668  00b6 1c0003        	addw	x,#OFST-3
3669  00b9 cd0000        	call	c_ltor
3671  00bc ae000e        	ldw	x,#L61
3672  00bf cd0000        	call	c_lcmp
3674  00c2 25e8          	jrult	L3042
3676  00c4 2009          	jra	L3142
3677  00c6               L1142:
3678                     ; 60 		while(i > 0) i--;
3680  00c6 96            	ldw	x,sp
3681  00c7 1c0003        	addw	x,#OFST-3
3682  00ca a601          	ld	a,#1
3683  00cc cd0000        	call	c_lgsbc
3685  00cf               L3142:
3688  00cf 96            	ldw	x,sp
3689  00d0 1c0003        	addw	x,#OFST-3
3690  00d3 cd0000        	call	c_lzmp
3692  00d6 26ee          	jrne	L1142
3694  00d8 2080          	jpf	L1732
3724                     ; 65 void UART_INIT()
3724                     ; 66 {
3725                     	switch	.text
3726  00da               _UART_INIT:
3730                     ; 68 	CLK_PCKENR1 |= 0x20;
3732  00da 721a50c3      	bset	_CLK_PCKENR1,#5
3733                     ; 71 	PC_DDR |= 0xFF;
3735  00de c6500c        	ld	a,_PC_DDR
3736  00e1 aaff          	or	a,#255
3737  00e3 c7500c        	ld	_PC_DDR,a
3738                     ; 72 	PC_CR1 |= 0xFF;
3740  00e6 c6500d        	ld	a,_PC_CR1
3741  00e9 aaff          	or	a,#255
3742  00eb c7500d        	ld	_PC_CR1,a
3743                     ; 75 	USART1_CR2 = USART_CR2_TEN;
3745  00ee 35085235      	mov	_USART1_CR2,#8
3746                     ; 78 	USART1_CR3 &= ~(USART_CR3_STOP1 | USART_CR3_STOP2);
3748  00f2 c65236        	ld	a,_USART1_CR3
3749  00f5 a4cf          	and	a,#207
3750  00f7 c75236        	ld	_USART1_CR3,a
3751                     ; 81 	USART1_BRR2 = 0x05; 
3753  00fa 35055233      	mov	_USART1_BRR2,#5
3754                     ; 82 	USART1_BRR1 = 0x04;
3756  00fe 35045232      	mov	_USART1_BRR1,#4
3757                     ; 83 }
3760  0102 81            	ret
3794                     ; 85 void RTC_INIT()
3794                     ; 86 {
3795                     	switch	.text
3796  0103               _RTC_INIT:
3800                     ; 88 	CLK_PCKENR2 |= 0x04;
3802  0103 721450c4      	bset	_CLK_PCKENR2,#2
3803                     ; 90 	CLK_CRTCR |= 0x02;
3805  0107 721250c1      	bset	_CLK_CRTCR,#1
3806                     ; 93 	RTC_WPR = 0xCA;
3808  010b 35ca5159      	mov	_RTC_WPR,#202
3809                     ; 94 	RTC_WPR = 0x53;
3811  010f 35535159      	mov	_RTC_WPR,#83
3812                     ; 97 	if ((RTC_ISR1 & 0x40) == 0)
3814  0113 c6514c        	ld	a,_RTC_ISR1
3815  0116 a540          	bcp	a,#64
3816  0118 260b          	jrne	L7342
3817                     ; 100     RTC_ISR1 = 0x80;
3819  011a 3580514c      	mov	_RTC_ISR1,#128
3821  011e               L5442:
3822                     ; 103     while ((RTC_ISR1 & 0x40) == 0);
3824  011e c6514c        	ld	a,_RTC_ISR1
3825  0121 a540          	bcp	a,#64
3826  0123 27f9          	jreq	L5442
3827  0125               L7342:
3828                     ; 106 	RTC_TR1 = 0x02;
3830  0125 35025140      	mov	_RTC_TR1,#2
3831                     ; 107 	RTC_TR2 = 0x35;
3833  0129 35355141      	mov	_RTC_TR2,#53
3834                     ; 108 	RTC_TR3 = 0x53;
3836  012d 35535142      	mov	_RTC_TR3,#83
3837                     ; 110 	RTC_DR1 = 0x20;
3839  0131 35205144      	mov	_RTC_DR1,#32
3840                     ; 111 	RTC_DR2 = 0xC8;
3842  0135 35c85145      	mov	_RTC_DR2,#200
3843                     ; 112 	RTC_DR3 = 0x17;
3845  0139 35175146      	mov	_RTC_DR3,#23
3846                     ; 114 	RTC_ISR1 =0x00;
3848  013d 725f514c      	clr	_RTC_ISR1
3849                     ; 115 	RTC_ISR2 =0x00;
3851  0141 725f514d      	clr	_RTC_ISR2
3852                     ; 116 	RTC_WPR = 0xFF; 
3854  0145 35ff5159      	mov	_RTC_WPR,#255
3855                     ; 117 }
3858  0149 81            	ret
3885                     ; 119 void ADC_INIT()
3885                     ; 120 {
3886                     	switch	.text
3887  014a               _ADC_INIT:
3891                     ; 122 	CLK_PCKENR2 |= 0x01;
3893  014a 721050c4      	bset	_CLK_PCKENR2,#0
3894                     ; 129 	ADC1_CR1 = 0x01;
3896  014e 35015340      	mov	_ADC1_CR1,#1
3897                     ; 132 	ADC1_CR2 = 0x07;
3899  0152 35075341      	mov	_ADC1_CR2,#7
3900                     ; 134 	ADC1_CR3 = 0xE0;
3902  0156 35e05342      	mov	_ADC1_CR3,#224
3903                     ; 135 }
3906  015a 81            	ret
3948                     ; 137 int readChannel(int adcChannel)
3948                     ; 138 {
3949                     	switch	.text
3950  015b               _readChannel:
3952  015b 89            	pushw	x
3953       00000000      OFST:	set	0
3956                     ; 139 	ADC1_CR3 &= ~0x1F;
3958  015c c65342        	ld	a,_ADC1_CR3
3959  015f a4e0          	and	a,#224
3960  0161 c75342        	ld	_ADC1_CR3,a
3961                     ; 140 	ADC1_CR3 |= (0x1F & adcChannel);
3963  0164 9f            	ld	a,xl
3964  0165 a41f          	and	a,#31
3965  0167 ca5342        	or	a,_ADC1_CR3
3966  016a c75342        	ld	_ADC1_CR3,a
3967                     ; 142 	ADC1_SQR2 = 0;
3969  016d 725f534b      	clr	_ADC1_SQR2
3970                     ; 143 	ADC1_SQR3 = 0;
3972  0171 725f534c      	clr	_ADC1_SQR3
3973                     ; 144 	ADC1_SQR4 = 0;
3975  0175 725f534d      	clr	_ADC1_SQR4
3976                     ; 146 	if (adcChannel > 15)
3978  0179 9c            	rvf
3979  017a 1e01          	ldw	x,(OFST+1,sp)
3980  017c a30010        	cpw	x,#16
3981  017f 2f18          	jrslt	L7742
3982                     ; 148 		ADC1_SQR2 = (0x01 << (adcChannel-16));
3984  0181 7b02          	ld	a,(OFST+2,sp)
3985  0183 a010          	sub	a,#16
3986  0185 5f            	clrw	x
3987  0186 4d            	tnz	a
3988  0187 2a01          	jrpl	L03
3989  0189 53            	cplw	x
3990  018a               L03:
3991  018a 97            	ld	xl,a
3992  018b a601          	ld	a,#1
3993  018d 5d            	tnzw	x
3994  018e 2704          	jreq	L23
3995  0190               L43:
3996  0190 48            	sll	a
3997  0191 5a            	decw	x
3998  0192 26fc          	jrne	L43
3999  0194               L23:
4000  0194 c7534b        	ld	_ADC1_SQR2,a
4002  0197 2034          	jra	L1052
4003  0199               L7742:
4004                     ; 150 	else if (adcChannel > 7)
4006  0199 9c            	rvf
4007  019a 1e01          	ldw	x,(OFST+1,sp)
4008  019c a30008        	cpw	x,#8
4009  019f 2f18          	jrslt	L3052
4010                     ; 152 		ADC1_SQR3 = (0x01 << (adcChannel-8));
4012  01a1 7b02          	ld	a,(OFST+2,sp)
4013  01a3 a008          	sub	a,#8
4014  01a5 5f            	clrw	x
4015  01a6 4d            	tnz	a
4016  01a7 2a01          	jrpl	L63
4017  01a9 53            	cplw	x
4018  01aa               L63:
4019  01aa 97            	ld	xl,a
4020  01ab a601          	ld	a,#1
4021  01ad 5d            	tnzw	x
4022  01ae 2704          	jreq	L04
4023  01b0               L24:
4024  01b0 48            	sll	a
4025  01b1 5a            	decw	x
4026  01b2 26fc          	jrne	L24
4027  01b4               L04:
4028  01b4 c7534c        	ld	_ADC1_SQR3,a
4030  01b7 2014          	jra	L1052
4031  01b9               L3052:
4032                     ; 156 		ADC1_SQR4 = (0x01 << adcChannel);
4034  01b9 7b02          	ld	a,(OFST+2,sp)
4035  01bb 5f            	clrw	x
4036  01bc 4d            	tnz	a
4037  01bd 2a01          	jrpl	L44
4038  01bf 53            	cplw	x
4039  01c0               L44:
4040  01c0 97            	ld	xl,a
4041  01c1 a601          	ld	a,#1
4042  01c3 5d            	tnzw	x
4043  01c4 2704          	jreq	L64
4044  01c6               L05:
4045  01c6 48            	sll	a
4046  01c7 5a            	decw	x
4047  01c8 26fc          	jrne	L05
4048  01ca               L64:
4049  01ca c7534d        	ld	_ADC1_SQR4,a
4050  01cd               L1052:
4051                     ; 158 	ADC1_CR1 |= 0x02;
4053  01cd 72125340      	bset	_ADC1_CR1,#1
4055  01d1               L1152:
4056                     ; 160 	while(!(ADC1_SR & 0x01));
4058  01d1 c65343        	ld	a,_ADC1_SR
4059  01d4 a501          	bcp	a,#1
4060  01d6 27f9          	jreq	L1152
4061                     ; 161 	return (ADC1_DRH << 8)|ADC1_DRL;
4063  01d8 c65344        	ld	a,_ADC1_DRH
4064  01db 5f            	clrw	x
4065  01dc 97            	ld	xl,a
4066  01dd c65345        	ld	a,_ADC1_DRL
4067  01e0 02            	rlwa	x,a
4070  01e1 5b02          	addw	sp,#2
4071  01e3 81            	ret
4105                     	xdef	_main
4106                     	xdef	_sendString
4107                     	xdef	_sendChar
4108                     	xdef	_readChannel
4109                     	xdef	_ADC_INIT
4110                     	xdef	_RTC_INIT
4111                     	xdef	_UART_INIT
4112                     	xdef	_voltageChannel
4113                     	xdef	_currentChannels
4114                     	xref	_strlen
4115                     	xref.b	c_y
4134                     	xref	c_lzmp
4135                     	xref	c_lgsbc
4136                     	xref	c_lcmp
4137                     	xref	c_ltor
4138                     	xref	c_lgadc
4139                     	end
